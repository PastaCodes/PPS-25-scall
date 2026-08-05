package it.unibo.scall
package grammar

import Element.*
import ProcessedGrammar.*
import util.CollectionUtils.*

case class ProcessedGrammar(startSymbol: Nonterminal,
                            terminals: Seq[Terminal],
                            productions: Productions)

object ProcessedGrammar:

  case class InternalNonterminal(name: String)
  type AnyNonterminal = Nonterminal | InternalNonterminal
  type AnySymbol = Terminal | AnyNonterminal
  extension (s: AnySymbol)
    def name: String = s match
      case Terminal(name, _, _) => name
      case Nonterminal(name, _) => name
      case InternalNonterminal(name) => name
  type SymbolSeq = Seq[AnySymbol]
  type Alternatives = Set[SymbolSeq]
  type Productions = MultiMap[AnyNonterminal, SymbolSeq]

  case class VisitResult(alternatives: Alternatives,
                         nonterminals: Set[Nonterminal] = Set.empty,
                         productions: Productions = Map.empty)
  
  def of(g: Grammar, startSymbol: Nonterminal): ProcessedGrammar =
    val res = visit(startSymbol)
    ProcessedGrammar(startSymbol, g.terminals, res.productions)
  
  def visit(e: Element, skipNonterminals: Set[Nonterminal] = Set.empty): VisitResult =
    given Set[Nonterminal] = skipNonterminals
    
    e match

      case Eps =>
        VisitResult(Alternatives.ofEps)
      
      case s: Terminal =>
        VisitResult(Alternatives.ofSymbol(s))
      
      case s: Nonterminal =>
        if !skipNonterminals.contains(s) then
          visitUnary(s.rule())(
            alternativesFn      = _ => Alternatives.ofSymbol(s),
            addNonterminals     = Set(s),
            addProductionsFn    = v => Productions.ofNonterminal(s, v.alternatives)
          )(using skipNonterminals = skipNonterminals incl s)
        else
          VisitResult(Alternatives.ofSymbol(s))
      
      case Concat(t1, t2) =>
        visitBinary(t1, t2)(
          alternativesFn = (v1, v2) => Alternatives.ofConcat(v1.alternatives, v2.alternatives)
        )
      
      case Alternation(t1, t2) =>
        visitBinary(t1, t2)(
          alternativesFn = (v1, v2) => Alternatives.ofAlternation(v1.alternatives, v2.alternatives)
        )
      
      case Optional(t) =>
        visitUnary(t)(
          alternativesFn      = v => Alternatives.ofOptional(v.alternatives)
        )
      
      case ZeroOrMore(t) =>
        val repetition = repetitionNonterminal(t)
        visitUnary(t)(
          alternativesFn      = _ => Alternatives.ofZeroOrMore(repetition),
          addProductionsFn    = v => Productions.ofOrMore(v.alternatives, repetition)
        )
      
      case OneOrMore(t) =>
        val repetition = repetitionNonterminal(t)
        visitUnary(t)(
          alternativesFn      = v => Alternatives.ofOneOrMore(v.alternatives, repetition),
          addProductionsFn    = v => Productions.ofOrMore(v.alternatives, repetition)
        )
  
  private def visitUnary(t: Element)
                        (alternativesFn: VisitResult => Alternatives,
                         addTerminals: Set[Terminal] = Set.empty,
                         addNonterminals: Set[Nonterminal] = Set.empty,
                         addProductionsFn: VisitResult => Productions = _ => Map.empty)
                        (using skipNonterminals: Set[Nonterminal]): VisitResult =
    val v = ProcessedGrammar.visit(t, skipNonterminals)
    VisitResult(
      alternativesFn(v),
      v.nonterminals union addNonterminals,
      v.productions unionAll addProductionsFn(v)
    )
  
  private def visitBinary(t1: Element, t2: Element)
                         (alternativesFn: (VisitResult, VisitResult) => Alternatives)
                         (using skipNonterminals: Set[Nonterminal]): VisitResult =
    val v1 = ProcessedGrammar.visit(t1, skipNonterminals)
    val v2 = ProcessedGrammar.visit(t2, skipNonterminals union v1.nonterminals)
    VisitResult(
      alternativesFn(v1, v2),
      v1.nonterminals union v2.nonterminals,
      v1.productions unionAll v2.productions
    )
  
  private def repetitionNonterminal(t: Element): InternalNonterminal =
    InternalNonterminal(name = ZeroOrMore(t).show)

  private object Alternatives:
    def ofEps: Alternatives                                                   = Set(Seq.empty)
    def ofSymbol(s: Symbol): Alternatives                                     = Set(Seq(s))
    def ofConcat(t1: Alternatives, t2: Alternatives): Alternatives            = t1 productConcat t2
    def ofAlternation(t1: Alternatives, t2: Alternatives): Alternatives       = t1 union t2
    def ofOptional(t: Alternatives): Alternatives                             = t incl Seq.empty
    def ofZeroOrMore(rep: InternalNonterminal): Alternatives                  = Set(Seq(rep))
    def ofOneOrMore(t: Alternatives, rep: InternalNonterminal): Alternatives  = t eachAppend rep

  private object Productions:
    def ofNonterminal(s: Nonterminal, b: Alternatives): Productions =
      Map(s -> b)
    def ofOrMore(t: Alternatives, rep: InternalNonterminal): Productions =
      Map(rep -> (t eachAppend rep incl Seq.empty))
