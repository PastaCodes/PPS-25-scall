package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarPartialFollowingsTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val A = -> (a)
    val B = -> (b)
    val C = -> (c)
    val D = -> (d)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")
    val d = -> ("d")

  test("nonterminal is processed as a single empty partial following"):
    PartialFollowings.ofNonterminal(SimpleGrammar.A) shouldBe Map(SimpleGrammar.A -> Set(Seq.empty))

  /* Given the sequence A B C D
   * The partial followings should be:
   * A: B C D
   * B: C D
   * C: D
   * D: ε
   */
  test("concatenation is processed as incremental partial followings"):
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.B)
    val aFollow = PartialFollowings.ofNonterminal(SimpleGrammar.A)
    val bFollow = PartialFollowings.ofNonterminal(SimpleGrammar.B)
    val abFollow = PartialFollowings.ofConcat(aFollow, bFollow, bAlt)
    val cAlt = Alternatives.ofSymbol(SimpleGrammar.C)
    val cFollow = PartialFollowings.ofNonterminal(SimpleGrammar.C)
    val abcFollow = PartialFollowings.ofConcat(abFollow, cFollow, cAlt)
    val dAlt = Alternatives.ofSymbol(SimpleGrammar.D)
    val dFollow = PartialFollowings.ofNonterminal(SimpleGrammar.D)
    PartialFollowings.ofConcat(abcFollow, dFollow, dAlt) shouldBe Map(
      SimpleGrammar.A -> Set(Seq(SimpleGrammar.B, SimpleGrammar.C, SimpleGrammar.D)),
      SimpleGrammar.B -> Set(Seq(SimpleGrammar.C, SimpleGrammar.D)),
      SimpleGrammar.C -> Set(Seq(SimpleGrammar.D)),
      SimpleGrammar.D -> Set(Seq.empty)
    )

  /* Given the sequence (A B C) | (C B A)
   * The partial followings should be:
   * A: B C
   *    ε
   * B: A
   *    C
   * C: B A
   *    ε
   */
  test("alternation is processed by merging partial followings"):
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.B)
    val aFollow = PartialFollowings.ofNonterminal(SimpleGrammar.A)
    val bFollow = PartialFollowings.ofNonterminal(SimpleGrammar.B)
    val abFollow = PartialFollowings.ofConcat(aFollow, bFollow, bAlt)
    val cAlt = Alternatives.ofSymbol(SimpleGrammar.C)
    val cFollow = PartialFollowings.ofNonterminal(SimpleGrammar.C)
    val abcFollow = PartialFollowings.ofConcat(abFollow, cFollow, cAlt)
    val cbFollow = PartialFollowings.ofConcat(cFollow, bFollow, bAlt)
    val aAlt = Alternatives.ofSymbol(SimpleGrammar.A)
    val cbaFollow = PartialFollowings.ofConcat(cbFollow, aFollow, aAlt)
    PartialFollowings.ofAlternation(abcFollow, cbaFollow) shouldBe Map(
      SimpleGrammar.A -> Set(Seq(SimpleGrammar.B, SimpleGrammar.C), Seq.empty),
      SimpleGrammar.B -> Set(Seq(SimpleGrammar.A), Seq(SimpleGrammar.C)),
      SimpleGrammar.C -> Set(Seq(SimpleGrammar.B, SimpleGrammar.A), Seq.empty)
    )
