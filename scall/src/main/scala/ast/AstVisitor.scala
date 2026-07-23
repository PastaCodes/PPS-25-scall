package it.unibo.scall.ast

import it.unibo.scall.grammar.Element.Nonterminal
import it.unibo.scall.grammar.{AnyNonterminal, InternalNonterminal}
import it.unibo.scall.lexer.Token


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

trait AstVisitor[AstType]:

  def visitLogic: PartialFunction[CSTNode, AstType]

  def visit(node: CSTNode): AstType =
    if visitLogic.isDefinedAt(node) then
      visitLogic(node)
    else
      node match
        case CSTNode.RuleNode(sym, _) =>
          throw new IllegalArgumentException(s"Visitor error: No translation rule defined for rule node '${sym.name}'")
        case CSTNode.LeafNode(token) =>
          throw new IllegalArgumentException(s"Visitor error: No translation rule defined for token '${token.lexeme}'")

  def visitAll(nodes: Seq[CSTNode]): Seq[AstType] = nodes.map(visit)