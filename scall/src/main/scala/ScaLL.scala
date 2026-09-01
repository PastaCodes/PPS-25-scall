package it.unibo.scall

import ast.{AstDecoder, AstError, CSTNode}
import grammar.Element.Nonterminal
import grammar.{Grammar, ProcessedGrammar}
import lexer.Lexer
import parser.{ParseError, ParseReport, Parser, ParsingTable}

object ScaLL:

  type Analyzer[A] = String => AnalysisReport[A]

  case class AnalysisReport[A](tree: CSTNode, parseErrors: Seq[ParseError])(using decoder: AstDecoder[A]):
    lazy val decoded: Either[AstError, A] = decoder.decode(tree)
    def isParseValid: Boolean = parseErrors.isEmpty
    def isValid: Boolean = isParseValid && decoded.isRight

  def analyzer[A](grammar: Grammar, startSymbol: Nonterminal)(using decoder: AstDecoder[A]): Analyzer[A] =
    val lexer = Lexer(grammar.terminals)
    val processed = ProcessedGrammar.of(grammar, startSymbol)
    val table = ParsingTable.compute(processed)
    val parser = Parser(table, startSymbol)
    input =>
      val tokens = lexer.tokenize(input)
      val ParseReport(parseTree, parseErrors) = parser.parseAll(tokens)
      AnalysisReport(parseTree, parseErrors)
