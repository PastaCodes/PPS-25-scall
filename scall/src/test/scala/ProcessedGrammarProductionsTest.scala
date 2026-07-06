package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarProductionsTest extends AnyFunSuite:

  // noinspection TypeAnnotation, ForwardReference
  object SimpleGrammar extends Grammar:
    val start = -> (a | b)
    val a = -> ("a")
    val b = -> ("b")

  /* Given a production S 🡒 α X β
   * Where X 🡒 a | b
   * The additional productions should be:
   * X 🡒 a
   * X 🡒 b
   */
  test("nonterminal is processed as productions for all rule body alternatives"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    val alt = Alternatives.ofAlternation(aAlt, bAlt)
    Productions.ofNonTerminal(SimpleGrammar.start, alt) shouldBe Map(
      SimpleGrammar.start -> Set(
        Seq(SimpleGrammar.a),
        Seq(SimpleGrammar.b)
      )
    )

  /* Given a production S 🡒 α X* β
   * Where X 🡒 a | b
   * The additional productions should be:
   * R 🡒 a R
   * R 🡒 b R
   * R 🡒 ε
   * Where R is used in (see alternatives test):
   * S 🡒 α R β
   */
  test("zeroOrMore is processed as productions concatenating repetition and adding empty production"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    val alt = Alternatives.ofAlternation(aAlt, bAlt)
    val repetitionSymbol = InternalNonterminal()
    Productions.ofOrMore(alt, repetitionSymbol) shouldBe Map(
      repetitionSymbol -> Set(
        Seq(SimpleGrammar.a, repetitionSymbol),
        Seq(SimpleGrammar.b, repetitionSymbol),
        Seq.empty
      )
    )

  /* Given a production S 🡒 α X+ β
   * Where X 🡒 a | b
   * The additional productions should be:
   * R 🡒 a R
   * R 🡒 b R
   * R 🡒 ε
   * Where R is used in (see alternatives test):
   * S 🡒 α a R β
   * S 🡒 α b R β
   */
  test("oneOrMore is processed as productions concatenating repetition and adding empty production"):
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.a)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.b)
    val alt = Alternatives.ofAlternation(aAlt, bAlt)
    val repetitionSymbol = InternalNonterminal()
    Productions.ofOrMore(alt, repetitionSymbol) shouldBe Map(
      repetitionSymbol -> Set(
        Seq(SimpleGrammar.a, repetitionSymbol),
        Seq(SimpleGrammar.b, repetitionSymbol),
        Seq.empty
      )
    )
