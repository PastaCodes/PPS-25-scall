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
    val expression: Rule = ->(term ++ (plus ++ expression).?)
    val term: Rule = -> (digit.+)
    val plus: Terminal = -> ("+")
    val digit: Rule = -> (zero | one)
    val zero: Terminal = -> ("0")
    val one: Terminal = -> ("1")

  test("-> create a terminal from a string"):
    ArithmeticGrammar.plus shouldBe Terminal("+")

  test("-> create a rule with a lazily evaluated body"):
    ArithmeticGrammar.digit.body() shouldBe Alternation(Terminal("0"), Terminal("1"))

  test("a rule can reference rules defined after it"):
    ArithmeticGrammar.term.body() shouldBe OneOrMore(ArithmeticGrammar.digit)

  test("a rule can reference itself without looping"):
    ArithmeticGrammar.expression.body() shouldBe Concat(
      ArithmeticGrammar.term,
      Optional(Concat(ArithmeticGrammar.plus, ArithmeticGrammar.expression))
    )