package it.unibo.scall

import Element.*
import scala.annotation.tailrec

class Lexer(terminals: Seq[Terminal]):
  private val indexedTerminals = terminals.zipWithIndex

  def tokenize(input: String): LazyList[Token] =

    @tailrec
    def nextValid(pos: Int): Option[(Token, Int)] =
      if pos >= input.length then None
      else
        findLongestMatch(input, pos) match
          case Some(Token.Valid(term, lexeme)) if term.isSkipped =>
            nextValid(pos + lexeme.length)

          case Some(validToken) =>
            Some(validToken -> (pos + validToken.lexeme.length))

          case None =>
            Some(Token.Error(input.charAt(pos).toString) -> (pos + 1))

    LazyList.unfold(0)(nextValid)

  import Lexer.matchPrefixAt

  private def findLongestMatch(input: String, pos: Int): Option[Token] =
    indexedTerminals
      .flatMap: (t, index) =>
        t.matchPrefixAt(input, pos).map(Token.Valid(t, _) -> index)
      .maxByOption((token, index) => (token.lexeme.length, -index))
      .map(_._1)

object Lexer:

  extension (t: Terminal)
    def matchPrefixAt(s: String, pos: Int): Option[String] =
      val matcher = t.regex.pattern.matcher(s)
      matcher.region(pos, s.length)
      Option.when(matcher.lookingAt())(matcher.group())
