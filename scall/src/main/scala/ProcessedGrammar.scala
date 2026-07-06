package it.unibo.scall

import Element.*

case class InternalNonTerminal()
type AnyNonTerminal = Rule | InternalNonTerminal
type AnySymbol = Terminal | AnyNonTerminal
type SymbolSeq = Seq[AnySymbol]
type Alternatives = Set[SymbolSeq]

object Alternatives:
  def ofTerminal(s: Terminal): Alternatives = Set(Seq(s))
