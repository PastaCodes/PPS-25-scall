package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarTest extends AnyFunSuite:

  // noinspection TypeAnnotation
  object EmptyGrammar extends Grammar:
    val start = -> ("")

  test("terminal is processed as a single alternative"):
    Alternatives.ofTerminal(EmptyGrammar.start) shouldBe Set(Seq(EmptyGrammar.start))

  // noinspection TypeAnnotation, ForwardReference
  object SimpleGrammar extends Grammar:
    val start = -> (alternation)
    val a = -> ("a")
    val b = -> ("b")
    val concat = a ++ b
    val alternation = a | b
    val optional = a.?
    val zeroOrMore = a.*
    val oneOrMore = a.+

  test("nonterminal is processed as a single alternative"):
    Alternatives.ofNonterminal(SimpleGrammar.start) shouldBe Set(Seq(SimpleGrammar.start))
