package it.unibo.scall
package util

import alice.tuprolog.exceptions.InvalidObjectIdException
import alice.tuprolog.lib.OOLibrary
import alice.tuprolog.{Int as _, *}

import java.io.InputStream
import scala.jdk.CollectionConverters.{IterableHasAsJava, IteratorHasAsScala}
import scala.reflect.ClassTag

/** Functional-style bindings for an underlying tuProlog engine, with facilitated term conversion
 *  and strong scoping for registered objects. */
case class Scala2P private (private val inner: Prolog):
  private lazy val ooLibrary = inner.getLibrary("alice.tuprolog.lib.OOLibrary").asInstanceOf[OOLibrary]

object Scala2P:

  /** Creates a new prolog engine with the provided theory.
   *  The source is closed before returning. */
  def engineWithTheoryFile(source: InputStream): Scala2P =
    val inner = Prolog()
    val theory = try Theory.parseLazilyWithStandardOperators(source) finally source.close()
    inner.setTheory(theory)
    Scala2P(inner)

  /** Runs a scoped action, extending the engine's current theory with the specified knowledge base
   *  for the duration of the action, reverting to the original theory upon completion.
   *  Nesting is allowed. */
  def withKnowledge[R](knowledge: Iterable[Term])(action: () => R)(using engine: Scala2P): R =
    val theory = engine.inner.getTheory
    engine.inner.addTheory(Theory.of(knowledge.asJavaCollection))
    val result = action()
    engine.inner.setTheory(theory)
    result

  extension (goal: Term)(using engine: Scala2P)
    /** Returns all solutions to the specified goal as an iterable.
     *  Unsuccessful ("No.") solutions are also included.
     *  Consider using [[collectSuccess]] to handle successful solutions only. */
    def solveAll: Iterable[SolveInfo] =
      Iterable.single(engine.inner.solve(goal)) ++ new Iterable[SolveInfo]:
        override def iterator: Iterator[SolveInfo] = new Iterator[SolveInfo]:
          override def hasNext: Boolean = engine.inner.hasOpenAlternatives
          override def next(): SolveInfo = engine.inner.solveNext

  extension (solutions: Iterable[SolveInfo])
    /** Applies the given function to all successful solutions. */
    def collectSuccess[R](mapper: SolveInfo => R): Iterable[R] =
      solutions.collect { case s if s.isSuccess => mapper(s) }

  type TermConversion[A] = Conversion[A, Term]
  given [A](using itemConv: TermConversion[A]): TermConversion[Iterable[A]] =
    i => Struct.list(i.map(itemConv).asJava)
  def compoundTerm(functor: String, args: Term*): Struct = Struct.of(functor, args.toArray)
  def variable(name: String): Var = Var.of(name)

  case class RegisterScope private[Scala2P] (engine: Scala2P)

  /** Runs a scoped action within a context that allows for objects to be registered and retrieved.
   *  All objects that are registered within this scope are unregistered before returning.
   *  Nesting register scopes is not supported. */
  def registerScope[A](f: RegisterScope ?=> A)(using engine: Scala2P): A =
    try f(using RegisterScope(engine))
    finally engine.ooLibrary.dismissAll()
  def register(obj: AnyRef)(using scope: RegisterScope): Struct =
    scope.engine.ooLibrary.register(obj)

  extension (solution: SolveInfo)
    /** Returns the registered object with type A that is bound to the specified variable.
     *  Error cases are ignored. */
    def getRegistered[A](variable: Var)(using scope: RegisterScope): A =
      val value = solution.getVarValue(variable.getName)
      val id = value.asInstanceOf[Struct]
      scope.engine.ooLibrary.getRegisteredObject(id).asInstanceOf[A]
    /** Retrieves the list that is bound to the specified variable and returns it as a collection of
     *  registered objects with type A. Error cases are ignored. */
    def getRegisteredList[A](variable: Var)(using scope: RegisterScope): Seq[A] =
      val value = solution.getVarValue(variable.getName)
      val list = value.asInstanceOf[Struct]
      list.listIterator().asScala.map(item =>
        val id = item.asInstanceOf[Struct]
        scope.engine.ooLibrary.getRegisteredObject(id).asInstanceOf[A]
      ).toSeq
    /** Retrieves the value that is bound to the specified variable and uses the provided extractor
     *  to convert it to a value of type A. Such extractors include [[Registered.unapply]]
     *  and [[Int.unapply]] and can be combined if the bound value can have one of several types. */
    def get[A](variable: Var)(extractor: Term => A): A =
      extractor(solution.getVarValue(variable.getName))

  object Registered:
    /** Finds whether the given term corresponds to a registered object of type A
     *  and returns it if it is. Useful when combined with other extractors inside [[get]]. */
    def unapply[A](t: Term)(using scope: RegisterScope, ct: ClassTag[A]): Option[A] = t match
      case id: Struct =>
        val value = try scope.engine.ooLibrary.getRegisteredObject(id).nn
                    catch case _: (InvalidObjectIdException | NullPointerException) => return None
        if ct.runtimeClass.isInstance(value) then Some(value.asInstanceOf[A]) else None
      case _ => None
  object Int:
    /** Finds whether the given term corresponds to an integer value and returns it if it is.
     *  Useful when combined with other extractors inside [[get]]. */
    def unapply(t: Term): Option[Int] = t match
      case i: alice.tuprolog.Int => Some(i.intValue())
      case _ => None
