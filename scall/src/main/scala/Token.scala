package it.unibo.scall

import Element.Terminal

enum Token:
  case Valid(terminal: Terminal, value: String)
  case Error(value: String)

  def lexeme: String = this match
    case Valid(_, t) => t
    case Error(t) => t

  def terminalOpt: Option[Terminal] = this match
    case Valid(t, _) => Some(t)
    case Error(_) => None
