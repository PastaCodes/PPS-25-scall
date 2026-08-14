package it.unibo.scall
package parser

import ast.CSTNode.*
import grammar.Element.{Eps, Nonterminal, Terminal}
import grammar.ProcessedGrammar.{AnyNonterminal, SymbolSeq}
import lexer.Token
import parser.ParsingTable.{Eof, ParsingTable, TerminalOrEof}

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ParserTest extends AnyFunSuite:

  val zero: Terminal = Terminal("zero", "0".r)
  val one: Terminal = Terminal("one", "1".r)
  val plus: Terminal = Terminal("plus", "\\+".r)
  val a: Terminal = Terminal("a", "a".r)
  val E: Nonterminal = Nonterminal("E", () => Eps)
  val X: Nonterminal = Nonterminal("X", () => Eps)
  val T: Nonterminal = Nonterminal("T", () => Eps)
  val S: Nonterminal = Nonterminal("S", () => Eps)

  def table(cells: ((AnyNonterminal, TerminalOrEof), SymbolSeq)*): ParsingTable =
    cells.toMap

  def token(t: Terminal): Token.Valid = Token.Valid(t, t.name)
  def tokens(ts: Terminal*): LazyList[Token] = LazyList.from(ts).map(token)

  test("parses a production with a single terminal"):
    val parser = Parser(table((S, a) -> Seq(a)), S)
    parser.parse(tokens(a)) shouldBe Right(RuleNode(S, Seq(LeafNode(token(a)))))

  test("parses the empty production without consuming input"):
    val parser = Parser(table((S, Eof) -> Seq.empty), S)
    parser.parse(tokens()) shouldBe Right(RuleNode(S, Seq.empty))

  // the table of E -> T X; X -> plus E | ε; T -> zero | one
  val arithmetic: ParsingTable = table(
    (E, zero) -> Seq(T, X),
    (E, one) -> Seq(T, X),
    (X, plus) -> Seq(plus, E),
    (X, Eof) -> Seq.empty,
    (T, zero) -> Seq(zero),
    (T, one) -> Seq(one),
    )

  test("parses a nested expression producing the full CST"):
    val parser = Parser(arithmetic, E)
    parser.parse(tokens(one, plus, zero)) shouldBe Right(
      RuleNode(E, Seq(
        RuleNode(T, Seq(LeafNode(token(one)))),
        RuleNode(X, Seq(
          LeafNode(token(plus)),
          RuleNode(E, Seq(
            RuleNode(T, Seq(LeafNode(token(zero)))),
            RuleNode(X, Seq.empty)
          )),
        )),
      ))
    )

  test("reports an unexpected token with the expected alternatives"):
    val parser = Parser(arithmetic, E)
    parser.parse(tokens(plus)) shouldBe Left(
      ParseError.UnexpectedToken(Seq("one", "zero"), token(plus))
    )

  test("reports an unexpected end of input"):
    val parser = Parser(arithmetic, E)
    parser.parse(tokens(one, plus)) shouldBe Left(
      ParseError.UnexpectedEndOfInput(Seq("one", "zero"))
    )

  test("reports input left over after a complete parse"):
    val parser = Parser(table((S, a) -> Seq(a)), S)
    parser.parse(tokens(a, a)) shouldBe Left(ParseError.TrailingInput(token(a)))

  test("reports lexical errors coming from the lexer"):
    val parser = Parser(arithmetic, E)
    val bad = Token.Error("$")
    parser.parse(LazyList(bad)) shouldBe Left(ParseError.LexicalError(bad))