package it.unibo.scall
package ast

import grammar.ProcessedGrammar.AnyNonterminal
import lexer.Token

enum CSTNode:
  case RuleNode(symbol: AnyNonterminal, children: Seq[CSTNode])
  case LeafNode(token: Token)