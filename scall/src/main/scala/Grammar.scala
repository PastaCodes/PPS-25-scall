package it.unibo.scall

import Element.*

trait Grammar:
  protected def ->(body: => Element): Rule = Rule(() => body)
  protected def ->(text: String): Terminal = Terminal(text)
