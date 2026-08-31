package it.unibo.scall
package ast

import grammar.ProcessedGrammar.*

enum AstError:
  case DecodingError(message: String)
  case UnexpectedNodeStructure(expected: AnySymbol, actual: CSTNode)
  case AggregateError(errors: Seq[AstError])

object AstError:
  extension (error: AstError)
    def show: String = error match
      case DecodingError(text)         => text
      case UnexpectedNodeStructure(expected, _) => s"unexpected node structure where ${expected.name} was expected"
      case AggregateError(errors)      => errors.map(_.show).mkString("; ")
