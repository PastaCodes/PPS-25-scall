package it.unibo.scall
package ast

import grammar.Element.{Nonterminal, Terminal}
import lexer.Token

/** Provides type-safe extractors to perform pattern matching directly against
 *  grammar [[Nonterminal]]s and [[Terminal]]s, abstracting away the underlying [[CSTNode]] boilerplate.
 */
object TypedExtractors:

  extension (symbol: Nonterminal)
    def unapplySeq(node: CSTNode): Option[Seq[CSTNode]] = node match
      case CSTNode.RuleNode(s, children) if s.name == symbol.name => Some(children)
      case _ => None

  extension (terminal: Terminal)
    def unapply(node: CSTNode): Option[String] = node match
      case CSTNode.LeafNode(Token.Valid(t, lexeme, _)) if t.name == terminal.name => Some(lexeme)
      case _ => None

  object AnyToken:
    def unapply(node: CSTNode): Option[String] = node match
      case CSTNode.LeafNode(Token.Valid(_, lexeme, _)) => Some(lexeme)
      case _ => None
