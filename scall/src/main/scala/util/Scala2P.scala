package it.unibo.scall
package util

import alice.tuprolog.*

import java.io.InputStream
import scala.jdk.CollectionConverters.IterableHasAsJava

def engineWithTheoryFile(source: InputStream): Prolog =
  val engine = Prolog()
  val theory = Theory.parseLazilyWithStandardOperators(source)
  engine.setTheory(theory)
  engine

extension (engine: Prolog)
  def withKnowledge[R](knowledge: Iterable[Term])(action: () => R): R =
    val theory = engine.getTheory
    engine.addTheory(Theory.of(knowledge.asJavaCollection))
    val result = action()
    engine.setTheory(theory)
    result
  def solveAll(goal: Term): Iterable[SolveInfo] =
    Iterable.single(engine.solve(goal)) ++ new Iterable[SolveInfo]:
      override def iterator: Iterator[SolveInfo] = new Iterator[SolveInfo]:
        override def hasNext: Boolean = engine.hasOpenAlternatives
        override def next(): SolveInfo = engine.solveNext

extension (solutions: Iterable[SolveInfo])
  def collectSuccess[R](mapper: SolveInfo => R): Iterable[R] =
    solutions.collect { case s if s.isSuccess => mapper(s) }

given Conversion[String, Term] = Struct.atom(_)
given [A](using itemConv: Conversion[A, Term]): Conversion[Iterable[A], Term] =
  i => Struct.list(i.map(itemConv).asJava)
def compoundTerm(functor: String, args: Term*): Struct = Struct.of(functor, args.toArray)
def variable(name: String): Var = Var.of(name)

extension (solution: SolveInfo)
  def mapBinding[B](variable: Var)(mapper: PartialFunction[Term, B]): B =
    mapper(solution.getVarValue(variable.getName))
  def mapBindingAtom[B](variable: Var)(mapper: String => B): B =
    mapper(solution.getVarValue(variable.getName).asInstanceOf[Struct].getName)

object Atom:
  def unapply(t: Term): Option[String] =
    t match
      case s: Struct if s.getName != null && s.getArity == 0 && s.getName != "[]" => Some(s.getName)
      case _ => None
object EmptyList:
  def unapply(t: Term): Boolean =
    t match
      case s: Struct if s.getName == "[]" && s.getArity == 0 => true
      case _ => false
