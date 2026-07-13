package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import Element.*

class LexerPrefixMatchFilterTest extends AnyFunSuite:
  
  // noinspection ForwardReference, TypeAnnotation
  object NoSkippingGrammar extends Grammar:
    val whitespaceRule = -> ("\\s+".r)
    val idRule = -> ("[a-z]+".r)
    
  // noinspection ForwardReference, TypeAnnotation
  object SkippingGrammar extends Grammar:
    val whitespaceRule = -> ("\\s+".r, skip = true)
    val idRule = -> ("[a-z]+".r)

  val noSkippingLexer = Lexer(NoSkippingGrammar.terminals)
  val SkippingLexer = Lexer(SkippingGrammar.terminals)
  
  test("Lexer should tokenise whitespace if not marked as skipped"):
    val tokens = noSkippingLexer.tokenize(" a b ").toList

    tokens.map(_.lexeme) shouldBe List(" ", "a", " ", "b", " ")
    tokens.collect { case Token.Valid(t, _) => t } shouldBe List(
      NoSkippingGrammar.whitespaceRule,
      NoSkippingGrammar.idRule,
      NoSkippingGrammar.whitespaceRule,
      NoSkippingGrammar.idRule,
      NoSkippingGrammar.whitespaceRule
    )

  test("Lexer should automatically drop skipped terminals defined in grammar"):
    val tokens = SkippingLexer.tokenize(" a b  c ").toList

    tokens.map(_.lexeme) shouldBe List("a", "b", "c")
    tokens.collect { case Token.Valid(t, _) => t } shouldBe List(
      SkippingGrammar.idRule,
      SkippingGrammar.idRule,
      SkippingGrammar.idRule
    )