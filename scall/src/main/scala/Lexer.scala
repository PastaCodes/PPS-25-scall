package it.unibo.scall

import Element.*

class Lexer(terminals: Seq[Terminal]):

  def tokenize(input: String): LazyList[Token] =
    LazyList.unfold(0): pos =>
      Option.unless(pos >= input.length):
        findLongestMatch(input, pos)
          .map: validToken =>
            (validToken, pos + validToken.lexeme.length)
          .getOrElse:
            val errorChar = input.charAt(pos).toString
            (Token.Error(errorChar), pos + 1)

  import Lexer.matchPrefixAt

  private def findLongestMatch(input: String, pos: Int): Option[Token] =
    terminals.zipWithIndex
      .flatMap: (t, index) =>
        t.matchPrefixAt(input, pos).map(lexeme => (Token.Valid(t, lexeme), index))
      .maxByOption((token, index) => (token.lexeme.length, -index))
      .map((token, _) => token)

object Lexer:

  extension (t: Terminal)
    def matchPrefixAt(s: String, pos: Int): Option[String] =
      val matcher = t.regex.pattern.matcher(s)
      matcher.region(pos, s.length)
      Option.when(matcher.lookingAt())(matcher.group())