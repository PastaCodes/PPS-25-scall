package it.unibo.scall

import ast.{AstDecoder, AstError, CSTNode}
import grammar.Element.Nonterminal
import grammar.{Grammar, ProcessedGrammar}
import lexer.Lexer
import parser.{ParseError, ParseReport, Parser, ParsingTable}

/** Entry point of the library: turns a grammar into a reusable analysis pipeline,
 *  so that a client only has to provide the grammar itself and the decoding rules.
 */
object ScaLL:

  private type Analyzer[A] = String => AnalysisReport[A]

  /** Outcome of a single analysis, pairing the produced [[CSTNode CST]] with the syntactical
   * errors met along the way. The decoded AST is computed on demand, so it is never requested
   * when the input is not syntactically valid.
   */
  case class AnalysisReport[A](tree: CSTNode, parseErrors: Seq[ParseError])(using decoder: AstDecoder[A]):
    lazy val decoded: Either[AstError, A] = decoder.decode(tree)
    def isParseValid: Boolean = parseErrors.isEmpty
    def isValid: Boolean = isParseValid && decoded.isRight

  /** Builds an analyzer for the given grammar. The lexer, the converted grammar, the parsing
   * table and the parser are computed once and shared by every subsequent analysis.
   */
  def analyzer[A](grammar: Grammar, startSymbol: Nonterminal)(using decoder: AstDecoder[A]): Analyzer[A] =
    val lexer = Lexer(grammar.terminals)
    val processed = ProcessedGrammar.of(grammar, startSymbol)
    val table = ParsingTable.compute(processed)
    val parser = Parser(table, startSymbol)
    input =>
      val tokens = lexer.tokenize(input)
      val ParseReport(parseTree, parseErrors) = parser.parseAll(tokens)
      AnalysisReport(parseTree, parseErrors)
