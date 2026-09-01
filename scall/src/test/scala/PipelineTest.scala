package it.unibo.scall

import ast.{AstDecoder, AstError, CSTNode}
import grammar.Grammar
import grammar.Element.{Nonterminal, Terminal}
import lexer.Token
import parser.ParseError.show
import ast.CSTNode.{LeafNode, RuleNode}

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class PipelineTest extends AnyFunSuite:

  object Calc extends Grammar:
    val expr: Nonterminal = ->(term ++ (PLUS ++ term).*)
    val term: Nonterminal = ->(NUM | LPAREN ++ expr ++ RPAREN)
    val PLUS: Terminal = ->("+")
    val NUM: Terminal = ->("[0-9]+".r)
    val LPAREN: Terminal = ->("(")
    val RPAREN: Terminal = ->(")")
    val WS: Terminal = ->("[ \t\r\n]+".r, skip = true)

  private given AstDecoder[Int] with
    def decode(node: CSTNode): Either[AstError, Int] = node match
      case LeafNode(Token.Valid(t, lexeme, _)) if t == Calc.NUM => Right(lexeme.toInt)
      case LeafNode(_) => Right(0)
      case RuleNode(_, children) =>
        children.foldLeft[Either[AstError, Int]](Right(0)): (acc, child) =>
          for a <- acc; c <- decode(child) yield a + c
      case CSTNode.ErrorNode(_, _) => Left(AstError.DecodingError("error node in the tree"))

  private lazy val analyze = ScaLL.analyzer[Int](Calc, Calc.expr)

  test("a well formed input is analyzed with no errors"):
    val report = analyze("1 + (2 + 3)")
    report.parseErrors shouldBe empty
    report.isValid shouldBe true
    report.decoded shouldBe Right(6)

  test("skipped terminals never reach the parser"):
    analyze("1   +    2   ").decoded shouldBe Right(3)

  test("errors are accumulated"):
    val report = analyze("1 ++ 2 )")
    report.parseErrors.size should be > 1
    report.isParseValid shouldBe false

  test("an invalid character does not stop the analysis"):
    val report = analyze("1 @ + 2")
    report.parseErrors.map(_.show) should contain ("'@' is not a valid character")
