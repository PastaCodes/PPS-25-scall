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
  val vaL: Terminal = Terminal("val", "val".r)
  val id: Terminal = Terminal("id", "[a-z]+".r)
  val assign: Terminal = Terminal("assign", "=".r)
  val num: Terminal = Terminal("num", "[0-9]+".r)
  val semi: Terminal = Terminal("semi", ";".r)
  val P: Nonterminal = Nonterminal("P", () => Eps)
  val Prest: Nonterminal = Nonterminal("Prest", () => Eps)

  def table(cells: ((AnyNonterminal, TerminalOrEof), SymbolSeq)*): ParsingTable =
    cells.toMap

  def token(t: Terminal): Token.Valid = Token.Valid(t, t.name)

  def tokens(ts: Terminal*): LazyList[Token] = LazyList.from(ts).map(token)

  // the table of E -> T X; X -> plus E | ε; T -> zero | one
  val arithmetic: ParsingTable = table(
    (E, zero) -> Seq(T, X),
    (E, one) -> Seq(T, X),
    (X, plus) -> Seq(plus, E),
    (X, Eof) -> Seq.empty,
    (T, zero) -> Seq(zero),
    (T, one) -> Seq(one),
  )

  // the table of P -> S Prest; Prest -> S Prest | eps; S -> val id assign num semi
  val program: ParsingTable = table(
    (P, vaL) -> Seq(S, Prest),
    (Prest, vaL) -> Seq(S, Prest),
    (Prest, Eof) -> Seq.empty,
    (S, vaL) -> Seq(vaL, id, assign, num, semi),
  )

  test("parses a production with a single terminal"):
    val parser = Parser(table((S, a) -> Seq(a)), S)
    parser.parse(tokens(a)) shouldBe Right(RuleNode(S, Seq(LeafNode(token(a)))))

  test("parses the empty production without consuming input"):
    val parser = Parser(table((S, Eof) -> Seq.empty), S)
    parser.parse(tokens()) shouldBe Right(RuleNode(S, Seq.empty))

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
    val bad: Token.Error = Token.Error("$")
    parser.parse(LazyList(bad)) shouldBe Left(ParseError.LexicalError(bad))

  // --- error recovery ---

  test("skips the offending token and parses the rest of the expression"):
    val report = Parser(arithmetic, E).parseAll(tokens(one, plus, plus, zero))
    report.errors shouldBe Seq(ParseError.UnexpectedToken(Seq("one", "zero"), token(plus)))
    report.tree shouldBe RuleNode(E, Seq(
      RuleNode(T, Seq(LeafNode(token(one)))),
      RuleNode(X, Seq(
        LeafNode(token(plus)),
        RuleNode(E, Seq(
          RuleNode(T, Seq(LeafNode(token(zero)))),
          RuleNode(X, Seq.empty),
        )),
      )),
    ))

  test("reports both errors of a doubly broken expression"):
    val report = Parser(arithmetic, E).parseAll(tokens(one, plus, plus, zero, plus, plus))
    report.errors shouldBe Seq(
      ParseError.UnexpectedToken(Seq("one", "zero"), token(plus)),
      ParseError.UnexpectedToken(Seq("one", "zero"), token(plus)),
    )

  test("reports every lexical error instead of stopping at the first"):
    val bad: Token.Error = Token.Error("$")
    val worse: Token.Error = Token.Error("#")
    val input = LazyList(bad, token(one), worse, token(plus), token(zero))
    val report = Parser(arithmetic, E).parseAll(input)
    report.errors shouldBe Seq(ParseError.LexicalError(bad), ParseError.LexicalError(worse))
    report.isValid shouldBe false

  test("reports one error per broken statement"):
    // val id = ;   val id 5 ;
    val input = tokens(vaL, id, assign, semi, vaL, id, num, semi)
    val report = Parser(program, P).parseAll(input)
    report.errors shouldBe Seq(
      ParseError.UnexpectedToken(Seq("num"), token(semi)),
      ParseError.UnexpectedToken(Seq("assign"), token(num)),
    )

  test("leaves an error node where a symbol was missing"):
    val report = Parser(program, P).parseAll(tokens(vaL, id, assign, semi))
    report.tree shouldBe RuleNode(P, Seq(
      RuleNode(S, Seq(
        LeafNode(token(vaL)),
        LeafNode(token(id)),
        LeafNode(token(assign)),
        ErrorNode(num, Seq.empty),
        LeafNode(token(semi)),
      )),
      RuleNode(Prest, Seq.empty),
    ))

  test("drops the tokens no rule was waiting for"):
    // val id = 5 5 ;  the extra number is discarded, the statement is complete
    val report = Parser(program, P).parseAll(tokens(vaL, id, assign, num, num, semi))
    report.errors shouldBe Seq(ParseError.UnexpectedToken(Seq("semi"), token(num)))
    report.tree shouldBe RuleNode(P, Seq(
      RuleNode(S, Seq(
        LeafNode(token(vaL)),
        LeafNode(token(id)),
        LeafNode(token(assign)),
        LeafNode(token(num)),
        LeafNode(token(semi)),
      )),
      RuleNode(Prest, Seq.empty),
    ))

  test("gives up on garbage input without looping"):
    val report = Parser(program, P).parseAll(tokens(num, num, num))
    report.errors shouldBe Seq(ParseError.UnexpectedToken(Seq("val"), token(num)))
    report.tree shouldBe ErrorNode(P, Seq(token(num), token(num), token(num)))

  test("a wrong terminal is an error, not a crash"):
    val report = Parser(table((S, vaL) -> Seq(vaL, id)), S).parseAll(tokens(vaL, num))
    report.errors shouldBe Seq(ParseError.UnexpectedToken(Seq("id"), token(num)))

  test("parse keeps reporting only the first error"):
    val parser = Parser(arithmetic, E)
    parser.parse(tokens(one, plus, plus, zero)) shouldBe
      Left(ParseError.UnexpectedToken(Seq("one", "zero"), token(plus)))

  test("a correct input produces no error at all"):
    val report = Parser(arithmetic, E).parseAll(tokens(one, plus, zero))
    report.isValid shouldBe true
