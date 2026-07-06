package it.unibo.scall

import Element.*

case class InternalNonterminal()
type AnyNonterminal = Rule | InternalNonterminal
type AnySymbol = Terminal | AnyNonterminal
type SymbolSeq = Seq[AnySymbol]
type Alternatives = Set[SymbolSeq]
type Productions = Map[AnyNonterminal, Alternatives]

object Alternatives:
  def ofTerminal(s: Terminal): Alternatives = Set(Seq(s))
  def ofNonterminal(s: Rule): Alternatives = Set(Seq(s))
  def ofConcat(t1: Alternatives, t2: Alternatives): Alternatives = t1 productConcat t2
  def ofAlternation(t1: Alternatives, t2: Alternatives): Alternatives = t1 union t2
  def ofOptional(t: Alternatives): Alternatives = t incl Seq.empty
  def ofZeroOrMore(rep: InternalNonterminal): Alternatives = Set(Seq(rep))
  def ofOneOrMore(t: Alternatives, rep: InternalNonterminal): Alternatives = t.map(_ appended rep)

object Productions:
  def ofNonTerminal(s: Rule, b: Alternatives): Productions = Map(s -> b)

extension [A](self: Set[Seq[A]])
  infix def productConcat(other: Set[Seq[A]]): Set[Seq[A]] =
    for
      x <- self
      y <- other
    yield x concat y
