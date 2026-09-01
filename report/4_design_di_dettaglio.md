# Design di dettaglio

## Definizione della grammatica

La grammatica in forma EBNF è l'unico ingresso che l'utilizzatore deve fornire per descrivere la sintassi del proprio linguaggio, ed è qui il punto in cui la libreria è più esposta:
la sua forma determina quanto il requisito B2 possa considerarsi soddisfatto. La rappresentazione adottata è un _sum type_ ricorsivo, i casi cui casi corrispondono ai costrutti EBNF: 
elemento vuoto, terminale, nonterminale, concatenazione, alternativa, opzionalità e le due forme di ripetizione. Una grammatica è dunque un albero, ed è la struttura su cui opera la visita
ricorsiva descritta nella sezione successiva. 

Le scelte di design rilevanti riguardano la forma che la definizione assume dal punto di vista di chi la scrive.
* **Operatori EBNF come metodi di estensione.** I costrutti del _sum type_ non vengono invocati direttamente: la composizione avviene tramite operatori simbolici che ricalcano la notazione EBNF. 
    La scelta dei simboli non è arbitraria. Poiché in Scala la precedenza di un operatore è determinata dal suo primo carattere, l'alternativa lega meno della concatenazione e ciascuno dei due è
    associativo a sinistra.
* **Vocabolario del DSL ristretto alla definizione.** Gli operatori di definizione sono membri protetti del tipo che l'utilizzatore estende per dichiarare la propria grammatica. Sono dunque disponibili solo all'interno della 
    definizione e non introducono nomi nel resto del codice cliente.
* **Valutazione lazy delle regole grammaticali.** Il corpo di una regola non viene valutato all'atto della definizione, ma conservato e valutato solo quando la grammatica viene percorsa.
    È ciò che rende possibile definire nonterminali che si riferiscono ad altri nonterminali dichiarati successivamente o a sè stessi. 
* **Nomi dei simboli dedotti dalla definizione.** Ogni terminale e nonterminale assume il nome dell'identificatore che lo introduce, catturato in fase di compilazione. Questo per non creare discrepanza tra nome dichiarato e nome esposto.
* **Terminali espressi uniformemente come espressioni regolari.** Un terminale può essere dichiarato indicandone il lessema esatto oppure un'espressione regolare. Il lessema verrà comunque convertito in una espressione regolare che lo riconosce letteralmente.
* **Ordine di dichiarazione dei terminali preservato.** Poiché l'ordine di dichiarazione ne determina la priorità, la definizione di un terminale lo registra in una collezione ordinata mantenuta dalla
  grammatica. Si accetta consapevolmente uno stato mutabile su un tipo altrimenti immutabile: la mutazione è confinata alla fase di definizione e termina con essa.
* **Identità dei simboli.** I simboli sono confrontati per identità dell'istanza creata dalla definizione, non per struttura. Ciò è possibile perché una grammatica è un oggetto i cui membri
  vengono inizializzati una volta sola, e ogni simbolo attraversa immutato la conversione, la tabella di parsing e il parser. La scelta evita di dipendere dall'uguaglianza strutturale fra espressioni regolari, che l'ambiente non fornisce.

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

La libreria utilizzata per interfacciarsi con l'engine è caratterizzata da uno stile object-oriented e un ampio uso di stato mutabile,
pertanto si prevede che in fase di implementazione emerga la necessità di uno strato intermedio che fornisca maggiore robustezza e consenta di mantenere uno stile idiomatico. 

## Analizzatore Lessicale (Lexer)

L'analizzatore lessicale rappresenta il primo filtro della pipeline architetturale. 
Il suo scopo è la conversione della stringa di input in una sequenza di token, 
ciascuno associato a un simbolo terminale, mantenendo traccia delle coordinate spaziali 
calcolate in base alla posizione nel testo originale.

Le principali scelte di design sono ricadute su:

* **Immutabilità e incapsulamento dello stato (Cursor)**: Per supportare l'iterazione sull'input mantenendo l'assenza di side-effect, 
  il tracciamento spaziale (offset, riga, colonna) è incapsulato in un'entità privata Cursor. 
  Ad ogni match, il lexer non muta puntatori globali, ma genera una nuova istanza di Cursor tramite il metodo `advance`, garantendo la purezza funzionale dell'avanzamento.

