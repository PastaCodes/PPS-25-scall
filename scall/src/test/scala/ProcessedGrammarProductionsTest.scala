package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarProductionsTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val X = -> (a | b)
    val a = -> ("a")
    val b = -> ("b")

  import SimpleGrammar.*

  test("ε is processed without adding productions"):
    ProcessedGrammar.visit(Element.Eps).productions shouldBe empty

  test("terminal is processed without adding productions"):
    ProcessedGrammar.visit(a).productions shouldBe empty

  /* Given a production S 🡒 α X β
   * Where X 🡒 a | b
   * The additional productions should be:
   * X 🡒 a
   * X 🡒 b
   */
  test("nonterminal is processed as productions for all rule body alternatives"):
    ProcessedGrammar.visit(X).productions shouldBe Map(
      X -> Set(
        Seq(a),
        Seq(b)
      )
    )

  test("concatenation is processed without adding productions"):
    ProcessedGrammar.visit(a ++ b).productions shouldBe empty

  test("alternation is processed without adding productions"):
    ProcessedGrammar.visit(a | b).productions shouldBe empty

  test("optional is processed without adding productions"):
    ProcessedGrammar.visit(a.?).productions shouldBe empty

  /* Given a production S 🡒 α (a | b)* β
   * The additional productions should be:
   * R 🡒 a R
   * R 🡒 b R
   * R 🡒 ε
   * Where R is used in (see alternatives test):
   * S 🡒 α R β
   */
  test("zeroOrMore is processed as productions concatenating repetition and adding empty production"):
    ProcessedGrammar.visit((a | b).*).productions.toSeq should matchPattern:
      case Seq(
        (repetitionSymbol: InternalNonterminal) -> sequences
      ) if sequences == Set(
        Seq(a, repetitionSymbol),
        Seq(b, repetitionSymbol),
        Seq.empty
      ) =>

  /* Given a production S 🡒 α (a | b)+ β
   * The additional productions should be:
   * R 🡒 a R
   * R 🡒 b R
   * R 🡒 ε
   * Where R is used in (see alternatives test):
   * S 🡒 α a R β
   * S 🡒 α b R β
   */
  test("oneOrMore is processed as productions concatenating repetition and adding empty production"):
    ProcessedGrammar.visit((a | b).+).productions.toSeq should matchPattern:
      case Seq(
        (repetitionSymbol: InternalNonterminal) -> sequences
      ) if sequences == Set(
        Seq(a, repetitionSymbol),
        Seq(b, repetitionSymbol),
        Seq.empty
      ) =>
