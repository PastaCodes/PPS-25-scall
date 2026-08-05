package it.unibo.scall
package grammar

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class LeftFactoringTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object SimpleGrammar extends Grammar:
    val X = -> (a ++ b | a ++ c)
    val a = -> ("a")
    val b = -> ("b")
    val c = -> ("c")

  import SimpleGrammar.*

  test("should compute the longest common prefix"):
    val first = Seq(a, b, a, a)
    val second = Seq(a, b, b, a)
    val (common, firstSuffix, secondSuffix) = ProcessedGrammar.longestCommonPrefix(first, second)
    common shouldBe Seq(a, b)
    firstSuffix shouldBe Seq(a, a)
    secondSuffix shouldBe Seq(b, a)

  test("should group alternatives by common prefixes"):
    ProcessedGrammar.prefixed(Set(
      Seq(a, b, a, b, a),
      Seq(a, b, a, b, b),
      Seq(a, b, b),
      Seq(b, a),
      Seq(b, b),
      Seq(c, a)
    )) shouldBe Map(
      Seq(a, b) -> Set(
        Seq(a, b, a),
        Seq(a, b, b),
        Seq(b)
      ),
      Seq(b) -> Set(
        Seq(a),
        Seq(b)
      ),
      Seq(c, a) -> Set(
        Seq.empty
      )
    )
