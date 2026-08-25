package it.unibo.scall
package lexer

import grammar.Element.Terminal

enum Token(val lexeme: String, val position: Position):
  case Valid(terminal: Terminal, value: String, pos: Position) extends Token(value, pos)
  case Error(value: String, pos: Position) extends Token(value, pos)
