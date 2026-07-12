package it.unibo.scall

enum Element:
  case Eps
  case Terminal(pattern: String)
  case Nonterminal(rule: () => Element)
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
