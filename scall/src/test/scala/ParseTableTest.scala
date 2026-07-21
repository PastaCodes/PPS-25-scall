package it.unibo.scall

import Element.Eps
import ParseTable.Eof

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ParseTableTest extends AnyFunSuite:

  test("should load theory from file"):
    noException should be thrownBy
      util.Scala2P.engineWithTheoryFile(
        getClass.getResourceAsStream("/prolog/parse_table.pl")
      )

  // noinspection ForwardReference, TypeAnnotation
  object TestGrammar extends Grammar:
    val x = -> (y ++ a ++ b ++ c | b ++ c ++ y ++ z)
    val y = -> (c ++ d | z)
    val z = -> (Eps)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")
    val d = -> ("d")

  import TestGrammar.*

  test("should compute parse table"):
    val g = ProcessedGrammar.of(TestGrammar, TestGrammar.x)
    ParseTable.compute(g) shouldBe Map(
      (x, a) -> Seq(y, a, b, c),
      (x, b) -> Seq(b, c, y, z),
      (x, c) -> Seq(y, a, b, c),
      (y, a) -> Seq(z),
      (y, c) -> Seq(c, d),
      (y, Eof) -> Seq(z),
      (z, a) -> Seq.empty,
      (z, Eof) -> Seq.empty,
    )
