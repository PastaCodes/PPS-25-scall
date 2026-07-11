package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import Element.*

class GrammarTest extends AnyFunSuite:

  val a = TextTerminal("a")
  val b = TextTerminal("b")
  val c = TextTerminal("c")

  test("++ concatenates two elements"):
    (a ++ b) shouldBe Concat(a,b)

  test("++ is left-associative"):
    (a ++ b ++ c) shouldBe Concat(Concat(a, b), c)

  test("| combines two alternatives"):
    (a | b) shouldBe Alternation(a, b)

  test("? makes an element optional"):
    a.? shouldBe Optional(a)

  test("* repeats an element zero or more times"):
    a.* shouldBe ZeroOrMore(a)

  test("+ repeats an element one or more times"):
    a.+ shouldBe OneOrMore(a)

  test("++ binds tighter than |, as in EBNF"):
    (a ++ b | c ++ a) shouldBe Alternation(Concat(a, b), Concat(c, a))

  // noinspection ForwardReference
  object ArithmeticGrammar extends Grammar:
    val expression: Nonterminal = -> (term ++ (plus ++ expression).?)
    val term: Nonterminal = -> (digit.+)
    val plus: Terminal = -> ("+")
    val digit: Nonterminal = -> (zero | one)
    val zero: Terminal = -> ("0")
    val one: Terminal = -> ("1")
    val number: Terminal = -> ("[0-9]+".r)

  test("-> create a terminal from a string"):
    ArithmeticGrammar.plus shouldBe TextTerminal("+")

  test("-> create a terminal from a regular expression"):
    ArithmeticGrammar.number shouldBe RegexTerminal("[0-9]+")

  test("-> create a rule with a lazily evaluated body"):
    ArithmeticGrammar.digit.rule() shouldBe Alternation(TextTerminal("0"), TextTerminal("1"))

  test("a rule can reference rules defined after it"):
    ArithmeticGrammar.term.rule() shouldBe OneOrMore(ArithmeticGrammar.digit)

  test("a rule can reference itself without looping"):
    ArithmeticGrammar.expression.rule() shouldBe Concat(
      ArithmeticGrammar.term,
      Optional(Concat(ArithmeticGrammar.plus, ArithmeticGrammar.expression))
    )

  test("equal terminals are tracked once, whether text or regex"):
    object DuplicateGrammar extends Grammar:
      val p1: Terminal = -> ("+")
      val p2: Terminal = -> ("+")
      val r1: Terminal = -> ("[0-9]+".r)
      val r2: Terminal = -> ("[0-9]+".r)
    DuplicateGrammar.terminals shouldBe Set(TextTerminal("+"), RegexTerminal("[0-9]+"))

  test("a grammar keeps track of the terminals it defines"):
    ArithmeticGrammar.terminals shouldBe Set(
      ArithmeticGrammar.plus,
      ArithmeticGrammar.zero,
      ArithmeticGrammar.one,
      ArithmeticGrammar.number
    )

  test("Eps is the empty production"):
    (a | Eps) shouldBe Alternation(a, Eps)

  test("a rule body can be just Eps"):
    object EmptyGrammar extends Grammar:
      val nothing: Nonterminal = -> (Eps)
    EmptyGrammar.nothing.rule() shouldBe Eps
