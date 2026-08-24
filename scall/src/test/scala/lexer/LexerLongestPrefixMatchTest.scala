package it.unibo.scall
package lexer

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import grammar.Grammar

class LexerLongestPrefixMatchTest extends AnyFunSuite:

  object EmptyGrammar extends Grammar
  // noinspection ForwardReference, TypeAnnotation
  object BasicGrammar extends Grammar:
    val ifRule = -> ("if")
    val numRule = -> ("[0-9]+".r)
    val idRule = -> ("[a-z]+".r)
  // noinspection ForwardReference, TypeAnnotation
  object InvertedGrammar extends Grammar:
    val idRule = -> ("[a-z]+".r)
    val numRule = -> ("[0-9]+".r)
    val ifRule = -> ("if")
  // noinspection ForwardReference, TypeAnnotation
  object RegexGrammar extends Grammar:
    val specificRegexRule = -> ("ab".r)
    val generalRegexRule = -> ("[a-z]+".r)

  val emptyLexer = Lexer(EmptyGrammar.terminals)
  val basicLexer = Lexer(BasicGrammar.terminals)
  val invertedLexer = Lexer(InvertedGrammar.terminals)
  val regexLexer = Lexer(RegexGrammar.terminals)

  test("Lexer should return empty seq for empty string"):
    emptyLexer.tokenize("").toList shouldBe empty

    test("Lexer recognize string"):
      val tokens = basicLexer.tokenize("if").toList
      tokens.collect { case Token.Valid(t, _, _) => t } shouldBe List(BasicGrammar.ifRule)
      tokens.map(_.lexeme) shouldBe List("if")

    test("Lexer recognize regex"):
      val tokens = basicLexer.tokenize("test").toList
      tokens.collect { case Token.Valid(t, _, _) => t } shouldBe List(BasicGrammar.idRule)
      tokens.map(_.lexeme) shouldBe List("test")

    test("Lexer implements longest-prefix-match"):
      val tokens = basicLexer.tokenize("iffy").toList
      tokens.map(_.lexeme) shouldBe List("iffy")
      tokens.collect { case Token.Valid(t, _, _) => t } shouldBe List(BasicGrammar.idRule)

    test("Lexer should use declaration order as the tie-breaker mechanism"):
      val basicTokens = basicLexer.tokenize("if").toList
      basicTokens.collect { case Token.Valid(t, _, _) => t } shouldBe List(BasicGrammar.ifRule)
      basicTokens.map(_.lexeme) shouldBe List("if")

      val invertedToken = invertedLexer.tokenize("if").toList
      invertedToken.collect { case Token.Valid(t, _, _) => t } shouldBe List(InvertedGrammar.idRule)
      invertedToken.map(_.lexeme) shouldBe List("if")

      val regexTokens = regexLexer.tokenize("ab").toList
      regexTokens.collect { case Token.Valid(t, _, _) => t } shouldBe List(RegexGrammar.specificRegexRule)
      regexTokens.map(_.lexeme) shouldBe List("ab")

    test("Lexer emits Token.Error for unrecognized characters and keeps scanning"):
      val tokens = basicLexer.tokenize("123$#abc").toList

      tokens.map(_.lexeme) shouldBe List("123", "$", "#", "abc")
      tokens.collect { case Token.Valid(t, _, _) => t } shouldBe List(BasicGrammar.numRule, BasicGrammar.idRule)
      tokens.collect { case Token.Error(s, _) => s } shouldBe List("$", "#")
      