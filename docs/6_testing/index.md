# Testing

La fase di implementazione si svolge rispettando il principio di Test-Driven Development, in particolare seguendo il processo iterativo Red-Green-Refactor.
Ogni aggiunta di funzionalità al codice in produzione viene preceduta dalla scrittura di un test che la validi.
Dove possibile si preferisce introdurre inizialmente test minimali, seguiti poi da test più articolati che garantiscano robustezza.
È importante notare che i test rispettano gli stessi standard di qualità applicati al resto del sistema.

L'utilizzo del framework ScalaTest ha permesso di testare singoli pezzi di codice isolati.
Inoltre fornisce dei costrutti a supporto della scrittura di codice, come un DSL apposito,
per la realizzazione di test esplicativi e facilmente leggibili.
Questo consente inoltre di capire il funzionamento dell'implementazione a monte semplicemente osservando i test.

Ognuna delle componenti centrali del sistema è dunque caratterizzata da un grado di copertura totale in termini di _unit test_.
In aggiunta, il file `PipelineTest` contiene una raccolta di _integration test_, che vanno a verificare il comportamento complessivo del sistema.

### Esempi rilevanti

Questi esempi mostrano come le tecniche descritte sopra siano state applicate nel concreto all'interno del progetto.

```scala
test("Lexer should return empty seq for empty string"):
  emptyLexer.tokenize("").toList shouldBe empty

test("Lexer recognize string"):
  val tokens = basicLexer.tokenize("if").toList
  tokens.collect { case Token.Valid(t, _, _) => t } shouldBe List(BasicGrammar.ifRule)
  tokens.map(_.lexeme) shouldBe List("if")

test("Lexer implements longest-prefix-match"):
  val tokens = basicLexer.tokenize("iffy").toList
  tokens.map(_.lexeme) shouldBe List("iffy")
  tokens.collect { case Token.Valid(t, _, _) => t } shouldBe List(BasicGrammar.idRule)
```

In alcuni casi viene richiesto di verificare strutture non banali, facilitando l'operazione tramite pattern matching.

```scala
test("oneOrMore is processed by concatenating repetition to all alternatives"):
  ProcessedGrammar.visit((a | b).+).alternatives.toSeq should matchPattern:
    case Seq(
      Seq(s1, r1: InternalNonterminal),
      Seq(s2, r2: InternalNonterminal)
    ) if Set(s1, s2) == Set(a, b) && r1 == r2 =>
```

---

[Indice](../index.md) |
[Capitolo precedente](../5_implementazione/index.md) |
[Capitolo successivo](../7_retrospettiva/index.md)
