# Testing

La fase di implementazione si svolge rispettando il principio di Test-Driven Development, in particolare seguendo il processo iterativo Red-Green-Refactor.
Ogni aggiunta di funzionalità al codice in produzione viene preceduta dalla scrittura di un test che la validi.
Dove possibile si preferisce introdurre inizialmente test minimali, seguiti poi da test più articolati che garantiscano robustezza.
È importante notare che i test rispettano gli stessi standard di qualità applicati al resto del sistema.

L'utilizzo del framework ScalaTest ha permesso di testare singoli pezzi di codice isolati.
Inoltre fornisce dei costrutti a supporto della scrittura di codice, come un DSL apposito,
per la realizzazione di test esplicativi e facilmente leggibili.
Questo consente inoltre di capire il funzionamento dell'implementazione a monte semplicemente osservando i test.

---

Metodologia usata: TDD, Red-Green-Refactor (RGR)

Tecnologie usate: ScalaTest

Grado di copertura: Unit test + Componenti robuste (?) + demo eseguibile

Esempi rilevanti: divertiti
