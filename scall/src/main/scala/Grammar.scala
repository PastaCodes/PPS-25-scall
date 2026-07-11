package it.unibo.scall

import Element.*

import scala.util.matching.Regex

open class Grammar:

  private var _terminals = Set.empty[Terminal]

  protected def ->(body: => Element): Nonterminal =
    Nonterminal(() => body)

  protected def ->(regex: Regex): RegexTerminal =
    val s: RegexTerminal = RegexTerminal(regex.regex)
    this._terminals += s
    s

  protected def ->(text: String): TextTerminal =
    val s: TextTerminal = TextTerminal(text)
    this._terminals += s
    s

  def terminals: Set[Terminal] = _terminals
