package it.unibo.finf
package ast

import it.unibo.scall.ast.{AstDecoder, AstError, CSTNode}
import it.unibo.scall.grammar.ProcessedGrammar.AnyNonterminal
import it.unibo.scall.grammar.Element.Terminal
import it.unibo.scall.lexer.Token
import AstDecoder.*
import Finf.*

object TypedExtractors:
  extension (sym: AnyNonterminal)
    def unapplySeq(node: CSTNode): Option[Seq[CSTNode]] = node match
      case CSTNode.RuleNode(s, children) if s.name == sym.name => Some(children)
      case _ => None

  extension (term: Terminal)
    def unapply(node: CSTNode): Option[String] = node match
      case CSTNode.LeafNode(Token.Valid(t, lexeme)) if t.name == term.name => Some(lexeme)
      case _ => None

  object AnyRule:
    def unapplySeq(node: CSTNode): Option[Seq[CSTNode]] = node match
      case CSTNode.RuleNode(_, children) => Some(children)
      case _ => None

  object AnyToken:
    def unapply(node: CSTNode): Option[String] = node match
      case CSTNode.LeafNode(Token.Valid(_, lexeme)) => Some(lexeme)
      case _ => None

import TypedExtractors.*

object FinfDecoder:

  given typeRefDecoder: AstDecoder[TypeRef] with
    def decode(node: CSTNode): Either[AstError, TypeRef] = node match
      /* int | bool | ID */
      case typeRef(INT(_))  => Right(IntType)
      case typeRef(BOOL(_)) => Right(BoolType)
      case typeRef(ID(id))  => Right(CustomType(id))
      case _ => Left(AstError.UnexpectedNode("typeRef", node.toString))

  given paramListDecoder: AstDecoder[Seq[Parameter]] with
    def decode(node: CSTNode): Either[AstError, Seq[Parameter]] =
      def extract(n: CSTNode): Either[AstError, Seq[Parameter]] = n match
        case AnyRule() => Right(Seq.empty)
        /* ID ':' typeRef */
        case AnyRule(ID(id), COLON(_), tpeNode, next) =>
          for tpe <- tpeNode.as[TypeRef]; rest <- extract(next) yield Parameter(id, tpe) +: rest
        /* ',' ID ':' typeRef */
        case AnyRule(COMMA(_), ID(id), COLON(_), tpeNode, next) =>
          for tpe <- tpeNode.as[TypeRef]; rest <- extract(next) yield Parameter(id, tpe) +: rest
        case _ => Left(AstError.DecodingError("Invalid parameter list"))
      extract(node)

  given argListDecoder: AstDecoder[Seq[Expr]] with
    def decode(node: CSTNode): Either[AstError, Seq[Expr]] =
      def extract(n: CSTNode): Either[AstError, Seq[Expr]] = n match
        case AnyRule() => Right(Seq.empty)
        /* expression */
        case AnyRule(exprNode, next) =>
          for e <- exprNode.as[Expr]; rest <- extract(next) yield e +: rest
        /* ',' expression */
        case AnyRule(COMMA(_), exprNode, next) =>
          for e <- exprNode.as[Expr]; rest <- extract(next) yield e +: rest
        case _ => Left(AstError.DecodingError("Invalid argument list"))
      extract(node)

  given declListDecoder: AstDecoder[Seq[Declaration]] with
    def decode(node: CSTNode): Either[AstError, Seq[Declaration]] =
      def extract(n: CSTNode): Either[AstError, Seq[Declaration]] = n match
        case AnyRule() => Right(Seq.empty)
        /* declaration */
        case AnyRule(decl, next) =>
          for d <- decl.as[Declaration]; rest <- extract(next) yield d +: rest
        case _ => Left(AstError.DecodingError("Invalid declaration list"))
      extract(node)

  given exprDecoder: AstDecoder[Expr] with
    def decode(node: CSTNode): Either[AstError, Expr] = node match
      case expression(weak) =>
        weak.as[Expr]
      /* expression OP expression */
      case expression(leftNode, AnyToken(op), rightNode) =>
        for left <- leftNode.as[Expr]; right <- rightNode.as[Expr] yield BinaryOp(op, left, right)
      /* '(' expression ')' */
      case weakExpression(LPAREN(_), expr, RPAREN(_)) =>
        expr.as[Expr]
      /* if expression then '{' expression '}' else '{' expression '}' */
      case weakExpression(IF(_), cond, THEN(_), LBRACE(_), thn, RBRACE(_), ELSE(_), LBRACE(_), els, RBRACE(_)) =>
        for c <- cond.as[Expr]; t <- thn.as[Expr]; e <- els.as[Expr] yield If(c, t, e)
      /* print '(' argumentList ')' */
      case weakExpression(PRINT(_), LPAREN(_), args, RPAREN(_)) =>
        args.as[Seq[Expr]].map(Print(_))
      /* new ID '(' argumentList ')' */
      case weakExpression(NEW(_), ID(id), LPAREN(_), args, RPAREN(_)) =>
        args.as[Seq[Expr]].map(New(id, _))
      /* ID '(' argumentList ')' */
      case weakExpression(ID(func), LPAREN(_), args, RPAREN(_)) =>
        args.as[Seq[Expr]].map(Call(func, _))
      /* ID '.' ID */
      case weakExpression(ID(obj), DOT(_), ID(field)) =>
        Right(FieldAccess(obj, field))
      /* '!' weakExpression */
      case weakExpression(NOT(_), expr) =>
        expr.as[Expr].map(UnaryOp("!", _))
      /* '-' DIGITS */
      case weakExpression(MINUS(_), DIGITS(digits)) =>
        digits.toIntOption
          .toRight(AstError.DecodingError(s"Number out of bounds: -$digits"))
          .map(n => UnaryOp("-", IntLit(n)))
      /* DIGITS */
      case weakExpression(DIGITS(digits)) =>
        digits.toIntOption
          .toRight(AstError.DecodingError(s"Number out of bounds: $digits"))
          .map(IntLit(_))
      /* true | false | null | ID */
      case weakExpression(TRUE(_))  => Right(BoolLit(true))
      case weakExpression(FALSE(_)) => Right(BoolLit(false))
      case weakExpression(NULL(_))  => Right(NullLit)
      case weakExpression(ID(id))   => Right(Id(id))
      case _ => Left(AstError.UnexpectedNode("expression", node.toString))

  given declDecoder: AstDecoder[Declaration] with
    def decode(node: CSTNode): Either[AstError, Declaration] = node match
      case topDeclaration(child)    => child.as[Declaration]
      case commonDeclaration(child) => child.as[Declaration]
      /* val ID ':' typeRef '=' expression ';' */
      case valueDeclaration(VAL(_), ID(id), COLON(_), tpeNode, ASSIGN(_), exprNode, SEMI(_)) =>
        for tpe <- tpeNode.as[TypeRef]; expr <- exprNode.as[Expr] yield ValDecl(id, tpe, expr)
      /* record ID '(' parameterList ')' ';' */
      case recordDeclaration(RECORD(_), ID(id), LPAREN(_), paramsNode, RPAREN(_), SEMI(_)) =>
        paramsNode.as[Seq[Parameter]].map(RecordDecl(id, _))
      /* functionSignature functionBody */
      case functionDeclaration(sig, body) =>
        for
          (id, tpe, params) <- sig match
            /* fun ID ':' typeRef '(' parameterList ')' */
            case functionSignature(FUN(_), ID(i), COLON(_), t, LPAREN(_), p, RPAREN(_)) =>
              for tt <- t.as[TypeRef]; pp <- p.as[Seq[Parameter]] yield (i, tt, pp)
            case _ => Left(AstError.DecodingError("Invalid function signature"))
          (decls, expr) <- body match
            /* let commonDeclaration* in expression ';' */
            case functionBody(LET(_), d, IN(_), e, SEMI(_)) =>
              for dd <- d.as[Seq[Declaration]]; ee <- e.as[Expr] yield (dd, ee)
            /* expression ';' */
            case functionBody(e, SEMI(_)) =>
              e.as[Expr].map((Seq.empty, _))
            case _ => Left(AstError.DecodingError("Invalid function body"))
        yield FunDecl(id, tpe, params, decls, expr)
      case _ => Left(AstError.UnexpectedNode("declaration", node.toString))

  given programDecoder: AstDecoder[Program] with
    def decode(node: CSTNode): Either[AstError, Program] = node match
      /* let topDeclaration* in expression ';' */
      case program(LET(_), decls, IN(_), expr, SEMI(_)) =>
        for d <- decls.as[Seq[Declaration]]; e <- expr.as[Expr] yield Program(d, e)
      /* expression ';' */
      case program(expr, SEMI(_)) =>
        expr.as[Expr].map(Program(Seq.empty, _))
      case _ => Left(AstError.UnexpectedNode("program", node.toString))
