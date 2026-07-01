package it.unibo.finf

import it.unibo.scall.Grammar

object Finf extends Grammar
  /*
  val program = -> (
    (LET ++ topDeclaration.* ++ IN).? ++ expression ++ SEMI
  )

  val topDeclaration = -> (
    recordDeclaration
  | functionDeclaration
  | valueDeclaration
  )

  val commonDeclaration = -> (
    functionDeclaration
  | valueDeclaration
  )

  val recordDeclaration = -> (
    RECORD ++ ID ++ LPAREN ++ parameterList ++ RPAREN ++ SEMI
  )

  val functionDeclaration = -> (
    functionSignature ++ functionBody
  )
  private val functionSignature = FUN ++ ID ++ COLON ++ typeRef ++ LPAREN ++ parameterList ++ RPAREN
  private val functionBody = (LET ++ commonDeclaration.* ++ IN).? ++ expression ++ SEMI

  val valueDeclaration = -> (
    VAL ++ ID ++ COLON ++ typeRef ++ ASSIGN ++ expression ++ SEMI
  )

  val parameterList = -> (
    ( ID ++ COLON ++ typeRef ++ (COMMA ++ ID ++ COLON ++ typeRef).* ).?
  )

  val expression = -> (
    weakExpression ++ (TIMES | DIV) ++ weakExpression
  | weakExpression ++ (PLUS | MINUS) ++ weakExpression
  | weakExpression ++ (LT | GT | LEQ | GEQ) ++ weakExpression
  | weakExpression ++ (EQ | NEQ) ++ weakExpression
  | weakExpression ++ AND ++ weakExpression
  | weakExpression ++ OR ++ weakExpression
  | weakExpression
  )

  val weakExpression = -> (
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

  val DIGITS  = -> (???)

  val ID      = -> (???)

  val WHITESP = -> (???)

  val COMMENT = -> (???)

  val ERR     = -> (???)
   */

@main
def demo(args: String*): Unit =
  ???
