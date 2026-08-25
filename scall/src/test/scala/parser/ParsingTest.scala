package it.unibo.scall
package parser

import grammar.Element.Terminal
import lexer.{Position, Token}
import parser.Parsing.*

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ParsingTest extends AnyFunSuite:

  val a: Terminal = Terminal("a", "a".r)
  val token: Token.Valid = Token.Valid(a, "a", Position(1, 1))
  val input: LazyList[Token] = LazyList(token, token)

  val anError: ParseError = ParseError.UnexpectedEndOfInput(Seq("a"))
  val anotherError: ParseError = ParseError.UnexpectedToken(Seq("a"), token)

  test("pure produces a value, consuming no input and reporting no error"):
    val step = pure(42).run(input)
    step.value shouldBe 42
    step.rest shouldBe input
    step.errors shouldBe Seq.empty

  test("peek returns the next token without consuming it"):
    val step = peek.run(input)
    step.value shouldBe Some(token)
    step.rest shouldBe input

  test("peek returns nothing at the end of the input"):
    val step = peek.run(LazyList.empty)
    step.value shouldBe None

  test("advance consumes exactly one token"):
    val step = advance.run(input)
    step.rest shouldBe LazyList(token)

  test("advance on empty input is harmless"):
    val step = advance.run(LazyList.empty)
    step.rest shouldBe LazyList.empty

  test("record reports an error, leaving the input untouched"):
    val step = record(anError).run(input)
    step.errors shouldBe Seq(anError)
    step.rest shouldBe input

  test("errors are collected in the order they are reported"):
    val step = (record(anError) andThen record(anotherError)).run(input)
    step.errors shouldBe Seq(anError, anotherError)

  test("map transforms the value without touching the input"):
    val step = pure(1).map(_ + 1).run(input)
    step.value shouldBe 2
    step.rest shouldBe input
    step.errors shouldBe Seq.empty

  test("flatMap transforms the value and keeps the effects of both steps"):
    val step = pure(1).flatMap(value => advance andThen pure(value + 1)).run(input)
    step.value shouldBe 2
    step.rest shouldBe LazyList(token)

  test("a for comprehension chains steps and gathers every error"):
    val parsing =
      for
        first <- pure(1)
        _ <- record(anError)
        second <- advance andThen pure(2)
        _ <- record(anotherError)
      yield first + second
    val step = parsing.run(input)
    step.value shouldBe 3
    step.rest shouldBe LazyList(token)
    step.errors shouldBe Seq(anError, anotherError)
