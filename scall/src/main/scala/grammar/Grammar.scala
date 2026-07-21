package it.unibo.scall
package grammar

import Element.*
import scala.util.matching.Regex

open class Grammar:

  private var _terminals = Vector.empty[Terminal]

  protected def ->(body: => Element)(using name: sourcecode.Name): Nonterminal =
    Nonterminal(name.value, () => body)

  protected def ->(pattern: String | Regex, skip: Boolean = false)(using name: sourcecode.Name): Terminal =
    val regex = pattern match
      case s: String => Regex.quote(s).r
      case r: Regex  => r
    register(Terminal(name.value, regex, skip))

  private def register(terminal: Terminal): Terminal =
    _terminals :+= terminal
    terminal

  def terminals: Seq[Terminal] = _terminals
