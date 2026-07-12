package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import Element.*

class LexerLongestPrefixMatchTest extends AnyFunSuite:
  val emptyLexer = Lexer(Seq.empty)
  val ifRule: Terminal = TextTerminal("if")
  val ifLexer = Lexer(Seq(ifRule))
  val idRule: Terminal = RegexTerminal("[a-z]+".r)
  val idLexer = Lexer(Seq(idRule))
  val numRule: Terminal = RegexTerminal("[0-9]+".r)

  test("Lexer should return empty seq for empty string"):
    emptyLexer.tokenize("") shouldBe empty

  test("Lexer recognize TextTerminal"):
    val tokens = ifLexer.tokenize("if")
    tokens.map(_.lexeme) shouldBe List("if")

  test("Lexer recognize RegexTerminal"):
    val tokens = idLexer.tokenize("test")
    tokens.map(_.lexeme) shouldBe List("test")

  test("Lexer implements longest-prefix-match"):
    val terminals = Seq(ifRule, idRule)
    val lexer = Lexer(terminals)
    val tokens = lexer.tokenize("iffy")
    tokens.map(_.lexeme) shouldBe List("iffy")
    tokens.map(_.terminalOpt.get) shouldBe List(idRule)

  test("Lexer should use declaration order as the tie-breaker mechanism"):
    val lexer1 = Lexer(Seq(ifRule, idRule))
    val tokens1 = lexer1.tokenize("if")
    tokens1.map(_.terminalOpt.get) shouldBe List(ifRule)

    val lexer2 = Lexer(Seq(idRule, ifRule))
    val tokens2 = lexer2.tokenize("if")
    tokens2.map(_.terminalOpt.get) shouldBe List(idRule)

    val specificRegex: Terminal = RegexTerminal("ab".r)
    val generalRegex: Terminal = RegexTerminal("[a-z]+".r)
    val lexer = Lexer(Seq(specificRegex, generalRegex))

    val tokens = lexer.tokenize("ab")
    tokens.map(_.lexeme) shouldBe List("ab")
    tokens.map(_.terminalOpt.get) shouldBe List(specificRegex)

  test("Lexer emits Token.Error for unrecognized characters and keeps scanning"):
    val lexer = Lexer(Seq(numRule, idRule))
    val tokens = lexer.tokenize("123$#abc")

    tokens shouldBe List(
      Token.Valid(numRule, "123"),
      Token.Error("$"),
      Token.Error("#"),
      Token.Valid(idRule, "abc")
    )
