package it.unibo.scall.ast

import it.unibo.scall.grammar.Element.{Eps, Nonterminal, Terminal}
import it.unibo.scall.lexer.Token
import it.unibo.scall.ast.Extractors.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

trait MockAst
case class MockId(value: String) extends MockAst

class TestVisitor extends AstVisitor[MockAst]:
  override val visitLogic: PartialFunction[CSTNode, MockAst] = {
    case Leaf("test_val") => MockId("test_val")
  }

class AstVisitorTest extends AnyFunSuite:

  val visitor = TestVisitor()

  test("Visitor applies the partial function correctly"):
    val node = CSTNode.LeafNode(Token.Valid(Terminal("x", "x".r), "test_val"))
    visitor.visit(node) shouldBe MockId("test_val")

  test("Visitor throws exception for tokens not covered by the partial function"):
    val unhandledToken = CSTNode.LeafNode(Token.Error("!"))
    val ex = intercept[IllegalArgumentException]:
      visitor.visit(unhandledToken)
    ex.getMessage should include("!")

  test("Visitor throws exception for rule nodes not covered by the partial function"):
    val unhandledRule = CSTNode.RuleNode(Nonterminal("missingRule", () => Eps), Seq.empty)
    val ex = intercept[IllegalArgumentException]:
      visitor.visit(unhandledRule)
    ex.getMessage should include("missingRule")