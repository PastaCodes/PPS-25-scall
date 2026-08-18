package it.unibo.scall
package ast

import grammar.ProcessedGrammar.AnyNonterminal
import lexer.Token

enum CSTNode:
  case RuleNode(symbol: AnyNonterminal, children: Seq[CSTNode])
  case LeafNode(token: Token)

object Extractors:
  object Rule:
    def unapply(node: CSTNode): Option[(String, Seq[CSTNode])] = node match
      case CSTNode.RuleNode(sym, children) => Some((sym.name, children))
      case _                               => None

  object RuleSeq:
    def unapplySeq(node: CSTNode): Option[(String, Seq[CSTNode])] = node match
      case CSTNode.RuleNode(sym, children) => Some((sym.name, children))
      case _                               => None

  object Leaf:
    def unapply(node: CSTNode): Option[String] = node match
      case CSTNode.LeafNode(token) => Some(token.lexeme)
      case _                       => None
