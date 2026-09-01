package it.unibo.scall
package parser

import grammar.Element.{TerminalOrEoi, Eoi}
import grammar.Element.Terminal
import grammar.ProcessedGrammar
import grammar.ProcessedGrammar.{AnyNonterminal, AnySymbol, SymbolSeq}
import util.CollectionUtils.mapEntries
import util.Scala2P
import util.Scala2P.{*, given}

/** Representation of an LL(1) [[ParsingTable parsing table]].
 *  Cells are identified by a pair consisting of a nonterminal, which is the current parsing state,
 *  and a terminal, which is read from input, or possibly an [[Eoi end of input]].
 *  Values within the table are production bodies.
 *  A parsing table can be computed from a [[ProcessedGrammar]] by means of the [[compute]] method,
 *  which makes use of a [[Scala2P tuProlog engine]] and the theory from `/prolog/parsing_table.pl`.
 */
object ParsingTable:

  type ParsingTable = Map[(AnyNonterminal, TerminalOrEoi), SymbolSeq]

  private val theoryFile = "/prolog/parsing_table.pl"

  private lazy val engine = engineWithTheoryFile(
    getClass.getResourceAsStream(theoryFile)
  )
  given Scala2P = engine

  /** Computes a parsing table from the given grammar.
   *  The algorithm is implemented in prolog (see `/prolog/parsing_table.pl`),
   *  by computing the FIRST and FOLLOW sets for each nonterminal and production body.
   *  If the supplied grammar is not LL(1), the result is undefined.
   */
  def compute(grammar: ProcessedGrammar): ParsingTable =
    registerScope:
      withKnowledge(grammarKnowledge(grammar)): () =>
        val X = variable("X"); val A = variable("A"); val B = variable("B")
        val parsingTableGoal = compoundTerm("parsing_table_cell", X, A, B)
        parsingTableGoal.solveAll.collectSuccess: s =>
          val row = s.getRegistered[AnyNonterminal](X)
          val col = s.get(A):
            case Registered[Terminal](t) => t
            case Int(1) => Eoi
          val value = s.getRegisteredList[AnySymbol](B)
          (row, col) -> value
        .toMap

  private def grammarKnowledge(g: ProcessedGrammar)(using scope: RegisterScope) =
    import scala.language.implicitConversions
    given TermConversion[AnySymbol] = register
    val t = g.terminals.filter(!_.isSkipped).map: t =>
      compoundTerm("terminal", t)
    val p = g.productions.mapEntries: (head, body) =>
      compoundTerm("production", head, body)
    val s = compoundTerm("start_symbol", g.startSymbol)
    t ++ p :+ s
