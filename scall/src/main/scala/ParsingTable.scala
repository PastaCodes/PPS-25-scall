package it.unibo.scall

import Element.Terminal
import ProcessedGrammar.{AnyNonterminal, AnySymbol, SymbolSeq}
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
        val nt = variable("X")
        val t = variable("A")
        val b = variable("B")
        val parseTableGoal = compoundTerm("parsing_cell", nt, t, b)
        parseTableGoal.solveAll.collectSuccess { s => (
          (
            s.getRegistered[AnyNonterminal](nt),
            s.get(t):
              case RegisteredTerminal(t) => t
              case Int(1) => Eof
          ),
          s.getRegisteredList[AnySymbol](b)
        )}.toMap

  private def grammarKnowledge(using g: ProcessedGrammar, scope: RegisterScope) =
    import scala.language.implicitConversions
    given TermConversion[AnySymbol] = register
    val t = g.terminals.filter(!_.isSkipped).map: t =>
      compoundTerm("terminal", t)
    val p = g.productions.mapEntries: (head, body) =>
      compoundTerm("production", head, body)
    val f = g.followings.mapEntries: (nt, f) =>
      compoundTerm("following", nt, f.followingSeq, f.productionHead)
    val s = compoundTerm("start_symbol", g.startSymbol)
    t ++ p ++ f :+ s

  private object RegisteredTerminal:
    def unapply(t: alice.tuprolog.Term)(using scope: RegisterScope): Option[Terminal] =
      Registered.unapply(t)
