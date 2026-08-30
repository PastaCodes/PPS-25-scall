package it.unibo.scall
package ast

enum AstError:
  case DecodingError(message: String)
  case UnexpectedNode(expected: String, actual: String)
  case AggregateError(errors: Seq[AstError])

object AstError:
  extension (error: AstError)
    def show: String = error match
      case DecodingError(text)         => text
      case UnexpectedNode(expected, _) => s"unexpected node where $expected was expected"
      case AggregateError(errors)      => errors.map(_.show).mkString("; ")
