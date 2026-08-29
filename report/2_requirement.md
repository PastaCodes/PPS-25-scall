# Requirement specification

## Requisiti di business

| ID | Testo del requisito                                                                                                                                                                           | Criterio di accettazione                                                                                                   |
|----|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| B1 | Il sistema deve migliorare l'esperienza dell'utilizzatore nello sviluppo del frontend di un compilatore, richiedendo di implementare unicamente gli aspetti specifici del proprio linguaggio. | Il caso d'uso FINF non presenta dettagli implementativi di analisi lessicale e semantica.                                  |
| B2 | Il sistema deve migliorare l'esperienza dell'utilizzatore nello sviluppo del frontend di un compilatore, favorendo l'uso di codice idiomatico ed espressivo in scala.                         | Il caso d'uso FINF è realizzabile in stile EBNF e si basa su meccanismi di alto livello.                                   |
| B3 | Il sistema deve permettere lo sviluppo di linguaggi non banali.                                                                                                                               | Il caso d'uso FINF è funzionante in tutti i suoi aspetti.                                                                  |
| B4 | Il sistema permette agli sviluppatori della libreria di esplorare i paradigmi e i concetti centrali per il corso.                                                                             | All'interno della libreria viene fatto uso di programmazione logica e di meccanismi avanzati di programmazione funzionale. |
//TODO: in base all'introduzione, semplificare e cambiare con:
* "Il sistema deve migliorare l'esperienza dell'utilizzatore, richiedendo di implementare unicamente gli aspetti specifici del proprio linguaggio."
* "Il sistema deve migliorare l'esperienza dell'utilizzatore, favorendo l'uso di codice idiomatico ed espressivo in scala."

//TODO: FINF deve essere annesso come caso d'uso nella libreria all'interno del capitolo zero.

## Modello di dominio

![](images/domain_model.svg)

### Terminologia

* Un simbolo terminale, o semplicemente terminale, rappresenta un elemento lessicale non ulteriormente scomponibile.
* Un simbolo nonterminale, o semplicemente nonterminale, rappresenta un elemento sintattico che definisce un insieme di combinazioni valide di simboli terminali e nonterminali.
* Una grammatica libera dal contesto, o CFG, raccoglie un insieme di terminali e nonterminali che definiscono un linguaggio libero dal contesto.
  In forma "pura" ogni nonterminale è definito da un insieme di produzioni, dove ciascuna ha un corpo costituito da una semplice sequenza di simboli.
  In forma estesa di Backus-Naur, o EBNF, ogni nonterminale è definito da una singola regola, che però consente l'uso di operatori aggiuntivi, ad esempio di ripetizione.
* Un lessema rappresenta una porzione significativa di una stringa di caratteri in input.
* Un token associa ad un lessema un simbolo terminale della grammatica. 
* Un lexer, o analizzatore lessicale, trasforma una stringa di caratteri in uno stream di token.
* Un albero sintattico concreto, o CST, rappresenta la struttura sintattica di una stringa di input secondo le regole di una grammatica.
* Un parser, o analizzatore sintattico, trasforma uno stream di token in un CST. Un parser LL(1) sfrutta assunzioni sul tipo di linguaggio, risultando in un algoritmo più semplice.
* Una tabella di parsing è una struttura a supporto dell'algoritmo di parsing LL(1) che ne determina il comportamento in base allo stato attuale. 
* Un albero sintattico astratto, o AST, è una rielaborazione della struttura sintattica incentrata sui contenuti di rilevanza semantica.

### Struttura e comportamento

Il diagramma di attività illustra il flusso di esecuzione per l'elaborazione del codice sorgente.
La prima fase è costituita da tre input generati dell'utilizzatore:
la definizione della grammatica, la stringa di input da analizzare e le regole di conversione CST-AST.
Successivamente viene costruito il lexer e l'EBNF viene convertita in CFG, permettendo la generazione della parsing table necessaria per istanziare il parser.
In seguito a questa fase di costruzione inizia l'analisi lessicale, con il lexer che processa la stringa di input restituendo uno stream di token, preso a sua volta in input dal parser che, seguendo la parsing table, esegue l'analisi sintattica. La struttura viene validata andando a generare l'albero CST, il quale viene decodificato dal convertitore definito dall'utilizzatore che restituisce come output l'AST.


## Requisiti funzionali

### Utente

### Sistema

## Requisiti non funzionali

| ID  | Testo del requisito                                                                                                                                               | Criterio di accettazione                                                                                                                      |
|-----|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| NF1 | Il sistema, in quanto libreria, deve esporre una API robusta, chiara e intuitiva.                                                                                 | Tipi e metodi pubblici rispecchiano il modello di dominio e sono documentati dettagliatamente.                                                |
| NF2 | Il sistema deve produrre riscontri chiari e costruttivi in caso di errori lessicali o sintattici.                                                                 | Ad ogni tipo di errore è associata una descrizione dettagliata.                                                                               |
| NF3 | Il sistema deve essere in grado di inizializzare un lexer e un parser a partire da una grammatica e analizzare un file di input il tutto in un tempo accettabile. | Considerando la grammatica FINF e il file di input `quicksort.finf`, il tempo di esecuzione totale non supera i 10 secondi su hardware medio. |

## Requisiti di implementazione

| ID | Testo del requisito                                                                                                                           | Criterio di accettazione                                                         |
|----|-----------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| I1 | Il sistema deve mantenere un'alta qualità interna durante l'intero processo di sviluppo.                                                      | Vengono applicati correttamente i principi di RF, SOC, MOD, ABS, AOC, GEN e INC. |
| I2 | Il sistema deve essere sviluppato primariamente in Scala 3, utilizzando sbt come build system.                                                |                                                                                  |
| I3 | Il sistema deve essere testato durante l'intero processo di sviluppo utilizzando la libreria ScalaTest.                                       | Viene adottato l'approccio TDD.                                                  |
| I4 | Gli elementi più formali del sistema devono essere sviluppati in Prolog e integrati nell'ambiente Scala attraverso la libreria TuProlog Core. |                                                                                  |
