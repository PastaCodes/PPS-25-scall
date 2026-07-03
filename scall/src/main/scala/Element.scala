package it.unibo.scall

enum Element:
  case Terminal(text: String)
  case Rule(body: () => Element)
  case Concat(first: Element, second: Element)
  case Alternation(first: Element, second: Element)
  case Optional(inner: Element)
  case ZeroOrMore(inner: Element)
  case OneOrMore(inner: Element)

object Element:
  extension (element: Element)
    def ++(other: Element): Element = Concat(element, other)
    def |(other: Element): Element = Alternation(element, other)
    def ? : Element = Optional(element)
    def * : Element = ZeroOrMore(element)
    def + : Element = OneOrMore(element)
