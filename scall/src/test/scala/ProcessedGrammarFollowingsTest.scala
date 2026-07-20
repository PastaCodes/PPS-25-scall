package it.unibo.scall

import ProcessedGrammar.{Following, InternalNonterminal}

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarFollowingsTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val X = -> (A ++ B)
    val A = -> (a)
    val B = -> (b)
    val a = -> ("a")
    val b = -> ("b")

  import SimpleGrammar.*

  test("ε is processed without adding followings"):
    ProcessedGrammar.visit(Element.Eps).followings shouldBe empty

  test("terminal is processed without adding followings"):
    ProcessedGrammar.visit(a).followings shouldBe empty

  /* Given the production X 🡒 A B
   * The additional followings should be:
   * A: B       (head = X)
   * B: ε       (head = X)
   */
  test("nonterminal is processed by turning partial followings into followings"):
    ProcessedGrammar.visit(X).followings shouldBe Map(
      A -> Set(Following(X, Seq(B))),
      B -> Set(Following(X, Seq.empty))
    )

  test("concatenation is processed without adding followings"):
    ProcessedGrammar.visit(a ++ b).followings shouldBe empty

  test("alternation is processed without adding followings"):
    ProcessedGrammar.visit(a | b).followings shouldBe empty

  test("optional is processed without adding followings"):
    ProcessedGrammar.visit(a.?).followings shouldBe empty

  /* Given the sequence (A B)*
   * The additional followings should be:
   * A: B R     (head = R)
   * B: R       (head = R)
   * R: ε       (head = R)
   * Where R is (see productions test):
   * R 🡒 A B R
   * R 🡒 ε
   */
  test("zeroOrMore is processed by updating followings for repetition"):
    val followings = ProcessedGrammar.visit((A ++ B).*).followings
    val opt = followings.keySet.find(_.isInstanceOf[InternalNonterminal])
    opt should not be empty
    val repetitionSymbol = opt.get
    followings shouldBe Map(
      A -> Set(Following(repetitionSymbol, Seq(B, repetitionSymbol))),
      B -> Set(Following(repetitionSymbol, Seq(repetitionSymbol))),
      repetitionSymbol -> Set(Following(repetitionSymbol, Seq.empty))
    )

  /* Given the sequence (A B)+
   * The additional followings should be:
   * A: B R     (head = R)
   * B: R       (head = R)
   * R: ε       (head = R)
   * Where R is (see productions test):
   * R 🡒 A B R
   * R 🡒 ε
   */
  test("oneOrMore is processed by updating followings for repetition"):
    val followings = ProcessedGrammar.visit((A ++ B).+).followings
    val opt = followings.keySet.find(_.isInstanceOf[InternalNonterminal])
    opt should not be empty
    val repetitionSymbol = opt.get
    followings shouldBe Map(
      A -> Set(Following(repetitionSymbol, Seq(B, repetitionSymbol))),
      B -> Set(Following(repetitionSymbol, Seq(repetitionSymbol))),
      repetitionSymbol -> Set(Following(repetitionSymbol, Seq.empty))
    )
