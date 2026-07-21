package it.unibo.scall.ast

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import it.unibo.scall.Element.Terminal
import it.unibo.scall.Token
import it.unibo.scall.ast.CSTNode.LeafNode

class CSTNodeTest extends AnyFunSuite:

  test("CSTNode.LeafNode wraps a Token.Valid and retains its lexeme"):
    val terminal: Terminal = Terminal("testTerm", "test".r)
    val token = Token.Valid(terminal, "test")
    val leaf: LeafNode = CSTNode.LeafNode(token)

    leaf.token.lexeme shouldBe "test"