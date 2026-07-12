package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import Element.*

class LexerTest extends AnyFunSuite:
  val terminals: Seq[Terminal] = Seq(TextTerminal("if"))

  test("Lexer should return empty seq for empty string"):
    val lexer = Lexer(Seq.empty)
    lexer.tokenize("") shouldBe empty

  test("Lexer recognize TextTerminal"):
    val ifRule: Terminal = TextTerminal("if")
    val input = "if"

    val lexer = Lexer(Seq(ifRule))
    val tokens = lexer.tokenize(input)
    tokens.map(_.lexeme) shouldBe List("if")

  test("Lexer recognize RegexTerminal"):
    val idPattern: Terminal = RegexTerminal("[a-z]+".r)
    val input = "test"

    val lexer = Lexer(Seq(idPattern))
    val tokens = lexer.tokenize(input)
    tokens.map(_.lexeme) shouldBe List("test")

  test("Lexer implements longest-prefix-match"):
    val ifRule: Terminal = TextTerminal("if")
    val idPattern: Terminal = RegexTerminal("[a-z]+".r)
    val input = "if"

    val lexer = Lexer(Seq(ifRule, idPattern))
    val tokens = lexer.tokenize("iffy")
    tokens.map(_.lexeme) shouldBe List("iffy")
    tokens.map(_.terminal) shouldBe List(idPattern)

  test("Lexer should use declaration order as the tie-breaker mechanism"):
    val ifRule: Terminal = TextTerminal("if")
    val idTerm: Terminal = RegexTerminal("[a-z]+".r)
    val input = "if"

    val lexer1 = Lexer(Seq(ifRule, idTerm))
    val tokens1 = lexer1.tokenize(input)
    tokens1.map(_.terminal) shouldBe List(ifRule)

    val lexer2 = Lexer(Seq(idTerm, ifRule))
    val tokens2 = lexer2.tokenize(input)
    tokens2.map(_.terminal) shouldBe List(idTerm)

    val specificRegex: Terminal = RegexTerminal("ab".r)
    val generalRegex: Terminal = RegexTerminal("[a-z]+".r)
    val lexer = Lexer(Seq(specificRegex, generalRegex))

    val tokens = lexer.tokenize("ab")
    tokens.map(_.lexeme) shouldBe List("ab")
    tokens.map(_.terminal) shouldBe List(specificRegex)

