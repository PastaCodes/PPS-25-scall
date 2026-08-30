package it.unibo.scall
package ast

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.Inside.inside
import grammar.Element.{Eps, Nonterminal, Terminal}
import lexer.{Position, Token}
import ast.Extractors.*

class ExtractorsTest extends AnyFunSuite:

  private val leafNode = CSTNode.LeafNode(Token.Valid(Terminal("ID", "[a-z]+".r), "myVar", Position(1, 1)))
  private val ruleNode = CSTNode.RuleNode(Nonterminal("statement", () => Eps), Seq(leafNode))

  test("Leaf extractor extracts lexemes from token leaves"):
    inside(leafNode):
      case Leaf(lexeme) => lexeme shouldBe "myVar"

  test("Rule extractor extracts rule names from standard non-terminals"):
    inside(ruleNode):
      case Rule("statement", children) =>
        children shouldBe Seq(leafNode)

  test("RuleSeq extractor deconstructs CST children positionally"):
    val binaryNode = CSTNode.RuleNode(Nonterminal("binary", () => Eps), Seq(leafNode, leafNode, leafNode))
    inside(binaryNode):
      case RuleSeq("binary", left, op, right) =>
        left shouldBe leafNode
        op shouldBe leafNode
        right shouldBe leafNode
