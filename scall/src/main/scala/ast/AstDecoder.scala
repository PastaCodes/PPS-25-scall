package it.unibo.scall
package ast

enum AstError:
  case DecodingError(message: String)
  case UnexpectedNode(expected: String, actual: String)
  case AggregateError(errors: Seq[AstError])

/** A type class defining how to translate a generic [[CSTNode]] into a
 *  strongly-typed Abstract Syntax Tree (AST) node of type `A`.
 */
trait AstDecoder[A]:
  self =>
  def decode(node: CSTNode): Either[AstError, A]

  def map[B](f: A => B): AstDecoder[B] = node =>
    self.decode(node).map(f)

  def flatMap[B](f: A => AstDecoder[B]): AstDecoder[B] = node =>
    self.decode(node).flatMap(a => f(a).decode(node))

  def orElse[B >: A](fallback: => AstDecoder[B]): AstDecoder[B] = node =>
    self.decode(node)
      .orElse(fallback.decode(node))

object AstDecoder:

  def pure[A](value: A): AstDecoder[A] = _ => Right(value)

  def fail[A](error: AstError): AstDecoder[A] = _ => Left(error)

  /** Decodes a flat sequence of [[CSTNode]]s by repeatedly applying a partial function to extract chunks.
   * Useful for resolving flat LL(1) sequences into typed AST collections.
   */
  def decodeSequence[A](nodes: Seq[CSTNode])(extractChunk: PartialFunction[Seq[CSTNode], (Either[AstError, A], Seq[CSTNode])]): Either[AstError, Seq[A]] =
    if nodes.isEmpty then Right(Seq.empty)
    else extractChunk.lift(nodes) match
      case Some((decodedElement, remainingNodes)) =>
        for
          element <- decodedElement
          decodedRest <- decodeSequence(remainingNodes)(extractChunk)
        yield element +: decodedRest
      case None => Left(AstError.DecodingError("Invalid sequence structure"))

  extension (node: CSTNode)
    /** Triggers the decoding of a [[CSTNode]] into type `A` using an available [[AstDecoder]] in scope. */
    def as[A](using decoder: AstDecoder[A]): Either[AstError, A] =
      decoder.decode(node)

  extension (nodes: Seq[CSTNode])
    def decodeAll[A](using decoder: AstDecoder[A]): Either[AstError, Seq[A]] =
      val (errors, validNodes) = nodes.partitionMap(_.as[A])
      errors match
        case Seq() => Right(validNodes)
        case Seq(single) => Left(single)
        case multiple => Left(AstError.AggregateError(multiple))
