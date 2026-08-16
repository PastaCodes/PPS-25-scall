package it.unibo.scall
package parser

import ast.CSTNode
import ast.CSTNode.*
import grammar.Element.{Nonterminal, Terminal}
import grammar.ProcessedGrammar.{AnyNonterminal, AnySymbol, SymbolSeq}
import lexer.Token
import parser.ParsingTable.{Eof, ParsingTable}
import parser.Parser.ParseResult

enum ParseError:
  case UnexpectedToken(expected: Seq[String], found: Token.Valid)
  case UnexpectedEndOfInput(expected: Seq[String])
  case LexicalError(token: Token.Error)
  case TrailingInput(token: Token)

object Parser:
  type ParseResult[A] = Either[ParseError, (A, LazyList[Token])]

class Parser(table: ParsingTable, startSymbol: Nonterminal):
  import ParseError.*

  def parse(tokens: LazyList[Token]): Either[ParseError, CSTNode] =
    parseSymbol(startSymbol, tokens).flatMap: (cst, rest) =>
      rest.headOption match
        case Some(extra) => Left(TrailingInput(extra))
        case None => Right(cst)

  private def parseSymbol(symbol: AnySymbol, tokens: LazyList[Token]): ParseResult[CSTNode]=
    symbol match
      case terminal: Terminal => parseTerminal(terminal, tokens)
      case nonTerminal: AnyNonterminal => parseNonterminal(nonTerminal, tokens)

  private def parseTerminal(expected: Terminal, tokens: LazyList[Token]): ParseResult[CSTNode]=
    tokens.headOption match
      case Some(token @ Token.Valid(`expected`, _)) =>
        Right((LeafNode(token), tokens.tail))
      case Some(error @ Token.Error(_)) =>
        Left(LexicalError(error))
      case None =>
        Left(UnexpectedEndOfInput(Seq(expected.name)))

  private def parseNonterminal(nonterminal: AnyNonterminal, tokens: LazyList[Token]): ParseResult[CSTNode]=
    lookahead(tokens).flatMap: next =>
      table.get((nonterminal, next.map(_.terminal).getOrElse(Eof))) match
        case Some(production) => parseSequence(production, tokens).map: (children, rest) =>
          (RuleNode(nonterminal, children), rest)
        case None => next match
          case Some(token) => Left(UnexpectedToken(expectedFor(nonterminal), token))
          case None => Left(UnexpectedEndOfInput(expectedFor(nonterminal)))

  private def lookahead(tokens: LazyList[Token]): Either[ParseError, Option[Token.Valid]] =
    tokens.headOption match
      case Some(token: Token.Valid) => Right(Some(token))
      case Some(error: Token.Error) => Left(LexicalError(error))
      case None => Right(None)

  private def parseSequence(symbols: SymbolSeq, tokens: LazyList[Token]): ParseResult[Seq[CSTNode]] =
    symbols match
      case Seq() => Right((Seq.empty, tokens))
      case symbol :: rest =>
        for
          (node, afterSymbol) <- parseSymbol(symbol, tokens)
          (nodes, afterRest) <- parseSequence(rest, afterSymbol)
        yield (node +: nodes, afterRest)

  private def expectedFor(nonterminal: AnyNonterminal): Seq[String] =
    table.keys
      .collect { case (`nonterminal`, expected) => expected }
      .map {
        case t: Terminal => t.name
        case Eof => "end of input"
        }
      .toSeq.sorted
