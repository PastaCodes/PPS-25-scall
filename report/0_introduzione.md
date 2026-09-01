# ScaLL

***Top-down parsing in Scala***

Il gruppo si pone l'obiettivo di realizzare una libreria a supporto della creazione di frontend di compilatori per linguaggi di classe LL(1).

Un DSL (domain-specific language) altamente espressivo permette di definire grammatiche EBNF direttamente in codice Scala.
A partire dalla grammatica prodotta, la libreria si occupa degli aspetti di analisi lessicale e sintattica.
Inoltre, costrutti aggiuntivi consentono all'utilizzatore di specificare agevolmente una strategia di conversione di alberi sintattici concreti in alberi sintattici astratti.
Nel complesso, il sistema fornisce strumenti per realizzare nuovi linguaggi specificando soltanto gli aspetti specifici ad essi.

Il progetto comprende anche un caso d'uso che mostri gli aspetti della libreria in azione.
Si considera un linguaggio ad hoc semplice ma non banale, nominato FINF (FINF Is Not FOOL) e ispirato alla grammatica vista durante il corso di Linguaggi, Compilatori e Modelli Computazionali.
Oltre alle specifiche della grammatica e alla conversione di alberi sintattici, il caso d'uso consiste in una demo eseguibile da riga di comando.
Diversi file sorgenti possono essere passati come argomenti, così che vengano analizzati in sequenza.
Per ciascuno viene mostrato un albero sintattico astratto generato oppure un riscontro esaustivo riguardo eventuali errori rilevati.
Sono inclusi diversi esempi `.finf` utilizzabili per testare il funzionamento dello strumento.

La qualità della soluzione prodotta è determinata innanzitutto dalle caratteristiche della libreria in quanto tale, ossia in termini di utilità e utilizzabilità.
Il sottoprogetto FINF fornisce una base concreta su cui valutare la completezza delle funzionalità fornite e l'efficacia con cui possono essere applicate.
