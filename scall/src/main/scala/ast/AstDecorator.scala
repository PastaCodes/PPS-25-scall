package it.unibo.scall
package ast

import grammar.Element.Nonterminal
import grammar.{InternalNonterminal, AnyNonterminal}
import lexer.Token

extension (sym: AnyNonterminal)
  def name: String = sym match
    case n: Nonterminal => n.name
    case i: InternalNonterminal => i.name

enum CSTNode:
  case RuleNode(symbol: AnyNonterminal, children: Seq[CSTNode])
  case LeafNode(token: Token)

object Extractors:
  object Rule:
    def unapply(node: CSTNode): Option[(String, Seq[CSTNode])] = node match
      case CSTNode.RuleNode(sym, children) => Some((sym.name, children))
      case _ => None

  object Leaf:
    def unapply(node: CSTNode): Option[String] = node match
      case CSTNode.LeafNode(token) => Some(token.lexeme)
      case _ => None

enum AstError:
  case DecodingError(message: String)

trait AstDecoder[A]:
  def decode(node: CSTNode): Either[AstError, A]

object AstDecoder:

  extension (node: CSTNode)
    def as[A](using decoder: AstDecoder[A]): Either[AstError, A] =
      decoder.decode(node)

  extension (nodes: Seq[CSTNode])
    def decodeAll[A](using decoder: AstDecoder[A]): Either[AstError, Seq[A]] =
      nodes.foldLeft[Either[AstError, Seq[A]]](Right(Seq.empty)) { (acc, node) =>
        for
          seq <- acc
          a   <- node.as[A]
        yield seq :+ a
      }
