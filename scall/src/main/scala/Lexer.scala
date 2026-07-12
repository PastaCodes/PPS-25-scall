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
            case Some(token) => _tokenize(s.drop(token.lexeme.length), token :: acc)
            case None => acc.reverse
    _tokenize(input, Nil)

  import Lexer.matchPrefix
  private def findBestMatch(s: String): Option[Token] =
    terminals
      .flatMap: t =>
        t.matchPrefix(s).map(lexeme => Token(t, lexeme))
      .maxByOption(_.lexeme.length)

object Lexer:
  extension (t: Terminal)
    def matchPrefix(s: String): Option[String] = t match
      case TextTerminal(text) => Option.when(s.startsWith(text))(text)
      case RegexTerminal(regex) => regex.findPrefixOf(s)
