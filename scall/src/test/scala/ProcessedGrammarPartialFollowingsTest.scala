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

  import SimpleGrammar.*

  test("terminal is processed without adding partial followings"):
    ProcessedGrammar.visit(a).partialFollowings shouldBe empty

  test("nonterminal is processed as a single empty partial following"):
    ProcessedGrammar.visit(A).partialFollowings shouldBe Map(
      A -> Set(Seq.empty)
    )

  /* Given the sequence A B C D
   * The partial followings should be:
   * A: B C D
   * B: C D
   * C: D
   * D: ε
   */
  test("concatenation is processed as incremental partial followings"):
    ProcessedGrammar.visit(A ++ B ++ C ++ D).partialFollowings shouldBe Map(
      A -> Set(Seq(B, C, D)),
      B -> Set(Seq(C, D)),
      C -> Set(Seq(D)),
      D -> Set(Seq.empty)
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
    ProcessedGrammar.visit((A ++ B ++ C) | (C ++ B ++ A)).partialFollowings shouldBe Map(
      A -> Set(Seq(B, C), Seq.empty),
      B -> Set(Seq(A), Seq(C)),
      C -> Set(Seq(B, A), Seq.empty)
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
    ProcessedGrammar.visit(A ++ (B ++ C).? ++ D).partialFollowings shouldBe Map(
      A -> Set(Seq(B, C, D), Seq(D)),
      B -> Set(Seq(C, D)),
      C -> Set(Seq(D)),
      D -> Set(Seq.empty)
    )

  /* Given the sequence A*
   * The partial followings should be:
   * R: ε
   * Where R is (see productions test):
   * R 🡒 A R
   * R 🡒 ε
   */
  test("zeroOrMore is processed as a single empty partial following"):
    ProcessedGrammar.visit(A.*).partialFollowings.toSeq should matchPattern:
      case Seq(
        (repetitionSymbol: InternalNonterminal) -> sequences
      ) if sequences == Set(
        Seq.empty
      ) =>

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
    val partials = ProcessedGrammar.visit((A | B).+ ++ C).partialFollowings
    val opt = partials.keySet.find(_.isInstanceOf[InternalNonterminal])
    opt should not be empty
    val repetitionSymbol = opt.get
    partials shouldBe Map(
      A -> Set(Seq(repetitionSymbol, C)),
      B -> Set(Seq(repetitionSymbol, C)),
      repetitionSymbol -> Set(Seq(C)),
      C -> Set(Seq.empty)
    )
