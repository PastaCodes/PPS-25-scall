package it.unibo.scall
package lexer

import grammar.Element.Terminal

/** Represents a lexical unit extracted from the source code, retaining its original `lexeme` and `position`. */
enum Token(val lexeme: String, val position: Position):
  case Valid(terminal: Terminal, value: String, pos: Position) extends Token(value, pos)
  case Error(value: String, pos: Position) extends Token(value, pos)
