package it.unibo.scall
package lexer

case class Position(line: Int, column: Int):
  override def toString: String = s"$line:$column"

object Position:
  extension (pos: Position)
    def advanceBy(text: String): Position =
      text.foldLeft(pos): (current, char) =>
        if char == '\n' then Position(current.line + 1, 1)
        else Position(current.line, current.column + 1)
