package it.unibo.finf
package ast

import it.unibo.scall.ast.{AstDecoder, AstError, CSTNode}
import it.unibo.scall.ast.Extractors.{Leaf, Rule, RuleSeq}
import AstDecoder.*

object FinfDecoder:

  given typeRefDecoder: AstDecoder[TypeRef] with
    def decode(node: CSTNode): Either[AstError, TypeRef] = node match
      /* int | bool | ID */
      case RuleSeq("typeRef", Leaf("int"))  => Right(IntType)
      case RuleSeq("typeRef", Leaf("bool")) => Right(BoolType)
      case RuleSeq("typeRef", Leaf(id))     => Right(CustomType(id))
      case _ => Left(AstError.UnexpectedNode("typeRef", node.toString))

  given paramListDecoder: AstDecoder[Seq[Parameter]] with
    def decode(node: CSTNode): Either[AstError, Seq[Parameter]] =
      def extract(n: CSTNode): Either[AstError, Seq[Parameter]] = n match
        case Rule(_, Seq()) => Right(Seq.empty)
        /* ID ':' typeRef */
        case Rule(_, Seq(Leaf(id), _, tpeNode, next)) =>
          for tpe <- tpeNode.as[TypeRef]; rest <- extract(next) yield Parameter(id, tpe) +: rest
        /* ',' ID ':' typeRef */
        case Rule(_, Seq(_, Leaf(id), _, tpeNode, next)) =>
          for tpe <- tpeNode.as[TypeRef]; rest <- extract(next) yield Parameter(id, tpe) +: rest
        case _ => Left(AstError.DecodingError("Invalid parameter list"))
      extract(node)

  given argListDecoder: AstDecoder[Seq[Expr]] with
    def decode(node: CSTNode): Either[AstError, Seq[Expr]] =
      def extract(n: CSTNode): Either[AstError, Seq[Expr]] = n match
        case Rule(_, Seq()) => Right(Seq.empty)
        /* expression */
        case Rule(_, Seq(exprNode, next)) =>
          for e <- exprNode.as[Expr]; rest <- extract(next) yield e +: rest
        /* ',' expression */
        case Rule(_, Seq(_, exprNode, next)) =>
          for e <- exprNode.as[Expr]; rest <- extract(next) yield e +: rest
        case _ => Left(AstError.DecodingError("Invalid argument list"))
      extract(node)

  given declListDecoder: AstDecoder[Seq[Declaration]] with
    def decode(node: CSTNode): Either[AstError, Seq[Declaration]] =
      def extract(n: CSTNode): Either[AstError, Seq[Declaration]] = n match
        case Rule(_, Seq()) => Right(Seq.empty)
        /* declaration */
        case Rule(_, Seq(decl, next)) =>
          for d <- decl.as[Declaration]; rest <- extract(next) yield d +: rest
        case _ => Left(AstError.DecodingError("Invalid declaration list"))
      extract(node)

  given exprDecoder: AstDecoder[Expr] with
    def decode(node: CSTNode): Either[AstError, Expr] = node match
      case RuleSeq("expression", weak) =>
        weak.as[Expr]
      /* expression OP expression */
      case RuleSeq("expression", leftNode, Leaf(op), rightNode) =>
        for left <- leftNode.as[Expr]; right <- rightNode.as[Expr] yield BinaryOp(op, left, right)
      /* '(' expression ')' */
      case RuleSeq("weakExpression", Leaf("("), expr, Leaf(")")) =>
        expr.as[Expr]
      /* if expression then '{' expression '}' else '{' expression '}' */
      case RuleSeq("weakExpression", Leaf("if"), cond, Leaf("then"), Leaf("{"), thn, Leaf("}"), Leaf("else"), Leaf("{"), els, Leaf("}")) =>
        for c <- cond.as[Expr]; t <- thn.as[Expr]; e <- els.as[Expr] yield If(c, t, e)
      /* print '(' argumentList ')' */
      case RuleSeq("weakExpression", Leaf("print"), Leaf("("), args, Leaf(")")) =>
        args.as[Seq[Expr]].map(Print(_))
      /* new ID '(' argumentList ')' */
      case RuleSeq("weakExpression", Leaf("new"), Leaf(id), Leaf("("), args, Leaf(")")) =>
        args.as[Seq[Expr]].map(New(id, _))
      /* ID '(' argumentList ')' */
      case RuleSeq("weakExpression", Leaf(func), Leaf("("), args, Leaf(")")) =>
        args.as[Seq[Expr]].map(Call(func, _))
      /* ID '.' ID */
      case RuleSeq("weakExpression", Leaf(obj), Leaf("."), Leaf(field)) =>
        Right(FieldAccess(obj, field))
      /* '!' weakExpression */
      case RuleSeq("weakExpression", Leaf("!"), expr) =>
        expr.as[Expr].map(UnaryOp("!", _))
      /* '-' DIGITS */
      case RuleSeq("weakExpression", Leaf("-"), Leaf(digits)) =>
        Right(UnaryOp("-", IntLit(digits.toInt)))
      /* DIGITS */
      case RuleSeq("weakExpression", Leaf(digits)) if digits.forall(_.isDigit) =>
        Right(IntLit(digits.toInt))
      /* true | false | null | ID */
      case RuleSeq("weakExpression", Leaf("true"))  => Right(BoolLit(true))
      case RuleSeq("weakExpression", Leaf("false")) => Right(BoolLit(false))
      case RuleSeq("weakExpression", Leaf("null"))  => Right(NullLit)
      case RuleSeq("weakExpression", Leaf(id)) => Right(Id(id))
      case _ => Left(AstError.UnexpectedNode("expression", node.toString))

  given declDecoder: AstDecoder[Declaration] with
    def decode(node: CSTNode): Either[AstError, Declaration] = node match
      case RuleSeq("topDeclaration", child) => child.as[Declaration]
      case RuleSeq("commonDeclaration", child) => child.as[Declaration]
      /* val ID ':' typeRef '=' expression ';' */
      case RuleSeq("valDeclaration", _, Leaf(id), _, tpeNode, _, exprNode, _) =>
        for tpe <- tpeNode.as[TypeRef]; expr <- exprNode.as[Expr] yield ValDecl(id, tpe, expr)
      /* record ID '(' parameterList ')' ';' */
      case RuleSeq("recordDeclaration", _, Leaf(id), _, paramsNode, _, _) =>
        paramsNode.as[Seq[Parameter]].map(RecordDecl(id, _))
      /* functionSignature functionBody */
      case RuleSeq("functionDeclaration", sig, body) =>
        for
          (id, tpe, params) <- sig match
            /* fun ID ':' typeRef '(' parameterList ')' */
            case RuleSeq("functionSignature", _, Leaf(i), _, t, _, p, _) =>
              for tt <- t.as[TypeRef]; pp <- p.as[Seq[Parameter]] yield (i, tt, pp)
            case _ => Left(AstError.DecodingError("Invalid function signature"))
          (decls, expr) <- body match
            /* let commonDeclaration* in expression ';' */
            case RuleSeq("functionBody", _, d, _, e, _) =>
              for dd <- d.as[Seq[Declaration]]; ee <- e.as[Expr] yield (dd, ee)
            /* expression ';' */
            case RuleSeq("functionBody", e, _) =>
              e.as[Expr].map((Seq.empty, _))
            case _ => Left(AstError.DecodingError("Invalid function body"))
        yield FunDecl(id, tpe, params, decls, expr)
      case _ => Left(AstError.UnexpectedNode("declaration", node.toString))

  given programDecoder: AstDecoder[Program] with
    def decode(node: CSTNode): Either[AstError, Program] = node match
      /* let topDeclaration* in expression ';' */
      case RuleSeq("program", _, decls, _, expr, _) =>
        for d <- decls.as[Seq[Declaration]]; e <- expr.as[Expr] yield Program(d, e)
      /* expression ';' */
      case RuleSeq("program", expr, _) =>
        expr.as[Expr].map(Program(Seq.empty, _))
      case _ => Left(AstError.UnexpectedNode("program", node.toString))
