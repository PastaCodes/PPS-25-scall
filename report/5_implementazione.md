# Implementazione

## Buda Marco

File prodotti: `grammar/ProcessedGrammar`, `parser/ParsingTable`, `prolog/parsing_table`, `util/Scala2P`, `util/CollectionUtils`, `Finf`.
Test prodotti: `grammar/ProcessedGrammarAlternativesTest`, `grammar/ProcessedGrammarProductionsTest`, `grammar/LeftFactoringTest`, `parser/ParsingTableTest`.

Il mio contributo si è focalizzato sul processo di conversione di grammatiche, implementato in stile funzionale, e sulla costruzione di tabelle di parsing, implementata invece attraverso la programmazione logica.
In aggiunta, la stesura della grammatica FINF ha indirizzato la progettazione dei costrutti del DSL.

### Grammatica processata

Come evidenziato in fase di design, vengono privilegiati tipi di dato algebrici in linea con il paradigma funzionale.
La combinazione dei risultati intermedi si basa sull'uso di strutture dati immutabili, ricorsione e metodi di libreria come `foldLeft`.

La funzione di visita della grammatica sfrutta l'ottimizzazione per cui ogni nonterminale viene esplorato una sola volta.
Il calcolo di alternative e produzioni viene delegato a funzioni secondarie per una maggiore leggibilità e separazione di responsabilità.
Inoltre, l'introduzione di metodi di appoggio `visitUnary` e `visitBinary` riduce notevolmente le ripetizioni.
Questo estratto mostra la gestione del flusso di controllo:

```scala
def visit(e: Element)(using skip: Set[Nonterminal]): VisitResult = e match
  case Eps            => VisitResult(Alternatives.ofEps)
  case s: Terminal    => VisitResult(Alternatives.ofSymbol(s))
  case s: Nonterminal => if !skip(s) then visitUnary(s.rule()) /* ... */
  case Concat(t1, t2) => visitBinary(t1, t2) /* ... */
  /* ... */
  case ZeroOrMore(t) => val repetition = repetitionNonterminal(t)
    visitUnary(t)(alternativesFn   = _ => Alternatives.ofZeroOrMore(repetition),
                  addProductionsFn = v => Productions.ofOrMore(v.alternatives, repetition))
  /* ... */
```

In questo modo, il calcolo di alternative e produzioni risulta strutturalmente identico alle formule descritte in fase di design:

```scala
private object Alternatives:
  def ofEps: Alternatives                                                   = Set(Seq.empty)
  def ofSymbol(s: Symbol): Alternatives                                     = Set(Seq(s))
  def ofConcat(t1: Alternatives, t2: Alternatives): Alternatives            = t1 productConcat t2
  def ofAlternation(t1: Alternatives, t2: Alternatives): Alternatives       = t1 union t2
  def ofOptional(t: Alternatives): Alternatives                             = t incl Seq.empty
  def ofZeroOrMore(rep: InternalNonterminal): Alternatives                  = Set(Seq(rep))
  def ofOneOrMore(t: Alternatives, rep: InternalNonterminal): Alternatives  = t eachAppend rep
private object Productions:
  def ofNonterminal(s: Nonterminal, b: Alternatives): Productions =
    leftFactor(s, b)
  def ofOrMore(t: Alternatives, rep: InternalNonterminal): Productions =
    leftFactor(rep, t eachAppend rep incl Seq.empty)
```

I metodi `productConcat` e `eachAppend` utilizzati sopra sono parte di un blocco di metodi di appoggio definito in `CollectionUtils`.
In particolare, il metodo `productConcat` fa uso di una semplice monade per esprimere in modo conciso il concetto matematico di prodotto fra insiemi di stringhe:

```scala
extension [A](self: Set[Seq[A]])
  infix def eachAppend(e: A): Set[Seq[A]] = self.map(_ appended e)
  infix def productConcat(other: Set[Seq[A]]): Set[Seq[A]] =
    for x <- self; y <- other yield x concat y
```

### Left factoring

Il processo di left factoring, invocato all'occorrenza durante la visita ricorsiva, applica la strategia descritta in fase di design.
Il dettaglio implementativo più significativo sta nel metodo utilizzato per raggruppare le alternative in base ai prefissi comuni.
Il metodo responsabile di questa operazione si basa su una chiamata `foldLeft` sull'insieme di alternative in questione.
Il ruolo di accumulatore è ricoperto da una mappa, inizializzata vuota, che associa a ciascun prefisso l'insieme dei suffissi che lo seguono.
L'operatore interno confronta la nuova alternativa da considerare con i prefissi individuati fino a quel punto, in cerca di prefissi comuni.
Le chiavi della mappa sono iterate in modo _lazy_, grazie a `.to(LazyList)`, in modo che, in caso di corrispondenza, i confronti restanti vengano saltati.
In caso di successo, la chiave individuata viene accorciata al nuovo prefisso comune, la porzione restante viene anteposta nuovamente ai suffissi associati e, infine, il nuovo suffisso viene aggiunto alla mappa.
In caso contrario, l'alternativa considerata viene aggiunta interamente come nuova chiave, associata ad un unico elemento, ossia la stringa vuota.

