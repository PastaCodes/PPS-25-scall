package it.unibo.scall
package lexer

import grammar.Element.Terminal
import scala.annotation.tailrec

private[lexer] case class Cursor(offset: Int, pos: Position):
  def advance(text: String): Cursor =
    Cursor(offset + text.length, pos.advanceBy(text))

/** A functional lexical analyzer that translates a raw input string into a lazy stream
 *  of [[Token]]s based on a given sequence of grammar [[Terminal]]s.
 */
class Lexer(terminals: Seq[Terminal]):
  private val indexedTerminals = terminals.zipWithIndex

  /** Lazily evaluates the input string to produce a stream of tokens using the longest-prefix-match rule. */
  def tokenize(input: String): LazyList[Token] =

    @tailrec
    def nextValid(cursor: Cursor): Option[(Token, Cursor)] =
      if cursor.offset >= input.length then None
      else
        findLongestMatch(input, cursor) match
          case Some(validToken) if validToken.terminal.isSkipped =>
            nextValid(cursor.advance(validToken.lexeme))

          case Some(validToken) =>
            Some(validToken -> cursor.advance(validToken.lexeme))

          case None =>
            val errorChar = input.charAt(cursor.offset).toString
            val errorToken = Token.Error(errorChar, cursor.pos)
            Some(errorToken -> cursor.advance(errorChar))

    LazyList.unfold(Cursor(0, Position(1, 1)))(nextValid)

  import Lexer.matchPrefixAt

  private def findLongestMatch(input: String, cursor: Cursor): Option[Token.Valid] =
    indexedTerminals
      .flatMap: (t, index) =>
        t.matchPrefixAt(input, cursor.offset)
          .map(lexeme => Token.Valid(t, lexeme, cursor.pos) -> index)
      .maxByOption((token, index) => (token.lexeme.length, -index))
      .map(_._1)

object Lexer:

  extension (t: Terminal)
    def matchPrefixAt(s: String, pos: Int): Option[String] =
      val matcher = t.regex.pattern.matcher(s)
      matcher.region(pos, s.length)
      Option.when(matcher.lookingAt())(matcher.group())
