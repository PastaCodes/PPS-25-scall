package it.unibo.scall
package ast

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import grammar.Element.{Nonterminal, Terminal}
import lexer.{Position, Token}
import ast.AstDecoder.*
import ast.Extractors.*

object DecoderTestFixtures:
  sealed trait Expr
  case class IntLiteral(value: Int) extends Expr
  case class Identifier(name: String) extends Expr
  case class BinaryOp(op: String, left: Expr, right: Expr) extends Expr
  case class Block(statements: Seq[Expr]) extends Expr

class AstDecoderTest extends AnyFunSuite:
  import DecoderTestFixtures.*

  private val idTerminal: Terminal = Terminal("ID", "[a-zA-Z0-9+*-]+".r)
  private val numTerminal: Terminal = Terminal("NUM", "[a-zA-Z0-9+*-]+".r)
  private val opTerminal: Terminal = Terminal("OP", "[a-zA-Z0-9+*-]+".r)

  private def leaf(lexeme: String, tokenName: String = "ID"): CSTNode.LeafNode =
    val terminal: Terminal = tokenName match
      case "NUM" => numTerminal
      case "OP" => opTerminal
      case _ => idTerminal
    CSTNode.LeafNode(Token.Valid(terminal, lexeme, Position(1, 1)))

  private def rule(name: String, children: CSTNode*): CSTNode.RuleNode =
    CSTNode.RuleNode(Nonterminal(name, () => null), children)

  given AstDecoder[IntLiteral] with
    def decode(node: CSTNode): Either[AstError, IntLiteral] = node match
      case Leaf(valStr) =>
        valStr.toIntOption
          .map(IntLiteral(_))
          .toRight(AstError.DecodingError(s"Expected integer: '$valStr'"))
      case otherNode => Left(AstError.UnexpectedNodeStructure(numTerminal, otherNode))

  given exprDecoder: AstDecoder[Expr] with
    def decode(node: CSTNode): Either[AstError, Expr] = node match
      case Leaf(valStr) if valStr.headOption.exists(_.isDigit) =>
        valStr.toIntOption.map(IntLiteral(_)).toRight(AstError.DecodingError("Invalid number"))
      case Leaf(name) => Right(Identifier(name))
      case RuleSeq("binary_expr", leftNode, opNode, rightNode) =>
        for
          left <- leftNode.as[Expr]
          opStr <- opNode match
            case Leaf(s) => Right(s)
            case _ => Left(AstError.DecodingError("Operator must be leaf"))
          right <- rightNode.as[Expr]
        yield BinaryOp(opStr, left, right)
      case Rule("block", children) => children.decodeAll[Expr].map(Block(_))
      case _ => Left(AstError.DecodingError("Unrecognized structure"))

  test("pure lifts a value into a successful decoder"):
    leaf("any").as[String](using AstDecoder.pure("Value")) shouldBe Right("Value")

  test("fail lifts an error into a failing decoder"):
    val error = AstError.DecodingError("Failure")
    leaf("any").as[String](using AstDecoder.fail(error)) shouldBe Left(error)

  test("map applies domain transformations safely"):
    val doubledIntDecoder: AstDecoder[Int] = summon[AstDecoder[IntLiteral]].map(_.value * 2)
    leaf("21", "NUM").as[Int](using doubledIntDecoder) shouldBe Right(42)

  test("flatMap sequences dependent decoders based on intermediate results"):
    val positiveIntDecoder: AstDecoder[IntLiteral] = summon[AstDecoder[IntLiteral]].flatMap: lit =>
      if lit.value > 0 then AstDecoder.pure(lit)
      else AstDecoder.fail(AstError.DecodingError("Must be positive"))

    leaf("50", "NUM").as[IntLiteral](using positiveIntDecoder) shouldBe Right(IntLiteral(50))
    leaf("-10", "NUM").as[IntLiteral](using positiveIntDecoder) shouldBe Left(AstError.DecodingError("Must be positive"))

  test("orElse provides fallback decoding strategies upon primary failure"):
    val failingDecoder = AstDecoder.fail[Expr](AstError.DecodingError("Failed"))
    leaf("100", "NUM").as[Expr](using failingDecoder.orElse(exprDecoder)) shouldBe Right(IntLiteral(100))

  test("recursively decodes nested rule trees into complex AST hierarchies"):
    val cst = rule("binary_expr", leaf("x"), leaf("+", "OP"), leaf("10", "NUM"))
    cst.as[Expr] shouldBe Right(BinaryOp("+", Identifier("x"), IntLiteral(10)))

  test("decodeAll accumulates all errors across sequences"):
    val nodes = Seq(leaf("10", "NUM"), leaf("invalid1", "NUM"), leaf("invalid2", "NUM"))
    nodes.decodeAll[IntLiteral] should matchPattern:
      case Left(AstError.AggregateError(errors)) if errors.size == 2 =>

  test("decodeAll returns an unwrapped error if exactly one child fails"):
    val nodes = Seq(leaf("10", "NUM"), leaf("invalid", "NUM"))
    nodes.decodeAll[IntLiteral] should matchPattern:
      case Left(AstError.DecodingError(_)) =>

  test("decodeAll builds the complete collection when all nodes are valid"):
    val nodes = Seq(leaf("1", "NUM"), leaf("2", "NUM"))
    nodes.decodeAll[IntLiteral] shouldBe Right(Seq(IntLiteral(1), IntLiteral(2)))
