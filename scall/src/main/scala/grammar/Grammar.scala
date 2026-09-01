package it.unibo.scall
package grammar

import Element.*
import scala.util.matching.Regex

/** Base type for user-defined grammars: extending it makes the EBNF definition operators
 *  available and keeps track of the declared terminals in order of declaration.
 */
open class Grammar:

  private var _terminals = Vector.empty[Terminal]

  /** Defines a nonterminal, named after the value that introduces it. The body is kept
   * unevaluated, so that a rule may refer to nonterminals declared later or to itself.
   */
  protected def ->(body: => Element)(using name: sourcecode.Name): Nonterminal =
    Nonterminal(name.value, () => body)

  /** Defines a terminal, named after the value that introduces it, from either an exact
   * lexeme or a regular expression. Terminals declared as skipped are discarded by the lexer.
   */
  protected def ->(pattern: String | Regex, skip: Boolean = false)(using name: sourcecode.Name): Terminal =
    val regex = pattern match
      case s: String => Regex.quote(s).r
      case r: Regex  => r
    register(Terminal(name.value, regex, skip))

  private def register(terminal: Terminal): Terminal =
    _terminals :+= terminal
    terminal

  def terminals: Seq[Terminal] = _terminals
