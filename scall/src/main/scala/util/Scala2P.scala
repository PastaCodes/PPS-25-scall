package it.unibo.scall
package util

import alice.tuprolog.*
import alice.tuprolog.exceptions.InvalidObjectIdException
import alice.tuprolog.lib.OOLibrary

import java.io.InputStream
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.reflect.ClassTag

case class Scala2P private (private val inner: Prolog):
  private lazy val ooLibrary = inner.getLibrary("alice.tuprolog.lib.OOLibrary").asInstanceOf[OOLibrary]

object Scala2P:

  def engineWithTheoryFile(source: InputStream): Scala2P =
    val inner = Prolog()
    val theory = Theory.parseLazilyWithStandardOperators(source)
    inner.setTheory(theory)
    Scala2P(inner)

  extension (engine: Scala2P)
    def withKnowledge[R](knowledge: Iterable[Term])(action: () => R): R =
      val theory = engine.inner.getTheory
      engine.inner.addTheory(Theory.of(knowledge.asJavaCollection))
      val result = action()
      engine.inner.setTheory(theory)
      result
    def solveAll(goal: Term): Iterable[SolveInfo] =
      Iterable.single(engine.inner.solve(goal)) ++ new Iterable[SolveInfo]:
        override def iterator: Iterator[SolveInfo] = new Iterator[SolveInfo]:
          override def hasNext: Boolean = engine.inner.hasOpenAlternatives
          override def next(): SolveInfo = engine.inner.solveNext

  extension (solutions: Iterable[SolveInfo])
    def collectSuccess[R](mapper: SolveInfo => R): Iterable[R] =
      solutions.collect { case s if s.isSuccess => mapper(s) }

  type TermConversion[A] = Conversion[A, Term]
  given [A](using itemConv: TermConversion[A]): TermConversion[Iterable[A]] =
    i => Struct.list(i.map(itemConv).asJava)
  def compoundTerm(functor: String, args: Term*): Struct = Struct.of(functor, args.toArray)
  def variable(name: String): Var = Var.of(name)
  def register(obj: AnyRef)(using engine: Scala2P): Struct =
    engine.ooLibrary.register(obj)

  extension (solution: SolveInfo)
    def getRegistered[A](variable: Var)(using engine: Scala2P): A =
      val value = solution.getVarValue(variable.getName)
      val id = value.asInstanceOf[Struct]
      engine.ooLibrary.getRegisteredObject(id).asInstanceOf[A]
    def get[A](variable: Var)(extractor: Term => A): A =
      extractor(solution.getVarValue(variable.getName))

  object Registered:
    def unapply[A](t: Term)(using engine: Scala2P, ct: ClassTag[A]): Option[A] =
      t match
        case id: Struct =>
          val value = try engine.ooLibrary.getRegisteredObject(id).nn
                      catch case _: (InvalidObjectIdException | NullPointerException) => return None
          if ct.runtimeClass.isInstance(value) then Some(value.asInstanceOf[A]) else None
        case _ => None
  object Int:
    def unapply(t: Term): Option[scala.Int] =
      t match
        case i: alice.tuprolog.Int => Some(i.intValue())
        case _ => None
