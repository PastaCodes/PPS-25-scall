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
  def ofConcat(t1: Alternatives, t2: Alternatives): Alternatives =
    for a1 <- t1; a2 <- t2 yield a1 concat a2
  def ofAlternation(t1: Alternatives, t2: Alternatives): Alternatives = t1 union t2
  def ofOptional(t: Alternatives): Alternatives = t incl Seq.empty
  def ofZeroOrMore(rep: InternalNonterminal): Alternatives = Set(Seq(rep))
