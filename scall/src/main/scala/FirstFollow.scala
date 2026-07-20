package it.unibo.scall

import Element.{Eps, Terminal}
import util.{*, given}

type TerminalOrEps = Terminal | Eps.type
type FirstSet = Set[TerminalOrEps]
type FirstSets = Map[AnyNonterminal, FirstSet]
case object Eof
type TerminalOrEof = Terminal | Eof.type
type FollowSet = Set[TerminalOrEof]
type FollowSets = Map[AnyNonterminal, FollowSet]
case class FirstFollow(firstSets: FirstSets, followSets: FollowSets)

object FirstFollow:

  private val theoryFile = "/prolog/first_follow.pl"

  private lazy val engine = engineWithTheoryFile(
    getClass.getResourceAsStream(theoryFile)
  )

  def compute(grammar: ProcessedGrammar): FirstFollow =
    given ProcessedGrammar = grammar
    engine.withKnowledge(grammarKnowledge): () =>
      val nt = variable("X")
      val t = variable("A")
      val firstGoal = compoundTerm("first", nt, t)
      val firstSets: FirstSets = engine.solveAll(firstGoal).collectSuccess { s => (
        s.mapBindingAtom(nt)(findNonterminal),
        s.mapBinding(t):
          case Atom(name) => findTerminal(name)
          case Int(0) => Eps
      ): (AnyNonterminal, TerminalOrEps)}.toMultiMap
      val followGoal = compoundTerm("follow", nt, t)
      val followSets = engine.solveAll(followGoal).collectSuccess { s => (
        s.mapBindingAtom(nt)(findNonterminal),
        s.mapBinding(t):
          case Atom(name) => findTerminal(name)
          case Int(1) => Eof
      )}.toMultiMap
      FirstFollow(firstSets, followSets)

  private def grammarKnowledge(using g: ProcessedGrammar) =
    import scala.language.implicitConversions
    val t = g.terminals.filter(!_.isSkipped).map: t =>
      compoundTerm("terminal", t.name)
    val p = g.productions.mapEntries: (head, body) =>
      compoundTerm("production", head.name, body.map(_.name))
    val f = g.followings.mapEntries: (nt, f) =>
      compoundTerm("following", nt.name, f.followingSeq.map(_.name), f.productionHead.name)
    val s = compoundTerm("start_symbol", g.startSymbol.name)
    t ++ p ++ f :+ s

  private def findTerminal(name: String)(using g: ProcessedGrammar) =
    g.terminals.find(_.name == name).get

  private def findNonterminal(name: String)(using g: ProcessedGrammar) =
    g.productions.keySet.find(_.name == name).get
