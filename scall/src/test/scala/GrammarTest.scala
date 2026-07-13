package it.unibo.scall

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import scala.util.matching.Regex
import Element.*

class GrammarTest extends AnyFunSuite:

  private def terminal(text: String): Terminal = Terminal(text, text.r)

  val a: Terminal = terminal("a")
  val b: Terminal = terminal("b")
  val c: Terminal = terminal("c")

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

  object DuplicateGrammar extends Grammar:
    val p1: Terminal = ->("[ab]".r)
    val p2: Terminal = ->("[ab]".r)

  test("-> captures the name of the val being defined"):
    ArithmeticGrammar.plus.name shouldBe "plus"
    ArithmeticGrammar.number.name shouldBe "number"

  test("-> captures the name of the Nonterminal val being defined"):
    ArithmeticGrammar.expression.name shouldBe "expression"
    ArithmeticGrammar.digit.name shouldBe "digit"

  test("-> create a terminal from a string"):
    ArithmeticGrammar.plus.regex.regex shouldBe Regex.quote("+")

  test("-> create a terminal from a regular expression"):
    ArithmeticGrammar.number.regex.regex shouldBe "[0-9]+"

  test("-> create a rule with a lazily evaluated body"):
    ArithmeticGrammar.digit.rule() shouldBe Alternation(ArithmeticGrammar.zero, ArithmeticGrammar.one)

  test("a rule can reference rules defined after it"):
    ArithmeticGrammar.term.rule() shouldBe OneOrMore(ArithmeticGrammar.digit)

  test("a rule can reference itself without looping"):
    ArithmeticGrammar.expression.rule() shouldBe Concat(
      ArithmeticGrammar.term,
      Optional(Concat(ArithmeticGrammar.plus, ArithmeticGrammar.expression))
    )

  test("duplicated terminals are all kept, in declaration order"):
    DuplicateGrammar.p1 should not be DuplicateGrammar.p2
    DuplicateGrammar.terminals shouldBe Seq(DuplicateGrammar.p1, DuplicateGrammar.p2)

  test("duplicated terminals are distinguishable by name"):
    DuplicateGrammar.p1.name shouldBe "p1"
    DuplicateGrammar.p2.name shouldBe "p2"

  test("a grammar keeps track of the terminals it defines"):
    ArithmeticGrammar.terminals shouldBe Seq(
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

  test("a regex exposes its pattern as a compiled regex"):
    ArithmeticGrammar.number.regex.matches("42") shouldBe true
    ArithmeticGrammar.number.regex.matches("4a") shouldBe false

  test("a regex created from text matches it literally"):
    ArithmeticGrammar.plus.regex.matches("+") shouldBe true
    ArithmeticGrammar.plus.regex.matches("++") shouldBe false

  test("show render elements as EBNF"):
    ArithmeticGrammar.expression.rule().show shouldBe "term (plus expression)?"
    (a ++ b | c).show shouldBe "a b | c"
    ((a | b) ++ c).show shouldBe "(a | b) c"
    (a ++ b ++ c).show shouldBe "a b c"
    (a ++ b).?.show shouldBe "(a b)?"
    Eps.show shouldBe "\u03b5"
