package it.unibo.scall
package grammar

import grammar.ProcessedGrammar.{Alternatives, InternalNonterminal}
import util.CollectionUtils.flattenEntries

import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class LeftFactoringTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val X = -> (
      a ++ b ++ (a ++ b ++ (a | b) | b)
    | b ++ b
    | b ++ c
    | c ++ a
    )
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")

  import SimpleGrammar.*

  private val alternatives1: Alternatives = Set(
    Seq(a, b),
    Seq(a, c)
  )
  private val alternatives2: Alternatives = Set(
    Seq(a, b, a, b, a),
    Seq(a, b, a, b, b),
    Seq(a, b, b),
    Seq(b, b),
    Seq(b, c),
    Seq(c, a)
  )

  test("should compute the longest common prefix"):
    val first = Seq(a, b, a, a)
    val second = Seq(a, b, b, a)
    val (common, firstSuffix, secondSuffix) = ProcessedGrammar.longestCommonPrefix(first, second)
    common shouldBe Seq(a, b)
    firstSuffix shouldBe Seq(a, a)
    secondSuffix shouldBe Seq(b, a)

  test("should group alternatives by common prefixes"):
    ProcessedGrammar.prefixed(alternatives2) shouldBe Map(
      Seq(a, b) -> Set(
        Seq(a, b, a),
        Seq(a, b, b),
        Seq(b)
      ),
      Seq(b) -> Set(
        Seq(b),
        Seq(c)
      ),
      Seq(c, a) -> Set(
        Seq.empty
      )
    )

  /* Given alternatives:
   * a b | a c
   * Left factoring should result in:
   * a F
   * With productions:
   * F 🡒 b
   * F 🡒 c
   */
  test("should apply left factoring"):
    val (alternatives, productions) = ProcessedGrammar.leftFactor(alternatives1)
    val f = alternatives.collectFirst { case Seq(`a`, f: InternalNonterminal) => f }.value
    alternatives shouldBe Set(
      Seq(a, f)
    )
    productions shouldBe Map(
      f -> Set(
        Seq(b),
        Seq(c)
      )
    )

  /* Given alternatives:
   * a b a b a
   * a b a b b
   * a b b
   * b b
   * b c
   * c a
   * Left factoring should result in:
   * a b F1
   * b F2
   * c a
   * With productions:
   * F1 🡒 a b F3
   * F1 🡒 b
   * F2 🡒 b
   * F2 🡒 c
   * F3 🡒 a
   * F3 🡒 b
   */
  test("should apply left factoring recursively"):
    val (alternatives, productions) = ProcessedGrammar.leftFactor(alternatives2)
    val f1 = alternatives.collectFirst { case Seq(`a`, `b`, f: InternalNonterminal) => f }.value
    val f2 = alternatives.collectFirst { case Seq(`b`, f: InternalNonterminal) => f }.value
    alternatives shouldBe Set(
      Seq(a, b, f1),
      Seq(b, f2),
      Seq(c, a)
    )
    val f3 = (productions.keySet - f1 - f2).head
    productions shouldBe Map(
      f1 -> Set(
        Seq(a, b, f3),
        Seq(b)
      ),
      f2 -> Set(
        Seq(b),
        Seq(c)
      ),
      f3 -> Set(
        Seq(a),
        Seq(b)
      )
    )

  test("should apply left factoring when visiting a production rule"):
    val productions = ProcessedGrammar.visit(X).productions
    val flat = productions.flattenEntries
    val f1 = flat.collectFirst { case `X` -> Seq(`a`, `b`, f: InternalNonterminal) => f }.value
    val f2 = flat.collectFirst { case `X` -> Seq(`b`, f: InternalNonterminal) => f }.value
    val f3 = flat.collectFirst { case `f1` -> Seq(`a`, `b`, f: InternalNonterminal) => f }.value
    productions shouldBe Map(
      X -> Set(
        Seq(a, b, f1),
        Seq(b, f2),
        Seq(c, a)
      ),
      f1 -> Set(
        Seq(a, b, f3),
        Seq(b)
      ),
      f2 -> Set(
        Seq(b),
        Seq(c)
      ),
      f3 -> Set(
        Seq(a),
        Seq(b)
      )
    )
