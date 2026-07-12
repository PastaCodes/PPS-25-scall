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
