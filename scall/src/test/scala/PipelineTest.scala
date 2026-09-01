package it.unibo.scall

import ast.{AstDecoder, AstError, CSTNode}
import ast.TypedExtractors.*
import grammar.Grammar
import grammar.Element.{Nonterminal, Terminal}
import parser.ParseError.show

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

object PipelineTest:

  object Calc extends Grammar:
    val sum:  Nonterminal = -> ( NUM ++ PLUS ++ NUM )
    val PLUS: Terminal = -> ("+")
    val NUM:  Terminal = -> ("[0-9]+".r)
    val WS:   Terminal = -> ("[ \t\r\n]+".r, skip = true)

  enum CalcNode:
    case PlusNode(left: CalcNode, right: CalcNode)
    case IntNode(value: Int)

  import Calc.*
  import CalcNode.*

  given AstDecoder[CalcNode] with
    def decode(node: CSTNode): Either[AstError, CalcNode] = node match
      case sum(NUM(left), PLUS(_), NUM(right)) =>
        Right(PlusNode(IntNode(left.toInt), IntNode(right.toInt)))
      case _ => Left(AstError.UnexpectedNodeStructure(sum, node))

class PipelineTest extends AnyFunSuite:
  import PipelineTest.*
  import PipelineTest.given
  import CalcNode.*

  private lazy val analyze = ScaLL.analyzer[CalcNode](Calc, Calc.sum)

  test("a well formed input is analysed and decoded"):
    val report = analyze("1 + 2")
    report.parseErrors shouldBe empty
    report.isValid shouldBe true
    report.decoded shouldBe Right(PlusNode(IntNode(1), IntNode(2)))

  test("skipped terminals never reach the parser"):
    analyze("   1   +   2   ").decoded shouldBe Right(PlusNode(IntNode(1), IntNode(2)))

  test("an invalid character does not stop the analysis"):
    analyze("1 @ + 2").parseErrors.map(_.show) should contain ("'@' is not a valid character")

  test("input left over after the start symbol is reported"):
    analyze("1 + 2 3").parseErrors.map(_.show) should contain ("unexpected '3' after the end of the program")

  test("errors are accumulated instead of short-circuiting"):
    val report = analyze("1 1 + 2 3")
    report.isParseValid shouldBe false
    report.parseErrors.size should be > 1

  test("error messages are deterministic"):
    analyze("1 +").parseErrors.map(_.show) shouldBe analyze("1 +").parseErrors.map(_.show)