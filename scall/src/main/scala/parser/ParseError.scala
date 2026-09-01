package it.unibo.scall
package parser

import grammar.Element.TerminalOrEoi
import lexer.{Position, Token}

/** An error met during syntactical analysis. Lexical errors are reported here as well,
 *  so that a client has a single collection to inspect.
 */
enum ParseError:
  case UnexpectedToken(expected: Set[TerminalOrEoi], found: Token.Valid)
  case UnexpectedEndOfInput(expected: Set[TerminalOrEoi])
  case LexicalError(token: Token.Error)
  case TrailingInput(token: Token)

object ParseError:

  extension (error: ParseError)
    /** Where in the source the error was met, or nothing when the input ended prematurely. */
    def position: Option[Position] = error match
      case UnexpectedToken(_, found) => Some(found.position)
      case UnexpectedEndOfInput(_) => None
      case LexicalError(token) => Some(token.position)
      case TrailingInput(token) => Some(token.position)

    /** A description of this error, meant to be shown to the author of the analysed source. */
    def show: String = error match
      case UnexpectedToken(expected, found) =>
        s"unexpected '${found.lexeme}', expected ${listing(expected)}"
      case UnexpectedEndOfInput(expected) =>
        s"unexpected end of input, expected ${listing(expected)}"
      case LexicalError(token) =>
        s"'${token.lexeme}' is not a valid character"
      case TrailingInput(token) =>
        s"unexpected '${token.lexeme}' after the end of the program"

  private def listing(expected: Set[TerminalOrEoi]): String =
    expected.map(_.name).toSeq.sorted.mkString(", ")
