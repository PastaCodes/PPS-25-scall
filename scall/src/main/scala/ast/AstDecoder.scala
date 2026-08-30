package it.unibo.scall
package ast

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

  def decodeRightRecursiveList[A](node: CSTNode)(extractElement: PartialFunction[Seq[CSTNode], (Either[AstError, A], CSTNode)]): Either[AstError, Seq[A]] = node match
    case CSTNode.RuleNode(_, children) =>
      if children.isEmpty then Right(Seq.empty)
      else extractElement.lift(children) match
        case Some((decodedElement, remainingNodes)) =>
          for
            element <- decodedElement
            decodedRest <- decodeRightRecursiveList(remainingNodes)(extractElement)
          yield element +: decodedRest
        case None => Left(AstError.DecodingError("Invalid list structure"))
    case _ => Left(AstError.UnexpectedNode("List rule", node.toString))

  extension (node: CSTNode)
    def as[A](using decoder: AstDecoder[A]): Either[AstError, A] =
      decoder.decode(node)

  extension (nodes: Seq[CSTNode])
    def decodeAll[A](using decoder: AstDecoder[A]): Either[AstError, Seq[A]] =
      val (errors, validNodes) = nodes.partitionMap(_.as[A])
      errors match
        case Seq() => Right(validNodes)
        case Seq(single) => Left(single)
        case multiple => Left(AstError.AggregateError(multiple))
