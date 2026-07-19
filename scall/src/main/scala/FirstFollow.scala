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

  def compute(terminals: Iterable[Terminal],
              nonterminals: Iterable[AnyNonterminal],
              productions: Productions): FirstFollow =
    val terminalsKnowledge = terminals.map: t =>
      compoundTerm("terminal", t.name)
    val productionsKnowledge = productions.mapEntries: (head, body) =>
      compoundTerm("production", head.name, body.map(_.name))
    engine.withKnowledge(terminalsKnowledge ++ productionsKnowledge): () =>
      val nt = variable("X")
      val first = variable("A")
      val firstGoal = compoundTerm("first", nt, first)
      val firstSets: FirstSets = engine.solveAll(firstGoal)
        .collect[(AnyNonterminal, TerminalOrEps)]:
          case s if s.isSuccess => (
            s.mapBinding(nt):
              case Atom(name) => nonterminals.find(_.name == name).get,
            s.mapBinding(first):
              case Atom(name) => terminals.find(_.name == name).get
              case EmptyList() => Eps
          )
        .toMultiMap
      FirstFollow(firstSets, null)

  def compute(grammar: ProcessedGrammar): FirstFollow =
    compute(grammar.terminals, grammar.productions.keySet, grammar.productions)
