package it.unibo.scall
package lexer

import grammar.Element.Terminal

enum Token(val lexeme: String):
  case Valid(terminal: Terminal, value: String) extends Token(value)
  case Error(value: String) extends Token(value)
