package it.unibo.scall

import Element.{Eps, Terminal}
import util.{*, given}

type TerminalOrEps = Terminal | Eps.type
type FirstSet = Set[TerminalOrEps]
type FirstSets = Map[AnyNonterminal, FirstSet]
type FollowSet = Set[Terminal]
type FollowSets = Map[AnyNonterminal, FollowSet]
case class FirstFollow(firstSets: FirstSets, followSets: FollowSets)

object FirstFollow:

  private val theoryFile = "/prolog/first_follow.pl"

  private lazy val engine = engineWithTheoryFile(
    getClass.getResourceAsStream(theoryFile)
  )

  def compute(grammar: ProcessedGrammar): FirstFollow =
    given ProcessedGrammar = grammar
    engine.withKnowledge(terminalsKnowledge ++ productionsKnowledge ++ followingsKnowledge): () =>
      val nt = variable("X")
      val t = variable("A")
      val firstGoal = compoundTerm("first", nt, t)
      val firstSets: FirstSets = engine.solveAll(firstGoal).collectSuccess { s => (
        s.mapBindingAtom(nt)(findNonterminal),
        s.mapBinding(t):
          case Atom(name) => findTerminal(name)
          case EmptyList() => Eps
      ): (AnyNonterminal, TerminalOrEps)}.toMultiMap
      val followGoal = compoundTerm("follow", nt, t)
      val followSets = engine.solveAll(followGoal).collectSuccess { s => (
        s.mapBindingAtom(nt)(findNonterminal),
        s.mapBindingAtom(t)(findTerminal)
      )}.toMultiMap
      FirstFollow(firstSets, followSets)

  private def terminalsKnowledge(using g: ProcessedGrammar) =
    g.terminals.map: t =>
      compoundTerm("terminal", t.name)

  private def productionsKnowledge(using g: ProcessedGrammar) =
    g.productions.mapEntries: (head, body) =>
      compoundTerm("production", head.name, body.map(_.name))

  private def followingsKnowledge(using g: ProcessedGrammar) =
    g.followings.mapEntries: (nt, f) =>
      compoundTerm("following", nt.name, f.followingSeq.map(_.name), f.productionHead.name)

  private def findTerminal(name: String)(using g: ProcessedGrammar) =
    g.terminals.find(_.name == name).get

  private def findNonterminal(name: String)(using g: ProcessedGrammar) =
    g.productions.keySet.find(_.name == name).get
