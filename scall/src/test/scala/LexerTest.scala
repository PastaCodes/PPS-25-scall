package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import Element.*

class LexerTest extends AnyFunSuite:
  val emptyLexer = Lexer(Seq.empty)
  val ifRule: Terminal = TextTerminal("if")
  val ifLexer = Lexer(Seq(ifRule))
  val idRule: Terminal = RegexTerminal("[a-z]+".r)
  val idLexer = Lexer(Seq(idRule))

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
    tokens.map(_.terminal) shouldBe List(idRule)

  test("Lexer should use declaration order as the tie-breaker mechanism"):
    val lexer1 = Lexer(Seq(ifRule, idRule))
    val tokens1 = lexer1.tokenize("if")
    tokens1.map(_.terminal) shouldBe List(ifRule)

    val lexer2 = Lexer(Seq(idRule, ifRule))
    val tokens2 = lexer2.tokenize("if")
    tokens2.map(_.terminal) shouldBe List(idRule)

    val specificRegex: Terminal = RegexTerminal("ab".r)
    val generalRegex: Terminal = RegexTerminal("[a-z]+".r)
    val lexer = Lexer(Seq(specificRegex, generalRegex))

    val tokens = lexer.tokenize("ab")
    tokens.map(_.lexeme) shouldBe List("ab")
    tokens.map(_.terminal) shouldBe List(specificRegex)

