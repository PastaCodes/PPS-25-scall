package it.unibo.scall.ast

import it.unibo.scall.AnyNonterminal
import it.unibo.scall.Token

enum CSTNode:
  case RuleNode(symbol: AnyNonterminal, children: Seq[CSTNode])
  case LeafNode(token: Token)