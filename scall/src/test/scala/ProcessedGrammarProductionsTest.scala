package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarProductionsTest extends AnyFunSuite:

  // noinspection TypeAnnotation, ForwardReference
  object SimpleGrammar extends Grammar:
    val start = -> (a | b)
    val a = -> ("a")
    val b = -> ("b")

  test("nonterminal is processed as productions for all rule body alternatives"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    val bAlt = Alternatives.ofTerminal(SimpleGrammar.b)
    val alt = Alternatives.ofAlternation(aAlt, bAlt)
    Productions.ofNonTerminal(SimpleGrammar.start, alt) shouldBe Map(
      SimpleGrammar.start -> Set(
        Seq(SimpleGrammar.a),
        Seq(SimpleGrammar.b)
      )
    )
