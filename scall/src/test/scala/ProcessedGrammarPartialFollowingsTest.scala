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

  /* Given the sequence A (B C)? D
   * The partial followings should be:
   * A: B C D
   *    D
   * B: C D
   * C: D
   * D: ε
   */
  test("optional is processed by preserving partial followings"):
    val cAlt = Alternatives.ofSymbol(SimpleGrammar.C)
    val bFollow = PartialFollowings.ofNonterminal(SimpleGrammar.B)
    val cFollow = PartialFollowings.ofNonterminal(SimpleGrammar.C)
    val bcFollow = PartialFollowings.ofConcat(bFollow, cFollow, cAlt)
    val bcOptFollow = PartialFollowings.ofOptional(bcFollow)
    bcOptFollow shouldBe Map(
      SimpleGrammar.B -> Set(Seq(SimpleGrammar.C)),
      SimpleGrammar.C -> Set(Seq.empty)
    )
    val aFollow = PartialFollowings.ofNonterminal(SimpleGrammar.A)
    val bAlt = Alternatives.ofSymbol(SimpleGrammar.B)
    val bcAlt = Alternatives.ofConcat(bAlt, cAlt)
    val bcOptAlt = Alternatives.ofOptional(bcAlt)
    val abcOptFollow = PartialFollowings.ofConcat(aFollow, bcOptFollow, bcOptAlt)
    val dAlt = Alternatives.ofSymbol(SimpleGrammar.D)
    val dFollow = PartialFollowings.ofNonterminal(SimpleGrammar.D)
    PartialFollowings.ofConcat(abcOptFollow, dFollow, dAlt) shouldBe Map(
      SimpleGrammar.A -> Set(Seq(SimpleGrammar.B, SimpleGrammar.C, SimpleGrammar.D), Seq(SimpleGrammar.D)),
      SimpleGrammar.B -> Set(Seq(SimpleGrammar.C, SimpleGrammar.D)),
      SimpleGrammar.C -> Set(Seq(SimpleGrammar.D)),
      SimpleGrammar.D -> Set(Seq.empty)
    )

  /* Given the sequence A*
   * The partial followings should be:
   * R: ε
   * Where R is (see productions test):
   * R 🡒 A R
   * R 🡒 ε
   */
  test("zeroOrMore is processed as a single empty partial following"):
    val repetitionSymbol = InternalNonterminal()
    PartialFollowings.ofZeroOrMore(repetitionSymbol) shouldBe Map(
      repetitionSymbol -> Set(Seq.empty)
    )

  /* Given the sequence (A | B)+ C
   * The partial followings should be:
   * A: R C
   * B: R C
   * R: C
   * C: ε
   * Where R is (see productions test):
   * R 🡒 A R
   * R 🡒 B R
   * R 🡒 ε
   */
  test("oneOrMore is processed by updating partial followings for repetition"):
    val aFollow = PartialFollowings.ofNonterminal(SimpleGrammar.A)
    val bFollow = PartialFollowings.ofNonterminal(SimpleGrammar.B)
    val orFollow = PartialFollowings.ofAlternation(aFollow, bFollow)
    val repetitionSymbol = InternalNonterminal()
    val plusFollow = PartialFollowings.ofOneOrMore(orFollow, repetitionSymbol)
    plusFollow shouldBe Map(
      SimpleGrammar.A -> Set(Seq(repetitionSymbol)),
      SimpleGrammar.B -> Set(Seq(repetitionSymbol)),
      repetitionSymbol -> Set(Seq.empty),
    )
    val cAlt = Alternatives.ofSymbol(SimpleGrammar.C)
    val cFollow = PartialFollowings.ofNonterminal(SimpleGrammar.C)
    PartialFollowings.ofConcat(plusFollow, cFollow, cAlt) shouldBe Map(
      SimpleGrammar.A -> Set(Seq(repetitionSymbol, SimpleGrammar.C)),
      SimpleGrammar.B -> Set(Seq(repetitionSymbol, SimpleGrammar.C)),
      repetitionSymbol -> Set(Seq(SimpleGrammar.C)),
      SimpleGrammar.C -> Set(Seq.empty)
    )
