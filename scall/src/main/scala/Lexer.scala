package it.unibo.scall

import Element.*

class Lexer(terminals: Seq[Terminal]):

  def tokenize(input: String): LazyList[Token] =
    LazyList.unfold(0):
      case pos if pos >= input.length => None
      case pos => findLongestMatch(input, pos) match
        case Some(validToken) =>
          Some((validToken, pos + validToken.lexeme.length))
        case None =>
          val errorChar = input.charAt(pos).toString
          Some((Token.Error(errorChar), pos + 1))

  import Lexer.matchPrefixAt

  private def findLongestMatch(input: String, pos: Int): Option[Token] =
    terminals.zipWithIndex
      .flatMap: (t, index) =>
        t.matchPrefixAt(input, pos).map(lexeme => (Token.Valid(t, lexeme), index))
      .maxByOption((token, index) => (token.lexeme.length, -index))
      .map((token, _) => token)

object Lexer:

  extension (t: Terminal)
    def matchPrefixAt(s: String, pos: Int): Option[String] = t match
      case TextTerminal(text) =>
        Option.when(s.startsWith(text, pos))(text)
      case RegexTerminal(regex) =>
        val matcher = regex.pattern.matcher(s)
        matcher.region(pos, s.length)
        Option.when(matcher.lookingAt())(matcher.group())