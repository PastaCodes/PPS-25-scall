package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarTest extends AnyFunSuite:

  // noinspection TypeAnnotation, ForwardReference
  object SimpleGrammar extends Grammar:
    val start = -> (alternationSimple)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")
    val d = -> ("d")
    val alternationSimple = a | b
    val alternationDeep = (a | b) | (b | c)
    val concatSimple = a ++ b
    val concatDeep = (a | b) ++ (b | c)
    val optional = a.?
    val zeroOrMore = a.*
    val oneOrMore = a.+

  test("terminal is processed as a single alternative"):
    Alternatives.ofTerminal(SimpleGrammar.a) shouldBe Set(Seq(SimpleGrammar.a))

  test("nonterminal is processed as a single alternative"):
    Alternatives.ofNonterminal(SimpleGrammar.start) shouldBe Set(Seq(SimpleGrammar.start))

  test("alternation of terminals is processed as two alternatives"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    val bAlt = Alternatives.ofTerminal(SimpleGrammar.b)
    Alternatives.ofAlternation(aAlt, bAlt) shouldBe Set(
      Seq(SimpleGrammar.a), Seq(SimpleGrammar.b)
    )

  test("alternation is processed as the union of alternatives"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    val bAlt = Alternatives.ofTerminal(SimpleGrammar.b)
    val cAlt = Alternatives.ofTerminal(SimpleGrammar.c)
    val dAlt = Alternatives.ofTerminal(SimpleGrammar.d)
    val alt1 = Alternatives.ofAlternation(aAlt, bAlt)
    val alt2 = Alternatives.ofAlternation(cAlt, dAlt)
    Alternatives.ofAlternation(alt1, alt2) shouldBe Set(
      Seq(SimpleGrammar.a), Seq(SimpleGrammar.b), Seq(SimpleGrammar.c), Seq(SimpleGrammar.d)
    )

  test("concatenation of terminals is processed as a single alternative"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    val bAlt = Alternatives.ofTerminal(SimpleGrammar.b)
    Alternatives.ofConcat(aAlt, bAlt) shouldBe Set(
      Seq(SimpleGrammar.a, SimpleGrammar.b)
    )

  test("concatenation is processed as the product of concatenated alternatives"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    val bAlt = Alternatives.ofTerminal(SimpleGrammar.b)
    val cAlt = Alternatives.ofTerminal(SimpleGrammar.c)
    val dAlt = Alternatives.ofTerminal(SimpleGrammar.d)
    val alt1 = Alternatives.ofAlternation(aAlt, bAlt)
    val alt2 = Alternatives.ofAlternation(cAlt, dAlt)
    Alternatives.ofConcat(alt1, alt2) shouldBe Set(
      Seq(SimpleGrammar.a, SimpleGrammar.c),
      Seq(SimpleGrammar.a, SimpleGrammar.d),
      Seq(SimpleGrammar.b, SimpleGrammar.c),
      Seq(SimpleGrammar.b, SimpleGrammar.d)
    )
