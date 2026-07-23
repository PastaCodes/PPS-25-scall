package it.unibo.scall.ast

import it.unibo.scall.ast.Extractors.*
import it.unibo.scall.grammar.Element.{Eps, Nonterminal, Terminal}
import it.unibo.scall.grammar.InternalNonterminal
import it.unibo.scall.lexer.Token
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

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
    val rule = CSTNode.RuleNode(InternalNonterminal("statement*"), Seq.empty)
    rule match
      case Rule("statement*", children) => children shouldBe empty
      case _ => fail()