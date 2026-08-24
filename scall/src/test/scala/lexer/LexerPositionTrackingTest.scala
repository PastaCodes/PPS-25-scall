package it.unibo.scall
package lexer

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import grammar.{Element, Grammar}

class LexerPositionTrackingTest extends AnyFunSuite:

  object PosGrammar extends Grammar:
    val id: Element.Terminal = -> ("[a-z]+".r)
    val num: Element.Terminal = -> ("[0-9]+".r)
    val ws: Element.Terminal = -> ("""\s+""".r, skip = true)

  val lexer = Lexer(PosGrammar.terminals)

  test("Lexer advances column correctly on a single line ignoring skipped tokens"):
    val tokens = lexer.tokenize("abc 123 def").toList

    // "abc" at 1:1, space skipped (len 1), "123" at 1:5, space skipped (len 1), "def" at 1:9
    tokens.collect { case v: Token.Valid => v.position } shouldBe List(
      Position(1, 1),
      Position(1, 5),
      Position(1, 9)
    )

  test("Lexer increments line and resets column upon encountering newlines"):
    val input = "abc\n123\ndef"
    val tokens = lexer.tokenize(input).toList

    // "abc" at 1:1, "\n" skipped, "123" at 2:1, "\n" skipped, "def" at 3:1
    tokens.collect { case v: Token.Valid => v.position } shouldBe List(
      Position(1, 1),
      Position(2, 1),
      Position(3, 1)
    )

  test("Lexer correctly calculates positions spanning multiple newlines and carriage returns"):
    val input = "a\n\nb\r\nc"
    val tokens = lexer.tokenize(input).toList

    // "a" at 1:1
    // "\n\n" skipped -> advanced 2 lines, "b" at 3:1
    // "\r\n" skipped -> advanced 1 line,  "c" at 4:1
    tokens.collect { case v: Token.Valid => v.position } shouldBe List(
      Position(1, 1),
      Position(3, 1),
      Position(4, 1)
    )

  test("Lexer binds the exact location coordinates to Token.Error instances"):
    val input = "abc $ def\n #"
    val tokens = lexer.tokenize(input).toList
    val errors = tokens.collect { case e: Token.Error => e }

    errors.map(_.value) shouldBe List("$", "#")
    // "$" is at col 5 after "abc "
    // "#" is at col 2 on the next line after space.
    errors.map(_.position) shouldBe List(
      Position(1, 5),
      Position(2, 2)
    )