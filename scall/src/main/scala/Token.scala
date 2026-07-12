package it.unibo.scall

import Element.Terminal

enum Token(val lexeme: String):
  case Valid(terminal: Terminal, value: String) extends Token(value)
  case Error(value: String) extends Token(value)

  def terminalOpt: Option[Terminal] = this match
    case Valid(t, _) => Some(t)
    case Error(_) => None