package it.unibo.scall
package grammar

import Element.*
import ProcessedGrammar.*
import util.CollectionUtils.*

/** Representation of a Context-Free Grammar (CFG), which is defined by
 *  a start symbol, a collection of declared terminals, and a collection of declared productions.
 *  This form is better suited for the LL(1) algorithm and is obtained by a process of conversion
 *  from an [[Grammar EBNF grammar]] as described in [[ProcessedGrammar.of]].
 */
case class ProcessedGrammar(startSymbol: Nonterminal,
                            terminals: Seq[Terminal],
                            productions: Productions)

object ProcessedGrammar:

  /** A nonterminal which is not derived from the input grammar directly, rather it is added
   *  to the final grammar as a means to implement non-trivial features of the input grammar.
   *  Specifically, it can be the result of a repetition operator ([[Element.`*` *]] or [[Element.+ +]])
   *  or of the process of left factoring. See [[ProcessedGrammar.of]] for further details.
   */
  case class InternalNonterminal(name: String)

  type AnyNonterminal = Nonterminal | InternalNonterminal
  type AnySymbol = Terminal | AnyNonterminal
  type SymbolSeq = Seq[AnySymbol]
  type Alternatives = Set[SymbolSeq]
  type Productions = MultiMap[AnyNonterminal, SymbolSeq]

  case class VisitResult(alternatives: Alternatives,
                         nonterminals: Set[Nonterminal] = Set.empty,
                         productions: Productions = Map.empty)

  /** Performs a recursive traversal of the tree-like EBNF grammar and converts it
   *  to a CFG grammar. Each kind of [[Element production element]] is handled differently
   *  and may generate productions for the final grammar. Each scenario is described in the test classes
   *  (`ProcessedGrammarAlternativesTest` and `ProcessedGrammarProductionsTest`).
   */
  def of(g: Grammar, startSymbol: Nonterminal): ProcessedGrammar =
    val res = visit(startSymbol)
    ProcessedGrammar(startSymbol, g.terminals, res.productions)
  
  def visit(e: Element)(using skip: Set[Nonterminal] = Set.empty): VisitResult = e match

      case Eps =>
        VisitResult(Alternatives.ofEps)
      
      case s: Terminal =>
        VisitResult(Alternatives.ofSymbol(s))
      
      case s: Nonterminal =>
        if !skip(s) then
          visitUnary(s.rule())(
            alternativesFn      = _ => Alternatives.ofSymbol(s),
            addNonterminals     = Set(s),
            addProductionsFn    = v => Productions.ofNonterminal(s, v.alternatives)
          )(using skip incl s)
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
                        (using skip: Set[Nonterminal]): VisitResult =
    val v = ProcessedGrammar.visit(t)
    VisitResult(
      alternativesFn(v),
      v.nonterminals union addNonterminals,
      v.productions unionAll addProductionsFn(v)
    )
  
  private def visitBinary(t1: Element, t2: Element)
                         (alternativesFn: (VisitResult, VisitResult) => Alternatives)
                         (using skip: Set[Nonterminal]): VisitResult =
    val v1 = ProcessedGrammar.visit(t1)(using skip)
    val v2 = ProcessedGrammar.visit(t2)(using skip union v1.nonterminals)
    VisitResult(
      alternativesFn(v1, v2),
      v1.nonterminals union v2.nonterminals,
      v1.productions unionAll v2.productions
    )

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
      leftFactor(s, b)
    def ofOrMore(t: Alternatives, rep: InternalNonterminal): Productions =
      leftFactor(rep, t eachAppend rep incl Seq.empty)

  private def repetitionNonterminal(t: Element): InternalNonterminal =
    InternalNonterminal(name = ZeroOrMore(t).show)

  private def factoringNonterminal(b: Alternatives): InternalNonterminal =
    InternalNonterminal(name = s"(${b.show})")

  def longestCommonPrefix(first: SymbolSeq, second: SymbolSeq): (SymbolSeq, SymbolSeq, SymbolSeq) =
    val prefix = (first zip second).takeWhile(_ == _).map(_._1)
    (prefix, first.drop(prefix.size), second.drop(prefix.size))

  def prefixed(alternatives: Alternatives): Map[SymbolSeq, Alternatives] =
    alternatives.foldLeft(Map.empty): (prefixed, alternative) =>
      prefixed.keys.to(LazyList)
        .map(prefix => prefix -> longestCommonPrefix(prefix, alternative))
        .find:
          case (_, (common, _, _)) => common.nonEmpty
      match
        case Some(prefix, (common, prefixSuffix, altSuffix)) =>
          val newSuffixes = prefixed(prefix).map(prefixSuffix ++ _) incl altSuffix
          prefixed - prefix + (common -> newSuffixes)
        case None =>
          prefixed + (alternative -> Set(Seq.empty))

  def leftFactor(b: Alternatives): (Alternatives, Productions) =
    ProcessedGrammar.prefixed(b)
      .foldLeft[(Alternatives, Productions)]((Set.empty, Map.empty)):
        case ((alternatives, productions), (prefix, suffixes)) =>
          if suffixes.size == 1 then
            (alternatives + prefix, productions)
          else
            val (factoredSuffixes, innerProductions) = leftFactor(suffixes)
            val fact = factoringNonterminal(factoredSuffixes)
            val newProductions = innerProductions + (fact -> factoredSuffixes)
            (alternatives + (prefix :+ fact), productions ++ newProductions)

  def leftFactor(s: AnyNonterminal, b: Alternatives): Productions =
    val (f, p) = leftFactor(b)
    p + (s -> f)

  extension (s: AnySymbol)
    def name: String = s match
      case Terminal(name, _, _) => name
      case Nonterminal(name, _) => name
      case InternalNonterminal(name) => name
  extension (s: SymbolSeq)
    def show: String = if s.isEmpty then "\u03b5" else s.map(_.name).mkString(" ")
  extension (a: Alternatives)
    def show: String = a.map(_.show).mkString(" | ")
  extension (p: Productions)
    def show: Iterable[String] = p.mapEntries { (head, body) => s"${head.name} \u27f6 ${body.show}" }
