package it.unibo.scall
package parser

import ast.CSTNode
import ast.CSTNode.*
import grammar.Element.{Nonterminal, Terminal}
import grammar.ProcessedGrammar.{AnyNonterminal, AnySymbol, SymbolSeq}
import lexer.Token
import parser.ParsingTable.{Eof, ParsingTable, TerminalOrEof}
import parser.Parsing.*

enum ParseError:
  case UnexpectedToken(expected: Seq[String], found: Token.Valid)
  case UnexpectedEndOfInput(expected: Seq[String])
  case LexicalError(token: Token.Error)
  case TrailingInput(token: Token)

case class ParseReport(tree: CSTNode, errors: Seq[ParseError]):
  def isValid: Boolean = errors.isEmpty

class Parser(table: ParsingTable, startSymbol: Nonterminal):
  import ParseError.*

  private type Sync = Set[TerminalOrEof]

  def parseAll(tokens: LazyList[Token]): ParseReport =
    val step = parseProgram.run(tokens)
    ParseReport(step.value, step.errors)

  def parse(tokens: LazyList[Token]): Either[ParseError, CSTNode] =
    val report = parseAll(tokens)
    report.errors.headOption.toLeft(report.tree)

  private def parseProgram: Parsing[CSTNode] =
    for
      tree <- parseSymbol(startSymbol)(using Set(Eof))
      _ <- trailingInput
    yield tree

  private def trailingInput: Parsing[Unit] =
    peek.flatMap:
      case Some(token) => record(TrailingInput(token))
      case None => pure(())

  private def parseSymbol(symbol: AnySymbol)(using sync: Sync): Parsing[CSTNode] =
    lookahead.flatMap: next =>
      expand(symbol, next).getOrElse:
        for
          _ <- record(unexpected(symbol, next))
          junk <- skipUntil(symbol.starters union sync)
          resumed <- lookahead
          node <- expand(symbol, resumed).getOrElse(pure(ErrorNode(symbol, junk)))
        yield node

  private def expand(symbol: AnySymbol, next: Option[Token.Valid])(using Sync): Option[Parsing[CSTNode]] =
    (symbol, next) match
      case (terminal: Terminal, Some(token)) if token.terminal == terminal =>
        Some(advance andThen pure(LeafNode(token)))
      case (nonterminal: AnyNonterminal, _) =>
        table.get((nonterminal, next.terminal)).map: production =>
          parseSequence(production).map(RuleNode(nonterminal, _))
      case _ => None

  private def parseSequence(symbols: SymbolSeq)(using sync: Sync): Parsing[Seq[CSTNode]] =
    symbols match
      case Seq() => pure(Seq.empty)
      case symbol +: rest =>
        for
          node <- parseSymbol(symbol)(using sync union rest.starters)
          nodes <- parseSequence(rest)
        yield node +: nodes

  private def lookahead: Parsing[Option[Token.Valid]] =
    peek.flatMap:
      case Some(error: Token.Error) => record(LexicalError(error)) andThen advance andThen lookahead
      case next => pure(next.collect { case valid: Token.Valid => valid})

  private def skipUntil(resume: Sync): Parsing[Seq[Token.Valid]] =
    lookahead.flatMap:
      case Some(token) if !resume.contains(token.terminal) =>
        advance andThen skipUntil(resume).map(token +: _)
      case _ => pure(Seq.empty)

  private def unexpected(symbol: AnySymbol, next: Option[Token.Valid]): ParseError =
    val expected = symbol.starters.map(_.name).toSeq.sorted
    next match
      case Some(token) => UnexpectedToken(expected, token)
      case None => UnexpectedEndOfInput(expected)

  extension (symbol: AnySymbol)
    private def starters: Sync = symbol match
      case terminal: Terminal => Set(terminal)
      case nonterminal: AnyNonterminal =>
        table.keys.collect { case (`nonterminal`, terminal) => terminal}.toSet

  extension (symbols: SymbolSeq)
    private def starters: Sync = symbols.flatMap(_.starters).toSet

  extension (next: Option[Token.Valid])
    private def terminal: TerminalOrEof = next.map(_.terminal).getOrElse(Eof)

  extension (terminal: TerminalOrEof)
    private def name: String = terminal match
      case t: Terminal => t.name
      case Eof => "end of input"
