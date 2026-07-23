package it.unibo.scall.ast

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import it.unibo.scall.grammar.Element.Terminal
import it.unibo.scall.lexer.Token
import it.unibo.scall.ast.AstDecoder.*
import it.unibo.scall.ast.Extractors.*

trait MockAst
case class MockId(value: String) extends MockAst

class AstDecoderTest extends AnyFunSuite:

  given AstDecoder[MockAst] = {
    case Leaf(value) if value != "error" => Right(MockId(value))
    case Leaf("error")                   => Left(AstError.DecodingError("Deliberate error"))
    case _                               => Left(AstError.DecodingError("Not a leaf"))
  }

  val validLeaf1 = CSTNode.LeafNode(Token.Valid(Terminal("id", "a".r), "alpha"))
  val validLeaf2 = CSTNode.LeafNode(Token.Valid(Terminal("id", "b".r), "beta"))
  val errorLeaf  = CSTNode.LeafNode(Token.Valid(Terminal("id", "e".r), "error"))

  test("AstDecoder converts a single node successfully"):
    validLeaf1.as[MockAst] shouldBe Right(MockId("alpha"))

  test("AstDecoder returns Left on failure without throwing runtime exceptions"):
    errorLeaf.as[MockAst] shouldBe Left(AstError.DecodingError("Deliberate error"))

  test("decodeAll successfully decodes a sequence of valid nodes"):
    val nodes = Seq(validLeaf1, validLeaf2)
    nodes.decodeAll[MockAst] shouldBe Right(Seq(MockId("alpha"), MockId("beta")))

  test("decodeAll fails fast and returns Left if any node in the sequence fails"):
    val nodesWithError = Seq(validLeaf1, errorLeaf, validLeaf2)
    nodesWithError.decodeAll[MockAst] shouldBe Left(AstError.DecodingError("Deliberate error"))