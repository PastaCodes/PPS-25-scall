package it.unibo.scall

import Element.*

import scala.util.matching.Regex

trait Grammar:
  protected def ->(body: => Element): NonTerminal = NonTerminal(() => body)
  protected def ->(regex: Regex): RegexTerminal = RegexTerminal(regex)
  protected def ->(text: String): TextTerminal = TextTerminal(text)
