package it.unibo.finf
package ast

sealed trait FinfNode

case class Program(declarations: Seq[Declaration], body: Expr) extends FinfNode

sealed trait Declaration extends FinfNode
case class RecordDecl(name: String, params: Seq[Parameter]) extends Declaration
case class FunDecl(name: String, returnType: TypeRef, params: Seq[Parameter], localDecls: Seq[Declaration], body: Expr) extends Declaration
case class ValDecl(name: String, typeRef: TypeRef, value: Expr) extends Declaration

case class Parameter(name: String, typeRef: TypeRef) extends FinfNode

sealed trait TypeRef extends FinfNode
case object IntType extends TypeRef
case object BoolType extends TypeRef
case class CustomType(name: String) extends TypeRef

sealed trait Expr extends FinfNode
case class IntLit(value: Int) extends Expr
case class BoolLit(value: Boolean) extends Expr
case object NullLit extends Expr
case class Id(name: String) extends Expr
case class FieldAccess(record: String, field: String) extends Expr
case class Call(func: String, args: Seq[Expr]) extends Expr
case class New(record: String, args: Seq[Expr]) extends Expr
case class Print(args: Seq[Expr]) extends Expr
case class BinaryOp(op: String, left: Expr, right: Expr) extends Expr
case class UnaryOp(op: String, expr: Expr) extends Expr
case class If(cond: Expr, thenBranch: Expr, elseBranch: Expr) extends Expr
