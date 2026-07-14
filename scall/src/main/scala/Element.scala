package it.unibo.scall

import scala.util.matching.Regex

enum Element:
  case Eps
  case Terminal(name: String, regex: Regex, isSkipped: Boolean = false)
  case Nonterminal(name: String, rule: () => Element)
  case Concat(first: Element, second: Element)
  case Alternation(first: Element, second: Element)
  case Optional(inner: Element)
  case ZeroOrMore(inner: Element)
  case OneOrMore(inner: Element)

object Element:
  type Symbol = Terminal | Nonterminal
  
  extension (element: Element)
    def ++(other: Element): Concat = Concat(element, other)
    def |(other: Element): Alternation = Alternation(element, other)
    def ? : Optional = Optional(element)
    def * : ZeroOrMore = ZeroOrMore(element)
    def + : OneOrMore = OneOrMore(element)

    def show: String = element match
      case Element.Eps => "\u03b5"
      case Element.Terminal(name, _) => name
      case Element.Nonterminal(name, _) => name
      case Element.Concat(first, second) => s"${first.showInConcat} ${second.showInConcat}"
      case Element.Alternation(first, second) => s"${first.show} | ${second.show}"
      case Element.Optional(inner) => s"${inner.showAtom}?"
      case Element.ZeroOrMore(inner) => s"${inner.showAtom}*"
      case Element.OneOrMore(inner) => s"${inner.showAtom}+"

    private def showInConcat: String = element match
      case _: Alternation => s"(${element.show})"
      case _ => element.show

    private def showAtom: String = element match
      case _: (Concat | Alternation) => s"(${element.show})"
      case _ => element.show
