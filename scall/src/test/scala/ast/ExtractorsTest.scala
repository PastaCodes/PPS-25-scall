package it.unibo.scall.ast

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import it.unibo.scall.Element.{Eps, Nonterminal, Terminal}
import it.unibo.scall.{InternalNonterminal, Token}
import it.unibo.scall.ast.Extractors.*

class ExtractorsTest extends AnyFunSuite:

  val mockToken = Token.Valid(Terminal("id", "a".r), "myVar")
  val leafNode = CSTNode.LeafNode(mockToken)

  test("Leaf extractor returns the token lexeme"):
    leafNode match
      case Leaf(value) => value shouldBe "myVar"
      case _ => fail()

  test("Rule extractor extracts the name from an Element.Nonterminal"):
    val rule = CSTNode.RuleNode(Nonterminal("statement", () => Eps), Seq(leafNode))
    rule match
      case Rule("statement", children) => children.head shouldBe leafNode
      case _ => fail()

  test("Rule extractor extracts the name from an InternalNonterminal"):
    val repRule = CSTNode.RuleNode(InternalNonterminal("statement*"), Seq.empty)
    repRule match
      case Rule("statement*", children) => children shouldBe empty
      case _ => fail()