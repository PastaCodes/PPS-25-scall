package it.unibo.scall
package parser

import grammar.Element.{Eps, Eoi}
import grammar.{Grammar, ProcessedGrammar}
import parser.ParsingTable

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ParsingTableTest extends AnyFunSuite:

  test("should load theory from file"):
    noException should be thrownBy
      util.Scala2P.engineWithTheoryFile(
        getClass.getResourceAsStream("/prolog/parsing_table.pl")
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

  test("should compute parsing table"):
    val g = ProcessedGrammar.of(TestGrammar, TestGrammar.x)
    ParsingTable.compute(g) shouldBe Map(
      (x, a) -> Seq(y, a, b, c),
      (x, b) -> Seq(b, c, y, z),
      (x, c) -> Seq(y, a, b, c),
      (y, a) -> Seq(z),
      (y, c) -> Seq(c, d),
      (y, Eoi) -> Seq(z),
      (z, a) -> Seq.empty,
      (z, Eoi) -> Seq.empty,
    )
