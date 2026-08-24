package it.unibo.scall
package lexer

import grammar.Element.Terminal

case class Position(line: Int, column: Int):
  override def toString: String = s"$line:$column"

enum Token(val lexeme: String, val position: Position):
  case Valid(terminal: Terminal, value: String, pos: Position) extends Token(value, pos)
  case Error(value: String, pos: Position) extends Token(value, pos)
