package it.unibo.scall

import Element.*

import scala.util.matching.Regex

open class Grammar:

  private var _terminals = Vector.empty[Terminal]

  protected def ->(body: => Element): Nonterminal =
    Nonterminal(() => body)

  protected def ->(regex: Regex): Terminal =
    register(Terminal(regex))

  protected def ->(text: String): Terminal =
    register(Terminal(Regex.quote(text).r))

  private def register(terminal: Terminal): Terminal =
    _terminals :+= terminal
    terminal

  def terminals: Seq[Terminal] = _terminals
