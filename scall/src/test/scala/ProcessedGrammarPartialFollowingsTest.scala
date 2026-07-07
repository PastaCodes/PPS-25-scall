package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarPartialFollowingsTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val start = -> (a | b)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")
    val d = -> ("d")

  test("nonterminal is processed as a single empty partial following"):
    PartialFollowings.ofNonterminal(SimpleGrammar.start) shouldBe Map(SimpleGrammar.start -> Set(Seq.empty))
