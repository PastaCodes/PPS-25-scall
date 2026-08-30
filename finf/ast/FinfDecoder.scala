package it.unibo.finf
package ast

import it.unibo.scall.ast.{AstDecoder, AstError, CSTNode}
import it.unibo.scall.ast.TypedExtractors.*
import it.unibo.scall.ast.Extractors.*
import AstDecoder.*
import Finf.*

object FinfDecoder:

  given typeRefDecoder: AstDecoder[TypeRef] with
    def decode(node: CSTNode): Either[AstError, TypeRef] = node match
      /* int | bool | ID */
      case typeRef(INT(_))        => Right(IntType)
      case typeRef(BOOL(_))       => Right(BoolType)
      case typeRef(ID(typeName))  => Right(CustomType(typeName))
      case _ => Left(AstError.UnexpectedNode("typeRef", node.toString))

  given paramListDecoder: AstDecoder[Seq[Parameter]] with
    def decode(node: CSTNode): Either[AstError, Seq[Parameter]] = node match
      case RuleSeq(n, children*) if n == parameterList.name =>
        AstDecoder.decodeSequence(children):
          /* ID ':' typeRef ',' ... */
          case Seq(ID(paramName), COLON(_), typeNode, COMMA(_), rest*) =>
            (typeNode.as[TypeRef].map(Parameter(paramName, _)), rest)
          /* ID ':' typeRef */
          case Seq(ID(paramName), COLON(_), typeNode) =>
            (typeNode.as[TypeRef].map(Parameter(paramName, _)), Seq.empty)
      case _ => Left(AstError.UnexpectedNode("parameterList", node.toString))

  given argListDecoder: AstDecoder[Seq[Expr]] with
    def decode(node: CSTNode): Either[AstError, Seq[Expr]] = node match
      case RuleSeq(n, children*) if n == argumentList.name =>
        AstDecoder.decodeSequence(children):
          /* expression ',' ... */
          case Seq(exprNode, COMMA(_), rest*) => (exprNode.as[Expr], rest)
          /* expression */
          case Seq(exprNode) => (exprNode.as[Expr], Seq.empty)
      case _ => Left(AstError.UnexpectedNode("argumentList", node.toString))

  given exprDecoder: AstDecoder[Expr] with
    def decode(node: CSTNode): Either[AstError, Expr] = node match
      case expression(weakExprNode) =>
        weakExprNode.as[Expr]
      /* expression OP expression */
      case expression(leftNode, AnyToken(operator), rightNode) =>
        for
          leftExpr  <- leftNode.as[Expr]
          rightExpr <- rightNode.as[Expr]
        yield BinaryOp(operator, leftExpr, rightExpr)
      /* '(' expression ')' */
      case weakExpression(LPAREN(_), exprNode, RPAREN(_)) =>
        exprNode.as[Expr]
      /* if expression then '{' expression '}' else '{' expression '}' */
      case weakExpression(IF(_), conditionNode, THEN(_), LBRACE(_), thenNode, RBRACE(_), ELSE(_), LBRACE(_), elseNode, RBRACE(_)) =>
        for
          conditionExpr <- conditionNode.as[Expr]
          thenExpr      <- thenNode.as[Expr]
          elseExpr      <- elseNode.as[Expr]
        yield If(conditionExpr, thenExpr, elseExpr)
      /* print '(' argumentList ')' */
      case weakExpression(PRINT(_), LPAREN(_), argsNode, RPAREN(_)) =>
        argsNode.as[Seq[Expr]].map(Print(_))
      /* new ID '(' argumentList ')' */
      case weakExpression(NEW(_), ID(recordName), LPAREN(_), argsNode, RPAREN(_)) =>
        argsNode.as[Seq[Expr]].map(New(recordName, _))
      /* ID '(' argumentList ')' */
      case weakExpression(ID(functionName), LPAREN(_), argsNode, RPAREN(_)) =>
        argsNode.as[Seq[Expr]].map(Call(functionName, _))
      /* ID '.' ID */
      case weakExpression(ID(recordName), DOT(_), ID(fieldName)) =>
        Right(FieldAccess(recordName, fieldName))
      /* '!' weakExpression */
      case weakExpression(NOT(_), exprNode) =>
        exprNode.as[Expr].map(UnaryOp("!", _))
      /* '-' DIGITS */
      case weakExpression(MINUS(_), DIGITS(digitString)) =>
        digitString.toIntOption
          .toRight(AstError.DecodingError(s"Number out of bounds: -$digitString"))
          .map(parsedNumber => UnaryOp("-", IntLit(parsedNumber)))
      /* DIGITS */
      case weakExpression(DIGITS(digitString)) =>
        digitString.toIntOption
          .toRight(AstError.DecodingError(s"Number out of bounds: $digitString"))
          .map(IntLit(_))
      /* true | false | null | ID */
      case weakExpression(TRUE(_))      => Right(BoolLit(true))
      case weakExpression(FALSE(_))     => Right(BoolLit(false))
      case weakExpression(NULL(_))      => Right(NullLit)
      case weakExpression(ID(identifier)) => Right(Id(identifier))
      case _ => Left(AstError.UnexpectedNode("expression", node.toString))

  given declDecoder: AstDecoder[Declaration] with
    def decode(node: CSTNode): Either[AstError, Declaration] = node match
      case topDeclaration(declNode)    => declNode.as[Declaration]
      case commonDeclaration(declNode) => declNode.as[Declaration]
      /* val ID ':' typeRef '=' expression ';' */
      case valueDeclaration(VAL(_), ID(valName), COLON(_), typeNode, ASSIGN(_), valueNode, SEMI(_)) =>
        for
          decodedType  <- typeNode.as[TypeRef]
          decodedValue <- valueNode.as[Expr]
        yield ValDecl(valName, decodedType, decodedValue)
      /* record ID '(' parameterList ')' ';' */
      case recordDeclaration(RECORD(_), ID(recordName), LPAREN(_), paramsNode, RPAREN(_), SEMI(_)) =>
        paramsNode.as[Seq[Parameter]].map(RecordDecl(recordName, _))
      /* functionSignature functionBody */
      case functionDeclaration(signatureNode, bodyNode) =>
        for
          (funcName, returnType, parameters) <- signatureNode match
            /* fun ID ':' typeRef '(' parameterList ')' */
            case functionSignature(FUN(_), ID(nameStr), COLON(_), typeNode, LPAREN(_), paramsNode, RPAREN(_)) =>
              for
                decodedType   <- typeNode.as[TypeRef]
                decodedParams <- paramsNode.as[Seq[Parameter]]
              yield (nameStr, decodedType, decodedParams)
            case _ => Left(AstError.DecodingError("Invalid function signature"))
          (localDecls, bodyExpr) <- bodyNode match
            case RuleSeq(n, LET(_), rest*) if n == functionBody.name =>
              val declsNodes = rest.dropRight(3) // drops the last 3 (IN, expr, SEMI)
              val exprNode   = rest(rest.length - 2) // expr is the second-last element
              for
                declarations <- declsNodes.decodeAll[Declaration]
                expression   <- exprNode.as[Expr]
              yield (declarations, expression)
            /* expression ';' */
            case functionBody(exprNode, SEMI(_)) =>
              exprNode.as[Expr].map((Seq.empty, _))
            case _ => Left(AstError.DecodingError("Invalid function body"))
        yield FunDecl(funcName, returnType, parameters, localDecls, bodyExpr)
      case _ => Left(AstError.UnexpectedNode("declaration", node.toString))

  given programDecoder: AstDecoder[Program] with
    def decode(node: CSTNode): Either[AstError, Program] = node match
      case RuleSeq(n, LET(_), rest*) if n == program.name =>
        val declsNodes = rest.dropRight(3) // drops the last 3 (IN, expr, SEMI)
        val exprNode   = rest(rest.length - 2) // expr is the second-last element
        for
          declarations   <- declsNodes.decodeAll[Declaration]
          mainExpression <- exprNode.as[Expr]
        yield Program(declarations, mainExpression)
      /* expression ';' */
      case program(exprNode, SEMI(_)) =>
        exprNode.as[Expr].map(Program(Seq.empty, _))
      case _ => Left(AstError.UnexpectedNode("program", node.toString))
