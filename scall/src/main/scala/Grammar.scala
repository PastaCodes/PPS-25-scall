package it.unibo.scall

import Element.*
import scala.util.matching.Regex

open class Grammar:

  private var _terminals = Vector.empty[Terminal]

  protected def ->(body: => Element): Nonterminal =
    Nonterminal(() => body)

  protected def ->(pattern: String | Regex, skip: Boolean = false): Terminal =
    val regex = pattern match
      case s: String => Regex.quote(s).r
      case r: Regex  => r
    register(Terminal(regex, skip))

  private def register(terminal: Terminal): Terminal =
    _terminals :+= terminal
    terminal

  def terminals: Seq[Terminal] = _terminals