* **Gestione funzionale degli errori e ADT**: Per soddisfare il requisito di tolleranza ai caratteri non riconosciuti senza interrompere l'analisi, 
  il design esclude il lancio di eccezioni in fase lessicale. 
  Il tipo di ritorno è modellato come enum type `Token`, permettendo di istanziare ValidToken per i match corretti o di isolare il carattere invalido in uno specifico ErrorToken, 
  incapsulando il fallimento per consentire l'analisi del resto dell'input.

![](images/lexer_class_diagram.svg)

### Algoritmo di matching e risoluzione delle ambiguità

L'algoritmo di tokenizzazione procede consumando iterativamente porzioni della stringa di input $S$. Ad ogni passo, il sistema valuta l'insieme dei terminali $T$ definiti nella grammatica.
Sia $p_t$ il prefisso di $S$ che verifica la regola associata al terminale $t \in T$. Si definisce l'insieme dei match validi come:
$M = \lbrace\, (t, p_t) \;\vert{}\; p_t \text{ è un prefisso valido di } S \,\rbrace$

Per risolvere le ambiguità, il sistema applica in sequenza le seguenti strategie di filtraggio:
* **Maximal match (Longest-prefix-match)**: Il sistema seleziona il sottoinsieme $M_{max} \subseteq M$ che massimizza la lunghezza del prefisso riconosciuto. Ovvero:
  $M_{max} = \lbrace\, (t, p_t) \in M \;\vert{}\; \vert{}p_t\vert{} = \max_{(x, p_x) \in M} \vert{}p_x\vert{} \,\rbrace$
* **Fallback posizionale (Priorità)**: Se $\vert{}M_{max}\vert{} > 1$, si verifica una collisione fra terminali che riconoscono la medesima porzione di testo
  (ad esempio, una parola chiave come if che fa match sia col terminale IF che col terminale generico ID).
  Il sistema risolve il conflitto assegnando la priorità al simbolo terminale dichiarato per primo nella grammatica.
  Definendo $idx(t)$ come l'indice di dichiarazione del terminale $t$, si estrae l'unico vincitore $(t^*, p_{t^*})$ tale che $idx(t^*)$ sia minimo.
* **Scarto dei terminali ignorabili**: Se il terminale vincitore $t^*$ è esplicitamente marcato come ignorabile (es. spaziature o commenti),
  il prefisso $p_{t^*}$ viene consumato dall'input $S$, ma il sistema scarta la porzione in modo silente senza emettere alcun token nella sequenza di output.
* **Error fallback**: Nel caso limite in cui l'insieme dei match validi sia vuoto ($M = \emptyset$), il sistema non riconosce alcun prefisso.
  Per evitare stalli e proseguire l'analisi, il lexer consuma esattamente il primo carattere di $S$,
  lo incapsula in un ErrorToken tracciandone la posizione originaria, e riprende l'algoritmo sul resto della stringa.

## Analizzatore sintattico
È il filtro che converte lo stream di token in un CST, guidato dalla tabella di parsing e dal simbolo iniziale ricevuti in costruzione.
La sua interfaccia riflette la separazione tra la fase di costruzione, che si compie una volta sola nella grammatica, e la fase di analisi, ripetibile su input diversi senza ricalcolare nulla.

### Albero sintattico concreto
Il CST è un _sum type_ con tre casi:
* un nodo di regola, etichettato da un nonterminale e provvisto di una sequenza ordinata di figli.
* una foglia, etichettata dal token riconosciuto
* un nodo di errore, che conserva l'insieme dei terminali attesi e i token scartati durante il recupero.

Il nodo di errore dichiara che l'albero viene prodotto in ogni caso, anche per input malformato, e che il fallimento è un contenuto dell'albero, non un'alternativa ad esso.
Il risultato dell'analisi è un _product type_ che accosta l'albero all'elenco degli errori riscontrati. Su input scorretto l'albero conserva tutta la struttura riconosciuta, che è l'informazione di cui un decodificare ha bisogno.
Viene comunque esposta un'interfaccia secondaria che riduce il risultato al primo errore.

Il nodo di regola può essere unicamente etichettato da un nonterminale dichiarato dall'utilizzatore. I nonterminali ad uso interno non sono ammessi dal tipo.