```scala
def prefixed(alternatives: Alternatives): Map[SymbolSeq, Alternatives] =
  alternatives.foldLeft(Map.empty): (accumulator, alternative) =>
    prefixed.keys.to(LazyList)
      .map(prefix => prefix -> longestCommonPrefix(prefix, alternative))
      .find:
        case (_, (common, _, _)) => common.nonEmpty
    match
      case Some(prefix, (common, prefixSuffix, altSuffix)) =>
        val newSuffixes = prefixed(prefix).map(prefixSuffix ++ _) incl altSuffix
        prefixed - prefix + (common -> newSuffixes)
      case None =>
        prefixed + (alternative -> Set(Seq.empty))
```

### Tabella di parsing

Vengono distinti i casi in cui si considera una stringa piuttosto che un singolo simbolo.

```prolog
% first_str(String, EntryOr0)
first_str([], 0).
first_str([A | _], A) :- terminal(A).
first_str([X | _], A) :- first(X, A), A \== 0.
first_str([X | T], A) :- first(X, 0), first_str(T, A).

% first(Nonterminal, EntryOr0)
first(X, A) :- production(X, B), first_str(B, A).
```

Si introduce il concetto intermedio di "following", ossia un'occorrenza di uno specifico simbolo all'interno del corpo di una produzione.
La stringa di simboli che lo segue, oltre alla testa della produzione in questione, risultano utili per il calcolo dei FOLLOW set.

```prolog
% following(Nonterminal, String, Head)
following(X, S, H) :- production(H, B), append(_, [X | S], B), \+ terminal(X).

% follow(Nonterminal, EntryOr1)
follow(X, 1) :- start_symbol(X).
follow(X, A) :- following(X, S, _), first_str(S, A), A \== 0.
follow(X, A) :- following(X, S, H), H \== X, first_str(S, 0), follow(H, A).
```

Da qui in poi vai con un filo di gas.

```prolog
% parsing_cell(Nonterminal, TerminalOr1, Body)
parsing_table_cell(X, A, B) :- production(X, B), first_str(B, A), A \== 0.
parsing_table_cell(X, A, B) :- production(X, B), first_str(B, 0), follow(X, A).
```

## Jacopo Turchi

File prodotti: `lexer/Lexer`, `lexer/Position`, `lexer/Token`, `ast/AstDecoder`, `ast/AstError`, `ast/CSTNode`, `ast/TypedExtractors`, `finf/ast/FinfNode`, `finf/ast/FinfDecoder`.

Test prodotti: `lexer/LexerLongestPrefixMatchTest`, `lexer/LexerPositionTrackingTest`, `lexer/LexerPrefixMatchFilterTest`, `ast/AstDecoderTest`, `ast/CSTNodeTest`, `ast/ExtractorsTest`.

Il mio contributo si è concentrato sullo sviluppo della pipeline di front-end per l'analisi lessicale 
e sull'implementazione del motore di decodifica da CST ad AST. 
L'implementazione traduce i requisiti di assenza di side-effect e disaccoppiamento 
previsti nel design avvalendosi di costrutti avanzati di Scala 3, 
quali la valutazione pigra, la contextual abstraction e il design monadico.

### Lexer

L'analizzatore lessicale è stato progettato per operare in modo completamente privo di stato mutabile.
L'avanzamento lungo la stringa di input è tracciato tramite l'allocazione di istanze immutabili della classe Cursor.
Per garantire che il lexer consumi memoria ed esegua elaborazioni solo quando il parser richiede nuovi token, 
l'iterazione sull'input è implementata sfruttando il costrutto LazyList.unfold:

```scala
def tokenize(input: String): LazyList[Token] =
  @tailrec
  def nextValid(cursor: Cursor): Option[(Token, Cursor)] =
    if cursor.offset >= input.length then None
    else
      findLongestMatch(input, cursor) match
        case Some(validToken) if validToken.terminal.isSkipped =>
          nextValid(cursor.advance(validToken.lexeme))
        case Some(validToken) =>
          Some(validToken -> cursor.advance(validToken.lexeme))
        case None =>
            val errorChar = input.charAt(cursor.offset).toString
            val errorToken = Token.Error(errorChar, cursor.pos)
            Some(errorToken -> cursor.advance(errorChar))
  LazyList.unfold(Cursor(0, Position(1, 1)))(nextValid)
```

La funzione nextValid incapsula la ricorsione tail (@tailrec) e la logica di avanzamento. 
Tramite unfold, la lista pigra viene popolata generando il seed successivo (Cursor) in base al match corrente,
garantendo scalabilità anche su sorgenti di grandi dimensioni.
La risoluzione delle ambiguità (Longest-prefix-match) è implementata in stile dichiarativo
mediante una pipeline funzionale che fa uso di flatMap, maxByOption e combinatori su tuple, 
operando sui terminali indicizzati:

```scala
private def findLongestMatch(input: String, cursor: Cursor): Option[Token.Valid] =
  indexedTerminals
    .flatMap: (t, index) =>
      t.matchPrefixAt(input, cursor.offset)
        .map(lexeme => Token.Valid(t, lexeme, cursor.pos) -> index)
    .maxByOption((token, index) => (token.lexeme.length, -index))
    .map(_._1)
```

### AstDecoder

La trasformazione CST-AST richiede la composizione di operazioni dipendenti soggette a fallimento strutturale. 
Questa problematica è stata risolta implementando il pattern architetturale delle monadi attraverso il trait AstDecoder[A].
Il decoder espone le funzioni di ordine superiore map e flatMap che permettono di comporre decodificatori elementari in pipeline type-safe:







