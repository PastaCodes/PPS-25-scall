package it.unibo.scall

import Element.*

case class InternalNonterminal(name: String)
type AnyNonterminal = Nonterminal | InternalNonterminal
type AnySymbol = Terminal | AnyNonterminal
type SymbolSeq = Seq[AnySymbol]
type Alternatives = Set[SymbolSeq]
type Productions = MultiMap[AnyNonterminal, SymbolSeq]
type PartialFollowings = MultiMap[AnyNonterminal, SymbolSeq]
case class Following(productionHead: AnyNonterminal, followingSeq: SymbolSeq)
type Followings = MultiMap[AnyNonterminal, Following]

case class ProcessedGrammar(terminals: Seq[Terminal],
                            productions: Productions,
                            followings: Followings)

case class VisitResult(alternatives: Alternatives,
                       nonterminals: Set[Nonterminal] = Set.empty,
                       productions: Productions = Map.empty,
                       partialFollowings: PartialFollowings = Map.empty,
                       followings: Followings = Map.empty)

object ProcessedGrammar:
  
  def of(g: Grammar, startSymbol: Nonterminal): ProcessedGrammar =
    val res = visit(startSymbol)
    ProcessedGrammar(g.terminals, res.productions, res.followings)
  
  def visit(e: Element, skipNonterminals: Set[Nonterminal] = Set.empty): VisitResult =
    given Set[Nonterminal] = skipNonterminals
    
    e match

      case Eps =>
        VisitResult(alternatives = Alternatives.ofEps)
      
      case s: Terminal =>
        VisitResult(alternatives = Alternatives.ofSymbol(s))
      
      case s: Nonterminal =>
        if !skipNonterminals.contains(s) then
          visitUnary(s.rule())(
            alternativesFn      = _ => Alternatives.ofSymbol(s),
            addNonterminals     = Set(s),
            addProductionsFn    = v => Productions.ofNonterminal(s, v.alternatives),
            partialFollowingsFn = _ => PartialFollowings.ofNonterminal(s),
            addFollowingsFn     = v => Followings.ofNonterminal(s, v.partialFollowings)
          )(using skipNonterminals = skipNonterminals incl s)
        else
          VisitResult(
            alternatives = Alternatives.ofSymbol(s),
            partialFollowings = PartialFollowings.ofNonterminal(s)
          )
      
      case Concat(t1, t2) =>
        visitBinary(t1, t2)(
          (v1, v2) => Alternatives.ofConcat(v1.alternatives, v2.alternatives),
          (v1, v2) => PartialFollowings.ofConcat(v1.partialFollowings, v2.partialFollowings, v2.alternatives)
        )
      
      case Alternation(t1, t2) =>
        visitBinary(t1, t2)(
          (v1, v2) => Alternatives.ofAlternation(v1.alternatives, v2.alternatives),
          (v1, v2) => PartialFollowings.ofAlternation(v1.partialFollowings, v2.partialFollowings)
        )
      
      case Optional(t) =>
        visitUnary(t)(
          alternativesFn      = v => Alternatives.ofOptional(v.alternatives),
          partialFollowingsFn = v => PartialFollowings.ofOptional(v.partialFollowings),
        )
      
      case ZeroOrMore(t) =>
        val repetition = repetitionNonterminal(t)
        visitUnary(t)(
          alternativesFn      = _ => Alternatives.ofZeroOrMore(repetition),
          addProductionsFn    = v => Productions.ofOrMore(v.alternatives, repetition),
          partialFollowingsFn = _ => PartialFollowings.ofZeroOrMore(repetition),
          addFollowingsFn     = v => Followings.ofOrMore(v.partialFollowings, repetition, v.alternatives)
        )
      
      case OneOrMore(t) =>
        val repetition = repetitionNonterminal(t)
        visitUnary(t)(
          alternativesFn      = v => Alternatives.ofOneOrMore(v.alternatives, repetition),
          addProductionsFn    = v => Productions.ofOrMore(v.alternatives, repetition),
          partialFollowingsFn = v => PartialFollowings.ofOneOrMore(v.partialFollowings, repetition),
          addFollowingsFn     = v => Followings.ofOrMore(v.partialFollowings, repetition, v.alternatives)
        )
  
  private def visitUnary(t: Element)
                        (alternativesFn: VisitResult => Alternatives,
                         addTerminals: Set[Terminal] = Set.empty,
                         addNonterminals: Set[Nonterminal] = Set.empty,
                         addProductionsFn: VisitResult => Productions = _ => Map.empty,
                         partialFollowingsFn: VisitResult => PartialFollowings,
                         addFollowingsFn: VisitResult => Followings = _ => Map.empty)
                        (using skipNonterminals: Set[Nonterminal]): VisitResult =
    val v = ProcessedGrammar.visit(t, skipNonterminals)
    VisitResult(
      alternativesFn(v),
      v.nonterminals union addNonterminals,
      v.productions unionAll addProductionsFn(v),
      partialFollowingsFn(v),
      v.followings unionAll addFollowingsFn(v)
    )
  
  private def visitBinary(t1: Element, t2: Element)
                         (alternativesFn: (VisitResult, VisitResult) => Alternatives,
                          partialFollowingsFn: (VisitResult, VisitResult) => PartialFollowings)
                         (using skipNonterminals: Set[Nonterminal]): VisitResult =
    val v1 = ProcessedGrammar.visit(t1, skipNonterminals)
    val v2 = ProcessedGrammar.visit(t2, skipNonterminals union v1.nonterminals)
    VisitResult(
      alternativesFn(v1, v2),
      v1.nonterminals union v2.nonterminals,
      v1.productions unionAll v2.productions,
      partialFollowingsFn(v1, v2),
      v1.followings unionAll v2.followings
    )
  
  private def repetitionNonterminal(t: Element): InternalNonterminal =
    InternalNonterminal(name = ZeroOrMore(t).show)

object Alternatives:
  def ofEps: Alternatives                                                   = Set(Seq.empty)
  def ofSymbol(s: Symbol): Alternatives                                     = Set(Seq(s))
  def ofConcat(t1: Alternatives, t2: Alternatives): Alternatives            = t1 productConcat t2
  def ofAlternation(t1: Alternatives, t2: Alternatives): Alternatives       = t1 union t2
  def ofOptional(t: Alternatives): Alternatives                             = t incl Seq.empty
  def ofZeroOrMore(rep: InternalNonterminal): Alternatives                  = Set(Seq(rep))
  def ofOneOrMore(t: Alternatives, rep: InternalNonterminal): Alternatives  = t eachAppend rep

object Productions:
  def ofNonterminal(s: Nonterminal, b: Alternatives): Productions =
    Map(s -> b)
  def ofOrMore(t: Alternatives, rep: InternalNonterminal): Productions =
    Map(rep -> (t eachAppend rep incl Seq.empty))

object PartialFollowings:
  def ofNonterminal(s: Nonterminal): PartialFollowings =
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
  def ofNonterminal(s: Nonterminal, p: PartialFollowings): Followings =
    p.mapValues1(_.map(Following(s, _)))
  def ofOrMore(p: PartialFollowings, rep: InternalNonterminal, t: Alternatives): Followings =
    p.mapValues1(_.map(q => Following(rep, q appended rep))) updated (rep, Set(Following(rep, Seq.empty)))
