package it.unibo.scall

import Element.*


import scala.util.matching.Regex

open class Grammar:

  private var _terminals = Vector.empty[Terminal]

  protected def ->(body: => Element): Nonterminal =
    Nonterminal(() => body)

  protected def ->(regex: Regex)(using name: sourcecode.Name): Terminal =
    register(Terminal(name.value, regex))

  protected def ->(text: String)(using name: sourcecode.Name): Terminal =
    register(Terminal(name.value, Regex.quote(text).r))

  private def register(terminal: Terminal): Terminal =
    _terminals :+= terminal
    terminal

  def terminals: Seq[Terminal] = _terminals
