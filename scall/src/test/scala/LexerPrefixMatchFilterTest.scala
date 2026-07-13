package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import Element.*

class LexerPrefixMatchFilterTest extends AnyFunSuite:

  // noinspection ForwardReference, TypeAnnotation
  object BasicGrammar extends Grammar:
    val whitespaceRule = -> ("\\s+".r)
    val idRule = -> ("[a-z]+".r)

  import BasicGrammar.*

  val lexer = Lexer(BasicGrammar.terminals)

  test("Lexer should individuate whitespace terminals"):
    val tokens = lexer.tokenize(" a b  c ").toList

    tokens.map(_.lexeme) shouldBe List(" ", "a", " ", "b", "  ", "c", " ")
    tokens.collect { case Token.Valid(t, _) => t } shouldBe List(
      whitespaceRule,
      idRule,
      whitespaceRule,
      idRule,
      whitespaceRule,
      idRule,
      whitespaceRule)

  test("Lexer should skip whitespace terminals"):
    given skippedRules: Set[Terminal] = Set(whitespaceRule)
    val tokens = lexer.tokenize(" a b  c ").toList

    tokens.map(_.lexeme) shouldBe List("a", "b", "c")
    tokens.collect { case Token.Valid(t, _) => t } shouldBe List(idRule, idRule, idRule)