package it.unibo.finf

import ast.{FinfDecoder, Program}

import it.unibo.scall.ast.AstDecoder
import AstDecoder.as
import it.unibo.scall.grammar.ProcessedGrammar
import it.unibo.scall.lexer.Lexer
import it.unibo.scall.parser.{ParseError, ParseReport, Parser, ParsingTable}

import scala.io.Source
import scala.util.{Failure, Success, Using}

@main def demo(files: String*): Unit =
  if files.isEmpty then println("usage: finf <file.finf> ...")
  else
    val compiler = FinfCompiler()
    val failed = files.count(!compiler.report(_))
    println(s"\n${files.size - failed} of ${files.size} files parsed with no errors")

private class FinfCompiler:
  import FinfCompiler.*

  private val grammar = ProcessedGrammar.of(Finf, Finf.program)
  private val lexer = Lexer(grammar.terminals)
  private val parser = Parser(ParsingTable.compute(grammar), Finf.program)

  def report(path: String): Boolean =
    println(s"\n$path")
    Using(Source.fromFile(path))(_.mkString) match
      case Failure(_) =>
        println(" cannot read the file")
        false
      case Success(source) =>
        val outcome = parser.parseAll(lexer.tokenize(source))
        if outcome.isValid then printAst(outcome) else printErrors(outcome, source)
        outcome.isValid

  private def printErrors(outcome: ParseReport, source: String): Unit =
    val lines = source.linesIterator.toSeq
    outcome.errors.foreach(error => println(describe(error, lines)))
    println(s"  ${outcome.errors.size} ${if outcome.errors.size == 1 then "error" else "errors"}")

  private def printAst(outcome: ParseReport): Unit =
    println(" no errors")
    import FinfDecoder.given
    outcome.tree.as[Program] match
      case Right(ast) => println(render(ast, " "))
      case Left(error) => println(s" the tree is valid, but the AST decoder failed: ${error.show}")

private object FinfCompiler:

  private def describe(error: ParseError, lines: Seq[String]): String =
    error.position match
      case Some(p) => s"  $p  ${error.show}\n${excerpt(p.line, p.column, lines)}"
      case None => s"  at the end of the file  ${error.show}"

  private def excerpt(line: Int, column: Int, lines: Seq[String]): String =
    lines.lift(line - 1).fold("") { text =>
      val number = line.toString
      val gutter = " " * number.length
      s"     $number | $text\n     $gutter | ${" " * (column - 1)}^"
    }

  private def render(value: Any, indent: String): String = value match
    case values: Seq[?] => values.map(render(_, indent)).mkString
    case node: Product if node.productArity > 0 =>
      val fields = node.productElementNames.zip(node.productIterator).map(field(_, _, indent + "  "))
      s"$indent${node.productPrefix}\n${fields.mkString}"
    case leaf => s"$indent$leaf\n"

  private def field(name: String, value: Any, indent: String): String = value match
    case values: Seq[?] => s"$indent$name:\n${render(values, indent + "  ")}"
    case node: Product if node.productArity > 0 => s"$indent$name:\n${render(node, indent + "  ")}"
    case simple => s"$indent$name: $simple\n"
