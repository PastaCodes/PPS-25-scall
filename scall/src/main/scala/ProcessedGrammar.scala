package it.unibo.scall

import Element.*

type MultiMap[K, V] = Map[K, Set[V]]

case class InternalNonterminal()
type Symbol = Terminal | Rule
type AnyNonterminal = Rule | InternalNonterminal
type AnySymbol = Terminal | AnyNonterminal
type SymbolSeq = Seq[AnySymbol]
type Alternatives = Set[SymbolSeq]
type Productions = MultiMap[AnyNonterminal, SymbolSeq]
type PartialFollowings = MultiMap[AnyNonterminal, SymbolSeq]
case class Following(productionHead: AnyNonterminal, followingSeq: SymbolSeq)
type Followings = MultiMap[AnyNonterminal, Following]

object Alternatives:
  def ofSymbol(s: Symbol): Alternatives                                     = Set(Seq(s))
  def ofConcat(t1: Alternatives, t2: Alternatives): Alternatives            = t1 productConcat t2
  def ofAlternation(t1: Alternatives, t2: Alternatives): Alternatives       = t1 union t2
  def ofOptional(t: Alternatives): Alternatives                             = t incl Seq.empty
  def ofZeroOrMore(rep: InternalNonterminal): Alternatives                  = Set(Seq(rep))
  def ofOneOrMore(t: Alternatives, rep: InternalNonterminal): Alternatives  = t.map(_ appended rep)

object Productions:
  def ofNonterminal(s: Rule, b: Alternatives): Productions =
    Map(s -> b)
  def ofOrMore(t: Alternatives, rep: InternalNonterminal): Productions =
    Map(rep -> (t.map(_ appended rep) incl Seq.empty))

object PartialFollowings:
  def ofNonterminal(s: Rule): PartialFollowings =
    Map(s -> Set(Seq.empty))
  def ofConcat(f1: PartialFollowings, f2: PartialFollowings, t2: Alternatives): PartialFollowings =
    f1.map((k, v) => (k, v productConcat t2)) concat f2
  def ofAlternation(f1: PartialFollowings, f2: PartialFollowings): PartialFollowings =
    f1 unionAll f2

extension [A](self: Set[Seq[A]])
  infix def productConcat(other: Set[Seq[A]]): Set[Seq[A]] =
    for
      x <- self
      y <- other
    yield x concat y

extension [K](self: Set[K])
  def associateWith[V](valueSelector: K => V): Map[K, V] =
    self.map(k => k -> valueSelector(k)).toMap

extension [K, V](self: MultiMap[K, V])
  def getOrEmpty(key: K): Set[V] =
    self.getOrElse(key, Set.empty)
  infix def unionAll(other: MultiMap[K, V]): MultiMap[K, V] =
    val keys = self.keySet union other.keySet
    keys.associateWith(k => self.getOrEmpty(k) union other.getOrEmpty(k))
