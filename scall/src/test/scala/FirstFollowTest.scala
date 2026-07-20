package it.unibo.scall

import Element.Eps
import util.engineWithTheoryFile

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class FirstFollowTest extends AnyFunSuite:

  test("should load theory from file"):
    noException should be thrownBy
      engineWithTheoryFile(getClass.getResourceAsStream("/prolog/first_follow.pl"))

  // noinspection ForwardReference, TypeAnnotation
  object TestGrammar extends Grammar:
    val x = -> (y ++ a ++ b ++ c | a ++ c ++ y ++ z)
    val y = -> (c ++ d | z)
    val z = -> (Eps)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")
    val d = -> ("d")

  import TestGrammar.*

  test("should compute FIRST sets"):
    val g = ProcessedGrammar.of(TestGrammar, TestGrammar.x)
    FirstFollow.compute(g).firstSets shouldBe Map(
      x -> Set(a, c),
      y -> Set(c, Eps),
      z -> Set(Eps)
    )

  test("should compute FOLLOW sets"):
    val g = ProcessedGrammar.of(TestGrammar, TestGrammar.x)
    FirstFollow.compute(g).followSets shouldBe Map(
      x -> Set(Eof),
      y -> Set(a, Eof),
      z -> Set(a, Eof)
    )
