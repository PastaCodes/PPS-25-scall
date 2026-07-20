package it.unibo.scall

import Element.{Eps, Terminal}
import ProcessedGrammar.{AnyNonterminal, AnySymbol}
import util.CollectionUtils.{mapEntries, toMultiMap}
import util.Scala2P
import util.Scala2P.{*, given}

object FirstFollow:

  type TerminalOrEps = Terminal | Eps.type
  type FirstSet = Set[TerminalOrEps]
  type FirstSets = Map[AnyNonterminal, FirstSet]
  case object Eof
  type TerminalOrEof = Terminal | Eof.type
  type FollowSet = Set[TerminalOrEof]
  type FollowSets = Map[AnyNonterminal, FollowSet]
  case class FirstFollow(firstSets: FirstSets, followSets: FollowSets)

  private val theoryFile = "/prolog/first_follow.pl"

  private lazy val engine = engineWithTheoryFile(
    getClass.getResourceAsStream(theoryFile)
  )
  given Scala2P = engine

  def compute(grammar: ProcessedGrammar): FirstFollow =
    given ProcessedGrammar = grammar
    engine.withKnowledge(grammarKnowledge): () =>
      val nt = variable("X")
      val t = variable("A")
      val firstGoal = compoundTerm("first", nt, t)
      val firstSets: FirstSets = engine.solveAll(firstGoal).collectSuccess[(AnyNonterminal, TerminalOrEps)] { s => (
        s.getRegistered[AnyNonterminal](nt),
        s.get(t):
          case RegisteredTerminal(t) => t
          case Int(0) => Eps
      )}.toMultiMap
      val followGoal = compoundTerm("follow", nt, t)
      val followSets: FollowSets = engine.solveAll(followGoal).collectSuccess { s => (
        s.getRegistered[AnyNonterminal](nt),
        s.get(t):
          case RegisteredTerminal(t) => t
          case Int(1) => Eof
      )}.toMultiMap
      FirstFollow(firstSets, followSets)

  private def grammarKnowledge(using g: ProcessedGrammar) =
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
    def unapply(t: alice.tuprolog.Term): Option[Terminal] =
      Registered.unapply(t)
