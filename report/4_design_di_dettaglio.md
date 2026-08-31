# Design di dettaglio

## Analizzatore Lessicale (Lexer)
L'analizzatore lessicale rappresenta il primo filtro della pipeline architetturale.
Il suo scopo è la conversione della stringa di input in uno stream di token,
mantenendo traccia delle coordinate spaziali per la visualizzazione degli errori.

Le principali scelte di design sono ricadute su:
* **Immutabilità e ADT**: Il tracciamento spaziale è delegato a un modulo Position modellato come ADT immutabile.
    Ogni token generato detiene una referenza a una precisa istanza di Position,
    garantendo l'assenza di side-effect durante le fasi successive di parsing.
* **Gestione funzionale degli errori**: Per soddisfare il requisito di tolleranza ai caratteri non riconosciuti senza interrompere la pipeline,
  il design esclude il lancio di eccezioni in fase lessicale.
  Viene adottato un approccio polimorfico per i token: i caratteri non validi vengono incapsulati in un costrutto specifico `ErrorToken`, 
  permettendo al lexer di isolare il fallimento e proseguire la _tokenizzazione_ del resto del file.
* **Risoluzione delle ambiguità**: La logica di longest-prefix-match e di priorità dei terminali è centralizzata in una pipeline funzionale 
  che agisce da filtro progressivo (matching delle espressioni regolari, ordinamento per lunghezza del match, fallback sull'ordine di dichiarazione).

![](images/lexerClassDiagram.svg)

## Grammatica processata

![](images/detailed_design_processed_grammar_class.svg)

Volendo rappresentare il risultato della conversione da grammatica EBNF a CFG pura, si introduce il concetto di grammatica processata (`ProcessedGrammar`), un _product type_ composto da un simbolo di partenza, una collezione di terminali e una collezione di produzioni, come espresso dai requisiti.
Anche in questo caso, le priorità dei terminali vengono espresse dal loro ordinamento.

Si introduce il concetto di simbolo nonterminale ad uso interno (`InternalNonterminal`), che può essere utilizzato dagli operatori di ripetizione e dall'algoritmo di left factoring automatico.
Questi nonterminali sono distinti da quelli definiti dalla grammatica di partenza, in quanto:
* Non hanno una regola (EBNF) che li definisca.
* Non hanno un nome definito dall'utilizzatore. In fase di implementazione verrà aggiunto un nome ad uso interno.
* Non appaiono all'interno del CST generato dal parser.

Queste differenze giustificano la creazione di un tipo distinto. Ad ogni modo, si tratta di simboli che possono apparire come teste di produzioni.
Per questo scopo viene introdotto un _sum type_ che includa entrambi i tipi di nonterminali.
Inoltre, all'interno dei corpi di produzione possono apparire simboli di qualsiasi genere. Si definisce dunque un ulteriore tipo che riunisca terminali e nonterminali sia ad uso interno che non.

A questo punto, le produzioni sono rappresentate dall'associazione di un nonterminale come testa a una sequenza ordinata di simboli come corpo.

### Conversione

![](images/detailed_design_processed_grammar_activity.svg)

Data la struttura ad albero della grammatica EBNF in ingresso, il processo di conversione avviene attraverso una visita ricorsiva ispirata al _visitor pattern_, seppure in chiave funzionale.
La visita comincia a partire dal simbolo iniziale fornito ed esplora automaticamente tutti i simboli raggiungibili da esso.
Ogni chiamata restituisce un risultato intermedio e si occupa di combinare i risultati prodotti dagli eventuali nodi figli, oltre alle proprie informazioni.
A seconda che l'elemento attuale sia una foglia, un operatore unario, o un operatore binario, il numero di chiamate ricorsive varia da zero a due, una per ciascun nodo figlio.
Al termine del processo, il risultato della chiamata iniziale contiene le informazioni necessarie per popolare i campi della grammatica processata.

I risultati intermedi contengono un insieme di alternative e un accumulatore di produzioni generate.
Limitatamente a questa fase interna, si fa riferimento alle "alternative" come l'insieme di possibili sequenze di simboli che l'elemento in questione può assumere all'interno del corpo di produzione che lo contiene.
Si osservi che visitare lo stesso elemento più volte non produce mai nuove informazioni.

Il calcolo delle alternative ed eventuali produzioni generate varia a seconda dello specifico tipo di elemento.
Fra le possibili conversioni che producono una grammatica fattorizzata a sinistra equivalente, si adotta la seguente:

* Elemento vuoto:&ensp;$\mathrm{ALT}(\varepsilon)=\lbrace\, \varepsilon \,\rbrace$,&ensp;$\mathrm{PROD}(\varepsilon)=\emptyset$.
* Terminale:&ensp;$\mathrm{ALT}(b)=\lbrace\, b \,\rbrace$,&ensp;$\mathrm{PROD}(b)=\emptyset$.
* Nonterminale $X$ la cui regola ha come corpo $E$:&ensp;$\mathrm{ALT}(X)=\lbrace\, X \,\rbrace$,&ensp;$\mathrm{PROD}(X)=\mathrm{LFACT}(X, \mathrm{ALT}(E))$.
* Concatenazione:&ensp;$\mathrm{ALT}(E_1E_2)=\mathrm{ALT}(E_1)\cdot\mathrm{ALT}(E_2)$,&ensp;$\mathrm{PROD}(E_1E_2)=\emptyset$.
* Alternativa:&ensp;$\mathrm{ALT}(E_1 \lor E_2)=\mathrm{ALT}(E_1)\cup\mathrm{ALT}(E_2)$,&ensp;$\mathrm{PROD}(E_1 \lor E_2)=\emptyset$.
* Opzionalità:&ensp;$\mathrm{ALT}(E \lor \varepsilon)=\mathrm{ALT}(E)\cup\lbrace\, \varepsilon \,\rbrace$,&ensp;$\mathrm{PROD}(E \lor \varepsilon)=\emptyset$.
* Zero o più:&ensp;$\mathrm{ALT}(E^*)=\lbrace\, R \,\rbrace$,&ensp;$\mathrm{PROD}(E^*)=\mathrm{LFACT}\bigl(R, (\mathrm{ALT}(E)\cdot\lbrace\,R\,\rbrace)\cup\lbrace\,\varepsilon\,\rbrace\bigr)$.<br>
  Il simbolo $R$ è un nonterminale ad uso interno, introdotto per implementare la ripetizione. Ogni occorrenza di un operatore di ripetizione introduce un simbolo distinto.
* Uno o più:&ensp;$\mathrm{ALT}(E^+)=\mathrm{ALT}(E)\cdot\lbrace\, R \,\rbrace$,&ensp;$\mathrm{PROD}(E^+)=\mathrm{PROD}(E^*)$.

Si è fatto uso della notazione $A \cdot B$ per indicare il prodotto fra insiemi di stringhe, ossia $\lbrace\, \alpha\,\beta \;|\; \alpha \in A, \beta \in B \,\rbrace$.

### Left factoring

La funzione $\mathrm{LFACT}(X,A)$ indica le produzioni generate dall'applicazione dell'algoritmo di left factoring,
che vanno a sostituire le produzioni non fattorizzate $\lbrace\,X\rightarrow\alpha \;|\; \alpha \in A\,\rbrace$.
La strategia adottata prevede, per prima cosa, di raggruppare le alternative in base ad eventuali prefissi comuni.
Per ogni prefisso individuato $\beta_i$ e relativi suffissi $B_i$, viene introdotto un nonterminale ad uso interno $F_i$ e vengono generate le produzioni&ensp;$X\rightarrow\beta_i\,F_i$&ensp;e&ensp;$\mathrm{LFACT}(F_i,B_i)$.
La ricorsione si interrompe per le alternative che non hanno prefissi in comune con altre.

## Tabella di parsing

![](images/detailed_design_parsing_table_class.svg)

La rappresentazione di tabelle di parsing segue dai vincoli di dominio.
Viene introdotto l'oggetto `Eoi` per indicare l'esaurimento dell'input, così che possa apparire come colonna nella tabella, insieme ai terminali della grammatica.

### Costruzione

La costruzione è definita da regole formali, per questo motivo si opta per un'implementazione in programmazione logica.
Poiché questa scelta impatta drasticamente sulla struttura di una parte significativa del sistema, la si considera una decisione di design.
Altri elementi del dominio potrebbero essere espressi in termini di programmazione logica, ma, nel rispetto del requisito I2, si limita l'applicazione a questo solo sottoproblema.

In vista dell'implementazione in programmazione logica, si considera una riformulazione della definizione dei FIRST set e FOLLOW set:
* $\mathrm{FIRST}(\varepsilon) = \lbrace\, \varepsilon \,\rbrace$.
* $\mathrm{FIRST}(b\,\alpha) = \lbrace\, b \,\rbrace$.
* $\mathrm{FIRST}(X_1X_2\!\cdots\!X_n) \supseteq \mathrm{FIRST}(X_1) \setminus \lbrace\, \varepsilon \,\rbrace$.
* Se&ensp;$\varepsilon\in\mathrm{FIRST}(X_1)$&ensp;allora&ensp;$\mathrm{FIRST}(X_1X_2\!\cdots\!X_n) \supseteq \mathrm{FIRST}(X_2\!\cdots\!X_n)$.
* Per ogni produzione $X \rightarrow \alpha$ nella grammatica,&ensp;$\mathrm{FIRST}(X) \supseteq \mathrm{FIRST}(\alpha)$.
* $\mathrm{FOLLOW}(S) \ni \$$&ensp;dove $S$ è il simbolo iniziale.
* Per ogni produzione $Y \rightarrow \alpha\,X\,\beta$ nella grammatica,&ensp;$\mathrm{FOLLOW}(X) \supseteq \mathrm{FIRST}(\beta) \setminus \lbrace\, \varepsilon \,\rbrace$.
* Per ogni produzione $Y \rightarrow \alpha\,X\,\beta$ nella grammatica, se&ensp;$\varepsilon \in \mathrm{FIRST}(\beta)$&ensp;allora&ensp;$\mathrm{FOLLOW}(X) \supseteq \mathrm{FOLLOW}(Y)$.

![](images/detailed_design_parsing_table_activity.svg)

Il calcolo eseguito dall'engine logico viene integrato nel sistema tramite un'attività specifica, esponendo un'interfaccia astratta rispetto alla tecnologia e implementazione sottostante (vedi requisito I1).
Durante una singola richiesta, l'engine, creato inizialmente con la sola teoria di base, viene aggiornato in base alla struttura della grammatica da elaborare.
Viene sfruttata la funzionalità di registrazione degli oggetti, così che i risultati vengano riportati direttamente in termini di istanze ricevute in ingresso.
Le soluzioni prodotte dall'engine vengono raccolte per popolare la tabella da restituire.

## Decodifica CST-AST
