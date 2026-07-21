package it.unibo.scall.ast

import it.unibo.scall.Element.Nonterminal
import it.unibo.scall.InternalNonterminal

object Extractors:
  object Rule:
    def unapply(node: CSTNode): Option[(String, Seq[CSTNode])] = node match
      case CSTNode.RuleNode(sym, children) =>
        val name = sym match
          case n: Nonterminal => n.name
          case i: InternalNonterminal => i.name
        Some((name, children))
      case _ => None

  object Leaf:
    def unapply(node: CSTNode): Option[String] = node match
      case CSTNode.LeafNode(tok) => Some(tok.lexeme)
      case _ => None