package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarTest extends AnyFunSuite:

  // noinspection TypeAnnotation
  object EmptyGrammar extends Grammar:
    val start = -> ("")

  test("terminal is processed as a single alternative"):
    Alternatives.ofTerminal(EmptyGrammar.start) shouldBe Set(Seq(EmptyGrammar.start))
