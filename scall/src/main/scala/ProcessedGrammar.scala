package it.unibo.scall

import Element.*

type MultiMap[K, V] = Map[K, Set[V]]

type Symbol = Terminal | Rule
case class InternalNonterminal()
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
  def ofOneOrMore(t: Alternatives, rep: InternalNonterminal): Alternatives  = t eachAppend rep

object Productions:
  def ofNonterminal(s: Rule, b: Alternatives): Productions =
    Map(s -> b)
  def ofOrMore(t: Alternatives, rep: InternalNonterminal): Productions =
    Map(rep -> (t eachAppend rep incl Seq.empty))

object PartialFollowings:
  def ofNonterminal(s: Rule): PartialFollowings =
    Map(s -> Set(Seq.empty))
  def ofConcat(p1: PartialFollowings, p2: PartialFollowings, t2: Alternatives): PartialFollowings =
    p1.mapValues1(_ productConcat t2) concat p2
  def ofAlternation(p1: PartialFollowings, p2: PartialFollowings): PartialFollowings =
    p1 unionAll p2
  def ofOptional(p: PartialFollowings): PartialFollowings =
    p
  def ofZeroOrMore(rep: InternalNonterminal): PartialFollowings =
    Map(rep -> Set(Seq.empty))
  def ofOneOrMore(p: PartialFollowings, rep: InternalNonterminal): PartialFollowings =
    p.mapValues1(_ eachAppend rep) updated (rep, Set(Seq.empty))

object Followings:
  def ofNonterminal(s: Rule, p: PartialFollowings, f: Followings): Followings =
    f unionAll p.mapValues1(_.map(Following(s, _)))
  def ofOrMore(p: PartialFollowings, rep: InternalNonterminal, t: Alternatives): Followings =
    p.mapValues1(_.map(q => Following(rep, q appended rep))) updated (rep, Set(Following(rep, Seq.empty)))

extension [A](self: Set[Seq[A]])
  infix def eachAppend(e: A): Set[Seq[A]] =
    self.map(_ appended e)
  infix def productConcat(other: Set[Seq[A]]): Set[Seq[A]] =
    for
      x <- self
      y <- other
    yield x concat y

extension [K](self: Set[K])
  def associateWith[V](valueSelector: K => V): Map[K, V] =
    self.map(k => k -> valueSelector(k)).toMap

extension [K, V](self: Map[K, V])
  def mapValues1[W](transform: V => W): Map[K, W] =
    self.map((k, v) => (k, transform(v)))

extension [K, V](self: MultiMap[K, V])
  def getOrEmpty(key: K): Set[V] =
    self.getOrElse(key, Set.empty)
  infix def unionAll(other: MultiMap[K, V]): MultiMap[K, V] =
    val keys = self.keySet union other.keySet
    keys.associateWith(k => self.getOrEmpty(k) union other.getOrEmpty(k))
