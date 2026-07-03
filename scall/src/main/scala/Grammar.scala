package it.unibo.scall

import Element.*

trait Grammar:
  protected def ->(body: => Element): Element = Rule(() => body)
  protected def ->(text: String): Element = Terminal(text)
