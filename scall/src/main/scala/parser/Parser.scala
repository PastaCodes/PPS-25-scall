package it.unibo.scall
package parser

import ast.CSTNode
import ast.CSTNode.*
import grammar.Element.{Nonterminal, Terminal, TerminalOrEoi, Eoi}
import grammar.ProcessedGrammar.{AnyNonterminal, AnySymbol, SymbolSeq, InternalNonterminal}
import lexer.Token
import parser.ParsingTable.ParsingTable
import parser.Parsing.*

case class ParseReport(tree: CSTNode, errors: Seq[ParseError]):
  def isValid: Boolean = errors.isEmpty

class Parser(table: ParsingTable, startSymbol: Nonterminal):
  import ParseError.*

  private type Sync = Set[TerminalOrEoi]

  def parseAll(tokens: LazyList[Token]): ParseReport =
    val step = parseProgram.run(tokens)
    ParseReport(step.value, step.errors)

  def parse(tokens: LazyList[Token]): Either[ParseError, CSTNode] =
    val report = parseAll(tokens)
    report.errors.headOption.toLeft(report.tree)

  private def parseProgram: Parsing[CSTNode] =
    for
      nodes <- parseSymbol(startSymbol)(using Set(Eoi))
      _ <- trailingInput
    yield nodes.head

  private def trailingInput: Parsing[Unit] =
    peek.flatMap:
      case Some(token) => record(TrailingInput(token))
      case None => pure(())

  private def parseSymbol(symbol: AnySymbol)(using sync: Sync): Parsing[Seq[CSTNode]] =
    lookahead.flatMap: next =>
      expand(symbol, next).getOrElse:
        for
          _ <- record(unexpected(symbol, next))
          junk <- skipUntil(symbol.starters union sync)
          resumed <- lookahead
          node <- expand(symbol, resumed).getOrElse(pure(Seq(ErrorNode(symbol.starters, junk))))
        yield node

  private def expand(symbol: AnySymbol, next: Option[Token.Valid])(using Sync): Option[Parsing[Seq[CSTNode]]] =
    (symbol, next) match
      case (terminal: Terminal, Some(token)) if token.terminal == terminal =>
        Some(advance andThen pure(Seq(LeafNode(token))))
      case (nonterminal: AnyNonterminal, _) =>
        table.get((nonterminal, next.terminal)).map: production =>
          parseSequence(production).map(nodesFor(nonterminal, _))
      case _ => None

  private def nodesFor(nonterminal: AnyNonterminal, children: Seq[CSTNode]): Seq[CSTNode] =
    nonterminal match
      case rule: Nonterminal => Seq(RuleNode(rule, children))
      case _: InternalNonterminal => children

  private def parseSequence(symbols: SymbolSeq)(using sync: Sync): Parsing[Seq[CSTNode]] =
    symbols match
      case Seq() => pure(Seq.empty)
      case symbol +: rest =>
        for
          node <- parseSymbol(symbol)(using sync union rest.starters)
          others <- parseSequence(rest)
        yield node ++ others

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
    val expected = symbol.starters
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
    private def terminal: TerminalOrEoi = next.map(_.terminal).getOrElse(Eoi)
