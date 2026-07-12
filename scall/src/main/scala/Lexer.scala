package it.unibo.scall

import Element.*

import scala.annotation.tailrec

class Lexer(terminals: Seq[Terminal]):

  def tokenize(input: String): List[Token] =
    @tailrec
    def _tokenize(remaining: String, acc: List[Token]): List[Token] =
      remaining match
        case "" => acc.reverse
        case s =>
          findBestMatch(s) match
            case Some(validToken) =>
              _tokenize(s.drop(validToken.lexeme.length), validToken :: acc)
            case None =>
              val errorChar = s.take(1)
              _tokenize(s.drop(1), Token.Error(errorChar) :: acc)
    _tokenize(input, Nil)

  import Lexer.matchPrefix
  private def findBestMatch(s: String): Option[Token] =
    terminals.zipWithIndex
      .flatMap: (t, index) =>
        t.matchPrefix(s).map(lexeme => (Token.Valid(t, lexeme), index))
      .maxByOption((token, index) => (token.lexeme.length, -index))
      .map((token, _) => token)

object Lexer:

  extension (t: Terminal)
    def matchPrefix(s: String): Option[String] = t match
      case TextTerminal(text) => Option.when(s.startsWith(text))(text)
      case RegexTerminal(regex) => regex.findPrefixOf(s)
