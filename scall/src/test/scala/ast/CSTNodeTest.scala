package it.unibo.scall.ast

import it.unibo.scall.grammar.Element.{Eps, Nonterminal, Terminal}
import it.unibo.scall.ast.CSTNode.{LeafNode, RuleNode}
import it.unibo.scall.grammar.InternalNonterminal
import it.unibo.scall.lexer.Token
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class CSTNodeTest extends AnyFunSuite:

  test("CSTNode.LeafNode wraps a Token.Valid and retains its lexeme"):
    val terminal: Terminal = Terminal("testTerm", "test".r)
    val token = Token.Valid(terminal, "test")
    val leaf: LeafNode = CSTNode.LeafNode(token)

    leaf.token.lexeme shouldBe "test"

  test("CSTNode.RuleNode accepts both Nonterminal and InternalNonterminal"):
    val standardRule: Nonterminal = Nonterminal("expr", () => Eps)
    val internalRule = InternalNonterminal("expr_rep")

    val node1: RuleNode = CSTNode.RuleNode(standardRule, Seq.empty)
    val node2: RuleNode = CSTNode.RuleNode(internalRule, Seq.empty)

    node1.symbol shouldBe standardRule
    node2.symbol shouldBe internalRule