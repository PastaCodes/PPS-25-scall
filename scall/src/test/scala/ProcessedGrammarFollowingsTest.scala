package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarFollowingsTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val X = -> (A ++ B)
    val Y = -> (X ++ C)
    val A = -> (a)
    val B = -> (b)
    val C = -> (c)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")

  /* Given the production X 🡒 A B
   * The followings should be:
   * A: B       (head = X)
   * B: ε       (head = X)
   */
  test("nonterminal rule is processed by turning partial followings into followings"):
    val aPartials = PartialFollowings.ofNonterminal(SimpleGrammar.A)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.B)
    val bPartials = PartialFollowings.ofNonterminal(SimpleGrammar.B)
    val xPartials = PartialFollowings.ofConcat(aPartials, bPartials, bAlt)
    Followings.ofNonterminal(SimpleGrammar.X, xPartials, Map.empty) shouldBe Map(
      SimpleGrammar.A -> Set(Following(SimpleGrammar.X, Seq(SimpleGrammar.B))),
      SimpleGrammar.B -> Set(Following(SimpleGrammar.X, Seq.empty))
    )

  /* Given the production Y 🡒 X C
   * Where X 🡒 A B
   * The followings should be:
   * A: B       (head = X)
   * B: ε       (head = X)
   * X: C       (head = Y)
   * C: ε       (head = Y)
   */
  test("nonterminal rule is processed by preserving inner followings"):
    val aPartials = PartialFollowings.ofNonterminal(SimpleGrammar.A)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.B)
    val bPartials = PartialFollowings.ofNonterminal(SimpleGrammar.B)
    val xRulePartials = PartialFollowings.ofConcat(aPartials, bPartials, bAlt)
    val xFollow = Followings.ofNonterminal(SimpleGrammar.X, xRulePartials, Map.empty)
    val xPartials = PartialFollowings.ofNonterminal(SimpleGrammar.X)
    val cAlt = Alternatives.ofSymbol(SimpleGrammar.C)
    val cPartials = PartialFollowings.ofNonterminal(SimpleGrammar.C)
    val yPartials = PartialFollowings.ofConcat(xPartials, cPartials, cAlt)
    Followings.ofNonterminal(SimpleGrammar.Y, yPartials, xFollow) shouldBe Map(
      SimpleGrammar.A -> Set(Following(SimpleGrammar.X, Seq(SimpleGrammar.B))),
      SimpleGrammar.B -> Set(Following(SimpleGrammar.X, Seq.empty)),
      SimpleGrammar.X -> Set(Following(SimpleGrammar.Y, Seq(SimpleGrammar.C))),
      SimpleGrammar.C -> Set(Following(SimpleGrammar.Y, Seq.empty))
    )

  /* Given the sequence (A B)*
   * The followings should be:
   * A: B R     (head = R)
   * B: R       (head = R)
   * R: ε       (head = R)
   * Where R is (see productions test):
   * R 🡒 A B R
   * R 🡒 ε
   */
  test("zeroOrMore is processed by updating followings for repetition"):
    val aPartials = PartialFollowings.ofNonterminal(SimpleGrammar.A)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.B)
    val bPartials = PartialFollowings.ofNonterminal(SimpleGrammar.B)
    val abPartials = PartialFollowings.ofConcat(aPartials, bPartials, bAlt)
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.A)
    val abAlt = Alternatives.ofConcat(aAlt, bAlt)
    val repetitionSymbol = InternalNonterminal()
    Followings.ofOrMore(abPartials, repetitionSymbol, abAlt) shouldBe Map(
      SimpleGrammar.A -> Set(Following(repetitionSymbol, Seq(SimpleGrammar.B, repetitionSymbol))),
      SimpleGrammar.B -> Set(Following(repetitionSymbol, Seq(repetitionSymbol))),
      repetitionSymbol -> Set(Following(repetitionSymbol, Seq.empty))
    )
