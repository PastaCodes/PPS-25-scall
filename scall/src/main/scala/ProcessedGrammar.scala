package it.unibo.scall

import Element.*

case class InternalNonterminal()
type AnyNonterminal = Rule | InternalNonterminal
type AnySymbol = Terminal | AnyNonterminal
type SymbolSeq = Seq[AnySymbol]
type Alternatives = Set[SymbolSeq]

object Alternatives:
  def ofTerminal(s: Terminal): Alternatives = Set(Seq(s))
  def ofNonterminal(s: Rule): Alternatives = Set(Seq(s))
  def ofAlternation(t1: Alternatives, t2: Alternatives): Alternatives = t1.union(t2)