### Scelte di design
* **Ricorsione al posto della pila esplicita.** L'algoritmo LL(1) è definito in termini di una pila di simboli da riconoscere. L'implementazione non la introduce: la pila è quella delle chiamate, e il riconoscimento di un simbolo e di una sequenza di simboli sono due funzioni mutuamente ricorsive.
  La motivazione è la costruzione dell'albero, che con la ricorsione avviene naturalmente nella risalita, senza introdurre strutture dati parallele per ricomporre i nodi già chiusi.
* **Un simbolo può produrre più nodi.** Il riconoscimento di un simbolo porta ad una sequenza di nodi, questo per rendere possibile l'appiattimento dei nonterminali ad uso interno. Un simbolo interno restituisce direttamente i propri figli, che vengono così assorbiti dal padre, mentre un nonterminale dichiarato
  restituisce unicamente un nodo che li racchiude.
* **Stato e accumulo degli errori come monade.** L'analisi è espressa come composizione di funzioni che ricevono lo stesso stream residuo e restituiscono un valore, lo stream rimanente e gli errori incontrati. La loro composizione propaga lo stream e concatena gli errori.
  Si tratta della combinazione di uno stato e di un accumulatore, resa disponibile come tipo interno della libreria. L'effetto è che le funzioni di analisi si scrivono in forma dichiarativa, senza passare esplicitamente stream ed alenco errori e senza stato mutabile.
* **Insieme di sincronizzazione come attributo ereditato.** L'insieme dei terminali su cui riprendere dopo un errore dipende dal contesto in cui il simbolo corrente è stato espanso. Viene modellato come parametro contestuale, quindi si propaga implicitamente lungo la ricorsione, e sono esplicite solo
  le posizioni in cui viene esteso, ossia quelle in cui si entra in una sequenza e vi si aggiungono i terminali che possono iniziare i simboli rimanenti.

### Recupero degli errori
L'analisi non viene interrotta quando nessuna transizione è applicabile. L'errore viene comunque registrato, i token in input scartati finchè non se ne incotra uno appartenente all'insieme di sincronizzazione del simbolo corrente e l'ananlisi riprende da li.
Se il token raggiunto non consente comunque di espandere il simbolo, questo viene abbandonato e sostituito nell'albero da un nodo di errore. Non viene mai invalidata dunque la porzione di albero già costruita.

L'insieme di sincronizzazione adottato è una sovrastima di quello minimo, in quanto riunisce i terminali iniziali di tutti i simboli rimanenti nella sequenza anziché fermarsi al primo che possa essere vuoto. La scelta è voluta, poiché nel caso peggiore l'analisi riprende leggermente in anticipo, segnalando un errore in più, mentre la configurazione minima richiederebbe di
distinguere i simboli annullabili senza alcun grosso beneficio sulla qualità delle segnalazioni.

La presenza di input residuo dopo il riconoscimento del simbolo iniziale è segnalata come errore invece che essere ignorata.

## Decodifica CST-AST

Il processo di decodifica trasforma l'albero sintattico concreto (CST), generato dal parser,
nell'albero sintattico astratto (AST) specifico per il dominio dell'utente.
Poiché la forma del CST è strettamente vincolata alle regole di derivazione della grammatica formale,
il design necessita di disaccoppiare la visita dalla logica di costruzione dei nodi finali.

### Modellazione del CST

Come formalizzato nel diagramma delle classi, il risultato del parsing è strutturato tramite l'enum type `CSTNode`, 
che partiziona i nodi in tre categorie mutuamente esclusive:
* **RuleNode**: nodo interno generato da un'espansione grammaticale. 
  Contiene un riferimento al simbolo `Nonterminal` associato e la sequenza ordinata di figli `Seq[CSTNode]`.
* **LeafNode**: nodo foglia terminale, incapsula direttamente un `Token` generato dal Lexer.
* **ErrorNode**: nodo speciale introdotto per supportare il meccanismo di recovery del parser. 
  Traccia i simboli attesi `TerminalOrEoi` e gli eventuali token scartati per permettere una diagnostica dettagliata.

![](images/decoder_class_diagram.svg)

### Strategia di astrazione e pattern matching

