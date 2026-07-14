package it.unibo.finf

import it.unibo.scall.Element.Nonterminal
import it.unibo.scall.Grammar

// noinspection ForwardReference, ScalaWeakerAccess, TypeAnnotation
object Finf extends Grammar:

  val program = -> (
    (LET ++ topDeclaration.* ++ IN).? ++ expression ++ SEMI
  )

  val topDeclaration = -> (
    recordDeclaration
  | functionDeclaration
  | valueDeclaration
  )

  val commonDeclaration: Nonterminal = -> (
    functionDeclaration
  | valueDeclaration
  )

  val recordDeclaration = -> (
    RECORD ++ ID ++ LPAREN ++ parameterList ++ RPAREN ++ SEMI
  )

  val functionDeclaration = -> (
    functionSignature ++ functionBody
  )
  val functionSignature = -> (
    FUN ++ ID ++ COLON ++ typeRef ++ LPAREN ++ parameterList ++ RPAREN
  )
  val functionBody = -> (
    (LET ++ commonDeclaration.* ++ IN).? ++ expression ++ SEMI
  )

  val valueDeclaration = -> (
    VAL ++ ID ++ COLON ++ typeRef ++ ASSIGN ++ expression ++ SEMI
  )

  val parameterList = -> (
    ( ID ++ COLON ++ typeRef ++ (COMMA ++ ID ++ COLON ++ typeRef).* ).?
  )

  val expression: Nonterminal = -> (
    weakExpression ++ (TIMES | DIV) ++ weakExpression
  | weakExpression ++ (PLUS | MINUS) ++ weakExpression
  | weakExpression ++ (LT | GT | LEQ | GEQ) ++ weakExpression
  | weakExpression ++ (EQ | NEQ) ++ weakExpression
  | weakExpression ++ AND ++ weakExpression
  | weakExpression ++ OR ++ weakExpression
  | weakExpression
  )

  val weakExpression: Nonterminal = -> (
    LPAREN ++ expression ++ RPAREN
  | IF ++ expression ++ THEN ++ LBRACE ++ expression ++ RBRACE ++ ELSE ++ LBRACE ++ expression ++ RBRACE
  | PRINT ++ LPAREN ++ argumentList ++ RPAREN
  | ID ++ LPAREN ++ argumentList ++ RPAREN
  | NEW ++ ID ++ LPAREN ++ argumentList ++ RPAREN
  | ID ++ DOT ++ ID
  | ID
  | NOT ++ weakExpression
  | MINUS.? ++ DIGITS
  | (TRUE | FALSE)
  | NULL
  )

  val argumentList = -> (
    ( expression ++ (COMMA ++ expression).* ).?
  )

  val typeRef = -> (
    INT
  | BOOL
  | ID
  )

  val PLUS    = -> ("+")
  val MINUS   = -> ("-")
  val TIMES   = -> ("*")
  val DIV     = -> ("/")
  val LPAREN  = -> ("(")
  val RPAREN  = -> (")")
  val LBRACE  = -> ("{")
  val RBRACE  = -> ("}")
  val SEMI    = -> (";")
  val COLON   = -> (":")
  val COMMA   = -> (",")
  val DOT     = -> (".")
  val OR      = -> ("||")
  val AND     = -> ("&&")
  val NOT     = -> ("!")
  val LT      = -> ("<")
  val GT      = -> (">")
  val LEQ     = -> ("<=")
  val GEQ     = -> (">=")
  val EQ      = -> ("==")
  val NEQ     = -> ("!=")
  val ASSIGN  = -> ("=")

  val LET     = -> ("let")
  val IN      = -> ("in")
  val RECORD  = -> ("record")
  val FUN     = -> ("fun")
  val VAL     = -> ("val")
  val IF      = -> ("if")
  val THEN    = -> ("then")
  val ELSE    = -> ("else")
  val INT     = -> ("int")
  val BOOL    = -> ("bool")
  val NEW     = -> ("new")
  val PRINT   = -> ("print")
  val TRUE    = -> ("true")
  val FALSE   = -> ("false")
  val NULL    = -> ("null")

  val DIGITS  = -> ("0|[1-9][0-9]*".r)

  val ID      = -> ("[a-zA-Z][a-zA-Z0-9]*".r)

  val WHITESP = -> ("[\t \r\n]+".r, skip = true)

  val COMMENT = -> ("""/\*.*?\*/""".r, skip = true)

@main
def demo(args: String*): Unit =
  ???
