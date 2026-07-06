package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarAlternativesTest extends AnyFunSuite:

  // noinspection TypeAnnotation, ForwardReference
  object SimpleGrammar extends Grammar:
    val start = -> (a | b)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")
    val d = -> ("d")

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

  test("optional of terminal is processed as two alternatives"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    Alternatives.ofOptional(aAlt) shouldBe Set(
      Seq(SimpleGrammar.a),
      Seq()
    )

  test("optional is processed by adding an empty sequence"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    val bAlt = Alternatives.ofTerminal(SimpleGrammar.b)
    val alt = Alternatives.ofAlternation(aAlt, bAlt)
    Alternatives.ofOptional(alt) shouldBe Set(
      Seq(SimpleGrammar.a),
      Seq(SimpleGrammar.b),
      Seq()
    )

  test("zeroOrMore is processed as a single alternative"):
    val repetitionSymbol = InternalNonterminal()
    Alternatives.ofZeroOrMore(repetitionSymbol) shouldBe Set(
      Seq(repetitionSymbol)
    )

  test("oneOrMore of terminal is processed as a single alternative concatenating repetition"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    val repetitionSymbol = InternalNonterminal()
    Alternatives.ofOneOrMore(aAlt, repetitionSymbol) shouldBe Set(
      Seq(SimpleGrammar.a, repetitionSymbol)
    )

  test("oneOrMore is process by concatenating repetition to all alternatives"):
    val aAlt = Alternatives.ofTerminal(SimpleGrammar.a)
    val bAlt = Alternatives.ofTerminal(SimpleGrammar.b)
    val alt = Alternatives.ofAlternation(aAlt, bAlt)
    val repetitionSymbol = InternalNonterminal()
    Alternatives.ofOneOrMore(alt, repetitionSymbol) shouldBe Set(
      Seq(SimpleGrammar.a, repetitionSymbol),
      Seq(SimpleGrammar.b, repetitionSymbol)
    )
