package it.unibo.scall
package ast

import grammar.Element.{Eps, Nonterminal, Terminal}
import ast.CSTNode.{LeafNode, RuleNode}
import grammar.ProcessedGrammar.InternalNonterminal
import lexer.{Position, Token}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class CSTNodeTest extends AnyFunSuite:

  test("CSTNode.LeafNode wraps a Token.Valid and retains its lexeme"):
    val leaf: LeafNode = CSTNode.LeafNode(Token.Valid(Terminal("testTerm", "test".r), "test", Position(1, 1)))
    leaf.token.lexeme shouldBe "test"

  test("RuleNode encapsulates standard grammar non-terminals"):
    val standardRule: Nonterminal = Nonterminal("expr", () => Eps)
    val node: RuleNode = CSTNode.RuleNode(standardRule, Seq.empty)

    node.symbol shouldBe standardRule
    node.symbol.name shouldBe "expr"
    node.children shouldBe empty
