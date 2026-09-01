package it.unibo.scall
package parser

import lexer.Token
import parser.Parsing.pure

/** One step of analysis: the value produced, the tokens still to be read and the errors met. */
private case class Step[A](value: A, rest: LazyList[Token], errors: Seq[ParseError])

/** Composition of a state, the residual token stream, and an accumulator of errors.
 *  Analysis is written by combining the primitives of the companion object, so that the stream
 *  is threaded and the errors are concatenated by the composition itself.
 */
private case class Parsing[A](run: LazyList[Token] => Step[A]):
  def flatMap[B](next: A => Parsing[B]): Parsing[B] = Parsing: tokens =>
    val step = run(tokens)
    val continuation = next(step.value).run(step.rest)
    continuation.copy(errors = step.errors concat continuation.errors)

  def map[B](f: A => B): Parsing[B] = flatMap(value => pure(f(value)))
  infix def andThen[B](second: => Parsing[B]): Parsing[B] = flatMap(_ => second)

private object Parsing:
  def pure[A](value: A): Parsing[A] = Parsing(Step(value, _, Seq.empty))
  def record(error: ParseError): Parsing[Unit] = Parsing(Step((), _, Seq(error)))
  def peek: Parsing[Option[Token]] = Parsing(tokens => Step(tokens.headOption, tokens, Seq.empty))
  def advance: Parsing[Unit] = Parsing(tokens => Step((), tokens.drop(1), Seq.empty))
