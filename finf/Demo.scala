package it.unibo.finf

import ast.{FinfDecoder, Program}

import it.unibo.scall.ScaLL
import it.unibo.scall.ast.AstError
import it.unibo.scall.parser.ParseError

import scala.io.Source
import scala.util.{Failure, Success, Using}

private object Demo:

  @main def run(files: String*): Unit =
    if files.isEmpty then println("usage: finf <file.finf> ...")
    else
      val failed = files.count(!report(_))
      println(s"\n${files.size - failed} of ${files.size} files parsed with no errors")

  import FinfDecoder.given
  private val analyzer = ScaLL.analyzer[Program](Finf, Finf.program)

  private def report(path: String): Boolean =
    println(s"\n$path")
    Using(Source.fromFile(path))(_.mkString) match
      case Failure(_) =>
        println(" cannot read the file")
        false
      case Success(source) =>
        val outcome = analyzer(source)
        if outcome.isParseValid then printAst(outcome.decoded)
        else printErrors(outcome.parseErrors, source)
        outcome.isValid

  private def printErrors(parseErrors: Seq[ParseError], source: String): Unit =
    val lines = source.linesIterator.toSeq
    parseErrors.foreach(error => println(describe(error, lines)))
    println(s"  ${parseErrors.size} ${if parseErrors.size == 1 then "error" else "errors"}")

  private def printAst(decoded: Either[AstError, Program]): Unit =
    println(" no parse errors")
    decoded match
      case Right(ast) => println(render(ast, " "))
      case Left(error) => println(s" the tree is valid, but the AST decoder failed: ${error.show}")

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
