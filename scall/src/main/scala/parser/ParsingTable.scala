package it.unibo.scall
package parser

import grammar.Element.Terminal
import grammar.ProcessedGrammar
import grammar.ProcessedGrammar.{AnyNonterminal, AnySymbol, SymbolSeq}
import util.CollectionUtils.mapEntries
import util.Scala2P
import util.Scala2P.{*, given}

object ParsingTable:

  case object Eof
  type TerminalOrEof = Terminal | Eof.type
  type ParsingTable = Map[(AnyNonterminal, TerminalOrEof), SymbolSeq]

  private val theoryFile = "/prolog/parsing_table.pl"

  private lazy val engine = engineWithTheoryFile(
    getClass.getResourceAsStream(theoryFile)
  )
  given Scala2P = engine

  def compute(grammar: ProcessedGrammar): ParsingTable =
    given ProcessedGrammar = grammar
    registerScope:
      withKnowledge(grammarKnowledge): () =>
        val X = variable("X"); val A = variable("A"); val B = variable("B")
        val parsingTableGoal = compoundTerm("parsing_cell", X, A, B)
        parsingTableGoal.solveAll.collectSuccess { s => (
          (
            s.getRegistered[AnyNonterminal](X),
            s.get(A):
              case RegisteredTerminal(t) => t
              case Int(1) => Eof
          ),
          s.getRegisteredList[AnySymbol](B)
        )}.toMap

  private def grammarKnowledge(using g: ProcessedGrammar, scope: RegisterScope) =
    import scala.language.implicitConversions
    given TermConversion[AnySymbol] = register
    val t = g.terminals.filter(!_.isSkipped).map: t =>
      compoundTerm("terminal", t)
    val p = g.productions.mapEntries: (head, body) =>
      compoundTerm("production", head, body)
    val s = compoundTerm("start_symbol", g.startSymbol)
    t ++ p :+ s

  private object RegisteredTerminal:
    def unapply(t: alice.tuprolog.Term)(using scope: RegisterScope): Option[Terminal] =
      Registered.unapply(t)
