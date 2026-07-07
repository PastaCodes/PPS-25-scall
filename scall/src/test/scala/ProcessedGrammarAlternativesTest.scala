package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarAlternativesTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val start = -> (a | b)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")
    val d = -> ("d")

  test("terminal is processed as a single alternative"):
    Alternatives.ofSymbol(SimpleGrammar.a) shouldBe Set(Seq(SimpleGrammar.a))

  test("nonterminal is processed as a single alternative"):
    Alternatives.ofSymbol(SimpleGrammar.start) shouldBe Set(Seq(SimpleGrammar.start))

  test("alternation of terminals is processed as two alternatives"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    Alternatives.ofAlternation(aAlt, bAlt) shouldBe Set(
      Seq(SimpleGrammar.a), Seq(SimpleGrammar.b)
    )

  /* Given a production S 🡒 α (X | Y) β
   * Where X 🡒 a | b and Y 🡒 c | d
   * The result should be:
   * S 🡒 α a β
   * S 🡒 α b β
   * S 🡒 α c β
   * S 🡒 α d β
   */
  test("alternation is processed as the union of alternatives"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    val cAlt = Alternatives.ofSymbol(SimpleGrammar.c)
    val dAlt = Alternatives.ofSymbol(SimpleGrammar.d)
    val alt1 = Alternatives.ofAlternation(aAlt, bAlt)
    val alt2 = Alternatives.ofAlternation(cAlt, dAlt)
    Alternatives.ofAlternation(alt1, alt2) shouldBe Set(
      Seq(SimpleGrammar.a), Seq(SimpleGrammar.b), Seq(SimpleGrammar.c), Seq(SimpleGrammar.d)
    )

  test("concatenation of terminals is processed as a single alternative"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    Alternatives.ofConcat(aAlt, bAlt) shouldBe Set(
      Seq(SimpleGrammar.a, SimpleGrammar.b)
    )

  /* Given a production S 🡒 α (X Y) β
   * Where X 🡒 a | b and Y 🡒 c | d
   * The result should be:
   * S 🡒 α a c β
   * S 🡒 α a d β
   * S 🡒 α b c β
   * S 🡒 α b d β
   */
  test("concatenation is processed as the product of concatenated alternatives"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    val cAlt = Alternatives.ofSymbol(SimpleGrammar.c)
    val dAlt = Alternatives.ofSymbol(SimpleGrammar.d)
    val alt1 = Alternatives.ofAlternation(aAlt, bAlt)
    val alt2 = Alternatives.ofAlternation(cAlt, dAlt)
    Alternatives.ofConcat(alt1, alt2) shouldBe Set(
      Seq(SimpleGrammar.a, SimpleGrammar.c),
      Seq(SimpleGrammar.a, SimpleGrammar.d),
      Seq(SimpleGrammar.b, SimpleGrammar.c),
      Seq(SimpleGrammar.b, SimpleGrammar.d)
    )

  test("optional of terminal is processed as two alternatives"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    Alternatives.ofOptional(aAlt) shouldBe Set(
      Seq(SimpleGrammar.a),
      Seq()
    )

  /* Given a production S 🡒 α X? β
   * Where X 🡒 a | b
   * The result should be:
   * S 🡒 α a β
   * S 🡒 α b β
   * S 🡒 α β
   */
  test("optional is processed by adding an empty sequence to alternatives"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    val alt = Alternatives.ofAlternation(aAlt, bAlt)
    Alternatives.ofOptional(alt) shouldBe Set(
      Seq(SimpleGrammar.a),
      Seq(SimpleGrammar.b),
      Seq()
    )

  /* Given a production S 🡒 α X* β
   * Where X 🡒 a | b
   * The result should be:
   * S 🡒 α R β
   * Where R is (see productions test):
   * R 🡒 a R
   * R 🡒 b R
   * R 🡒 ε
   */
  test("zeroOrMore is processed as a single alternative"):
    val repetitionSymbol = InternalNonterminal()
    Alternatives.ofZeroOrMore(repetitionSymbol) shouldBe Set(
      Seq(repetitionSymbol)
    )

  test("oneOrMore of terminal is processed as a single alternative concatenating repetition"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val repetitionSymbol = InternalNonterminal()
    Alternatives.ofOneOrMore(aAlt, repetitionSymbol) shouldBe Set(
      Seq(SimpleGrammar.a, repetitionSymbol)
    )

  /* Given a production S 🡒 α X* β
   * Where X 🡒 a | b
   * The result should be:
   * S 🡒 α a R β
   * S 🡒 α b R β
   * Where R is (see productions test):
   * R 🡒 a R
   * R 🡒 b R
   * R 🡒 ε
   */
  test("oneOrMore is process by concatenating repetition to all alternatives"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    val alt = Alternatives.ofAlternation(aAlt, bAlt)
    val repetitionSymbol = InternalNonterminal()
    Alternatives.ofOneOrMore(alt, repetitionSymbol) shouldBe Set(
      Seq(SimpleGrammar.a, repetitionSymbol),
      Seq(SimpleGrammar.b, repetitionSymbol)
    )
