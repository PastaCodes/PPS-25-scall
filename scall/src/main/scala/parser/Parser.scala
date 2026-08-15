package it.unibo.scall
package parser

import ast.CSTNode
import ast.CSTNode.*
import grammar.Element.Terminal
import grammar.ProcessedGrammar.{AnyNonterminal, AnySymbol, SymbolSeq}
import lexer.Token
import parser.ParsingTable.{Eof, ParsingTable, TerminalOrEof}

enum ParseError:
  case UnexpectedToken(expected: Seq[String], found: Token)
  case UnexpectedEndOfInput(expected: Seq[String])
  case LexicalError(token: Token)
  case TrailingInput(token: Token)

class Parser(table: ParsingTable, startSymbol: AnyNonterminal):
  import ParseError.*

  def parse(tokens: LazyList[Token]): Either[ParseError, CSTNode] =
    parseSymbol(startSymbol, tokens).flatMap: (cst, rest) =>
      rest.headOption match
        case Some(extra) => Left(TrailingInput(extra))
        case None => Right(cst)

  private def parseSymbol(symbol: AnySymbol, tokens: LazyList[Token]):
  Either[ParseError, (CSTNode, LazyList[Token])] =
    symbol match
      case terminal: Terminal => parseTerminal(terminal, tokens)
      case nonTerminal: AnyNonterminal => parseNonTerminal(nonTerminal, tokens)

  private def parseTerminal(expected: Terminal, tokens: LazyList[Token]):
  Either[ParseError, (CSTNode, LazyList[Token])] =
    tokens.headOption match
      case Some(token @ Token.Valid(terminal, _)) if terminal == expected =>
        Right((LeafNode(token), tokens.tail))
      case Some(error @ Token.Error(_)) =>
        Left(LexicalError(error))
      case Some(token) =>
        Left(UnexpectedToken(Seq(expected.name), token))
      case None =>
        Left(UnexpectedEndOfInput(Seq(expected.name)))

  private def parseNonTerminal(nonterminal: AnyNonterminal, tokens: LazyList[Token]):
  Either[ParseError, (CSTNode, LazyList[Token])] =
    lookahead(tokens).flatMap: la =>
      table.get((nonterminal, la)) match
        case Some(production) => parseSequence(production, tokens).map: (children, rest) =>
          (RuleNode(nonterminal, children), rest)
        case None => la match
          case Eof => Left(UnexpectedEndOfInput(expectedFor(nonterminal)))
          case _ => Left(UnexpectedToken(expectedFor(nonterminal), tokens.head))

  private def lookahead(tokens: LazyList[Token]): Either[ParseError, TerminalOrEof] =
    tokens.headOption match
      case Some(Token.Valid(terminal, _)) => Right(terminal)
      case Some(error @ Token.Error(_)) => Left(LexicalError(error))
      case None => Right(Eof)

  private def parseSequence(symbols: SymbolSeq, tokens: LazyList[Token]):
  Either[ParseError, (Seq[CSTNode], LazyList[Token])] =
    symbols.foldLeft[Either[ParseError, (Seq[CSTNode], LazyList[Token])]](Right((Seq.empty, tokens))):
      (acc, symbol) =>
        acc.flatMap: (children, rest) =>
          parseSymbol(symbol, rest).map: (node, remaining) =>
            (children :+ node, remaining)

  private def expectedFor(nonterminal: AnyNonterminal): Seq[String] =
    table.keys
      .collect { case (`nonterminal`, expected) => expected }
      .map {
        case t: Terminal => t.name
        case Eof => "end of input"
        }
      .toSeq.sorted
