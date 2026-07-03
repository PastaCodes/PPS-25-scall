package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import Element.*

class GrammarTest extends AnyFunSuite:

  val a = Terminal("a")
  val b = Terminal("b")
  val c = Terminal("c")

  test("++ concatenates two element"):
    (a ++ b) shouldBe Concat(a,b)

  test("++ should be left-associative"):
    (a ++ b ++ c) shouldBe Concat(Concat(a, b), c)

  test("| should combines two alternatives"):
    (a | b) shouldBe Alternation(a, b)

  test("? makes an element optional"):
    a.? shouldBe Optional(a)

  test("* repeats an element zero or more times"):
    a.* shouldBe ZeroOrMore(a)

  test("+ repeats an element one or more times"):
    a.+ shouldBe OneOrMore(a)

  test("++ binds tighter then |, as in EBNF"):
    (a ++ b | c ++ a) shouldBe Alternation(Concat(a, b), Concat(c, a))

  object ArithmeticGrammar extends Grammar:
    val expression: Element = ->(term ++ (plus ++ expression).?)
    val term: Element = -> (digit.+)
    val plus: Element = -> ("+")
    val digit: Element = -> (zero | one)
    val zero: Element = -> ("0")
    val one: Element = -> ("1")

  test("-> create a terminal from a string"):
    ArithmeticGrammar.plus shouldBe Terminal("+")

  test("-> create a rule with a lazily evaluated body"):
    val Rule(body) = ArithmeticGrammar.digit: @unchecked
    body() shouldBe Alternation(Terminal("0"), Terminal("1"))

  test("a rule can reference rules defined after it"):
    val Rule(body) = ArithmeticGrammar.term: @unchecked
    body() shouldBe OneOrMore(ArithmeticGrammar.digit)

  test("a rule can reference itself without looping"):
    val Rule(body) = ArithmeticGrammar.expression: @unchecked
    body() shouldBe Concat(
      ArithmeticGrammar.term,
      Optional(Concat(ArithmeticGrammar.plus, ArithmeticGrammar.expression))
    )