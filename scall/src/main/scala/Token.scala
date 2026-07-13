package it.unibo.scall

import Element.Terminal

enum Token(val lexeme: String):
  case Valid(terminal: Terminal, value: String) extends Token(value)
  case Error(value: String) extends Token(value)