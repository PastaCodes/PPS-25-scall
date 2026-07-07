package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class ProcessedGrammarAlternativesTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val X = -> (a)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")
    val d = -> ("d")

  import SimpleGrammar.*

  test("terminal is processed as a single alternative"):
    ProcessedGrammar.visit(a).alternatives shouldBe Set(
      Seq(a)
    )

  test("nonterminal is processed as a single alternative"):
    ProcessedGrammar.visit(X).alternatives shouldBe Set(
      Seq(X)
    )

  test("alternation of terminals is processed as two alternatives"):
    ProcessedGrammar.visit(a | b).alternatives shouldBe Set(
      Seq(a), Seq(b)
    )

  /* Given a production S 🡒 α (a | b | c | d) β
   * The result should be:
   * S 🡒 α a β
   * S 🡒 α b β
   * S 🡒 α c β
   * S 🡒 α d β
   */
  test("alternation is processed as the union of alternatives"):
    ProcessedGrammar.visit(a | b | c | d).alternatives shouldBe Set(
      Seq(a), Seq(b), Seq(c), Seq(d)
    )

  test("concatenation of terminals is processed as a single alternative"):
    ProcessedGrammar.visit(a ++ b).alternatives shouldBe Set(
      Seq(a, b)
    )

  /* Given a production S 🡒 α ((a | b) (c | d)) β
   * The result should be:
   * S 🡒 α a c β
   * S 🡒 α a d β
   * S 🡒 α b c β
   * S 🡒 α b d β
   */
  test("concatenation is processed as the product of concatenated alternatives"):
    ProcessedGrammar.visit((a | b) ++ (c | d)).alternatives shouldBe Set(
      Seq(a, c),
      Seq(a, d),
      Seq(b, c),
      Seq(b, d)
    )

  test("optional of terminal is processed as two alternatives"):
    ProcessedGrammar.visit(a.?).alternatives shouldBe Set(
      Seq(a),
      Seq()
    )

  /* Given a production S 🡒 α (a | b)? β
   * The result should be:
   * S 🡒 α a β
   * S 🡒 α b β
   * S 🡒 α β
   */
  test("optional is processed by adding an empty sequence to alternatives"):
    ProcessedGrammar.visit((a | b).?).alternatives shouldBe Set(
      Seq(a),
      Seq(b),
      Seq()
    )

  /* Given a production S 🡒 α (a | b)* β
   * The result should be:
   * S 🡒 α R β
   * Where R is (see productions test):
   * R 🡒 a R
   * R 🡒 b R
   * R 🡒 ε
   */
  test("zeroOrMore is processed as a single alternative"):
    ProcessedGrammar.visit(a.*).alternatives.toSeq should matchPattern:
      case Seq(
        Seq(repetitionSymbol: InternalNonterminal)
      ) =>

  test("oneOrMore of terminal is processed as a single alternative concatenating repetition"):
    ProcessedGrammar.visit(a.+).alternatives.toSeq should matchPattern:
      case Seq(
        Seq(`a`, repetitionSymbol: InternalNonterminal)
      ) =>

  /* Given a production S 🡒 α (a | b)+ β
   * The result should be:
   * S 🡒 α a R β
   * S 🡒 α b R β
   * Where R is (see productions test):
   * R 🡒 a R
   * R 🡒 b R
   * R 🡒 ε
   */
  test("oneOrMore is processed by concatenating repetition to all alternatives"):
    ProcessedGrammar.visit((a | b).+).alternatives.toSeq should matchPattern:
      case Seq(
        Seq(s1, r1: InternalNonterminal),
        Seq(s2, r2: InternalNonterminal)
      ) if Set(s1, s2) == Set(a, b) && r1 == r2 =>