A livello di design, CST e AST sono entità indipendenti. 
Per evitare all'utilizzatore l'onere di navigare manualmente il CST tramite indici posizionali o laboriose ispezioni manuali sui `RuleNode`, 
il design introduce il pattern degli _Extractor Objects_ tipizzati.

Attraverso gli estrattori, la complessità dell'albero viene mascherata, 
consentendo di definire regole di decodifica dichiarative basate sul pattern matching nativo di Scala. 
L'efficacia di questa scelta di design emerge chiaramente nel caso d'uso del linguaggio FINF.
Per convertire le produzioni grammaticali nei nodi custom dell'AST (come _ValDecl_ o _FunDecl_),
l'utilizzatore ricorre alla decostruzione sintattica. 

Un estrattore come `valueDeclaration(VAL(_), ID(valName), COLON(_), typeNode, ASSIGN(_), valueNode, SEMI(_))` intercetta un RuleNode, 
scarta la sintassi superflua (parole chiave, punteggiatura) catturata dalle wildcard 
e lega direttamente alle variabili solo i nodi semantici rilevanti (_typeNode_, _valueNode_), 
rendendo il mapping verso l'AST immediato e type-safe.

### Design monadico e il companion object

Per governare la complessità computazionale della conversione tra i due alberi, 
il design implementa un approccio monadico. 
Come illustrato nel diagramma UML, l'astrazione poggia sul trait `AstDecoder`. 
L'esposizione delle funzioni di ordine superiore _map_ e _flatMap_ permette di comporre decodificatori elementari in pipeline complesse. 
L'operatore _orElse_ fornisce una logica di fallback fondamentale per processare produzioni con molteplici alternative 
(es. un'espressione che può essere una costante, un identificatore o un'operazione binaria).

A supporto del trait, il design valorizza il ruolo del _companion object_ AstDecoder, 
impiegato come modulo per orchestrare le funzionalità di libreria. 
Oltre a fornire i costruttori di base (pure, fail), il companion object è utilizzato per iniettare metodi di estensione (extension methods) come _.as[A]_ e _.decodeAll[A]_. 
Questa scelta idiomatica estende le funzionalità delle classi CSTNode e Seq[CSTNode] in modo del tutto non invasivo, 
snellendo drasticamente la sintassi delle for-comprehension usate per la decodifica (per esempio in FINF).

### Strategia di propagazione e aggregazione degli errori

Il risultato del metodo _decode_ è progettato per preservare la purezza funzionale: 
non solleva eccezioni a runtime, ma restituisce un Either[AstError, A]. 
L'enum type AstError forma una gerarchia che modella semanticamente le cause dell'interruzione: 
dal fallimento di business logic generato dall'utente, `DecodingError`, alle anomalie strutturali, 
come `UnexpectedNodeStructure` (che preserva formalmente sia il nodo aspettato AnySymbol che la realtà strutturale effettiva CSTNode incontrata).

A livello di design, per governare il flusso di esecuzione in presenza di fallimenti,
il modulo di decodifica espone due strategie complementari:
* **Fail-fast (Short-circuiting)**: Destinata all'elaborazione di strutture gerarchiche e logicamente dipendenti. 
  In tali scenari, il fallimento nella decodifica di una sotto-componente invalida di riflesso l'intero costrutto. 
  Facendo leva sulle proprietà algebriche del design monadico, il sistema interrompe l'analisi al primissimo errore riscontrato. 
  Questa scelta progettuale previene l'insorgere di stati inconsistenti a valle e arresta tempestivamente l'esecuzione di computazioni superflue.
* **Accumulazione esaustiva**: Concepita per l'elaborazione di collezioni composte da elementi logicamente indipendenti 
  (ad esempio, un elenco di dichiarazioni separate all'interno di un programma).
  In questo contesto, un'interruzione prematura sarebbe una mancanza che obbliga l'utilizzatore a un ciclo di risoluzione dei problemi frammentato e iterativo (essendo un solo errore riportato per volta). 
  Per superare i limiti dello short-circuiting nativo, il design espone un costrutto dedicato che forza la valutazione dell'intera collezione, intercettando ogni singola anomalia. 
  I molteplici fallimenti vengono quindi consolidati strutturalmente attraverso il pattern Composite `AggregateError`, 
  permettendo al sistema di restituire un report diagnostico simultaneo ed esaustivo.
