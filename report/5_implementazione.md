# Implementazione

Il gruppo si impegna a mantenere una forte collaborazione durante l'intera durata del progetto, attraverso confronti frequenti riguardo aspetti di dominio e scelte relative alle interfacce condivise.
Indipendentemente da questo, il processo di implementazione viene svolto singolarmente, pertanto ogni membro ha un chiaro insieme di file prodotti, dei quali possiede completa responsabilità.

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

Come anticipato, la costruzione della tabella di parsing avviene attraverso un engine TuProlog.
La teoria utilizzata coincide ampiamente con le formule rielaborate in fase di design, a partire dal calcolo dei FIRST set.
Per questi vengono distinti i casi in cui si considera una stringa piuttosto che un singolo simbolo.

```prolog
% first_str(String, EntryOr0)
first_str([], 0).
first_str([A | _], A) :- terminal(A).
first_str([X | _], A) :- first(X, A), A \== 0.
first_str([X | T], A) :- first(X, 0), first_str(T, A).

% first(Nonterminal, EntryOr0)
first(X, A) :- production(X, B), first_str(B, A).
```

Parallelamente, si introduce il concetto intermedio di "following", ossia un'occorrenza di uno specifico simbolo all'interno del corpo di una produzione.
La stringa di simboli che lo segue, oltre alla testa della produzione in questione, risultano utili per il calcolo dei FOLLOW set.

```prolog
% following(Nonterminal, String, Head)
following(X, S, H) :- production(H, B), append(_, [X | S], B), \+ terminal(X).

% follow(Nonterminal, EntryOr1)
follow(X, 1) :- start_symbol(X).
follow(X, A) :- following(X, S, _), first_str(S, A), A \== 0.
follow(X, A) :- following(X, S, H), H \== X, first_str(S, 0), follow(H, A).
```

Sulla base di questi avviene il calcolo delle singole celle della tabella di parsing.
Risolvendo il goal generico `parsing_table_cell(X, A, B)` è possibile ottenere il contenuto di ciascuna cella non vuota.
Lato Scala, è sufficiente iterare le soluzioni e raccogliere i valori sotto forma di mappa con chiavi composte.

```prolog
% parsing_cell(Nonterminal, TerminalOr1, Body)
parsing_table_cell(X, A, B) :- production(X, B), first_str(B, A), A \== 0.
parsing_table_cell(X, A, B) :- production(X, B), first_str(B, 0), follow(X, A).
```

La risoluzione del goal finale richiede che siano inclusi nella base di conoscenza le informazioni riguardo la specifica grammatica.
In particolare, deve essere indicato il simbolo di partenza tramite `start_symbol/1`, l'insieme di terminali tramite `terminal/1` per ciascuno e, infine, le produzioni tramite `production/2` con testa e corpo per ciascuna.

Il metodo `ParsingTable.compute` comincia con questa fase di estensione della base di conoscenza.
Per poter essere utilizzati dall'engine, gli oggetti della grammatica devono essere registrati.
Questo processo restituisce oggetti combinabili per formare i termini composti necessari.
Una volta formato e risolto il goal per la tabella di parsing, gli stessi oggetti possono essere estratti dalle soluzioni.

### Binding per Prolog

L'interazione Scala-Prolog viene regolata da un insieme di metodi raccolti in `Scala2P`.
Istanze di questo tipo racchiudono un riferimento ad un engine sottostante e vengono richieste come parametri di tipo `using` nei metodi forniti.
Design e implementazione di queste funzionalità sono guidati dal requisito di qualità interna del sistema (vedi I1), piuttosto che da rigidi requisiti funzionali o criteri di dominio.
Inoltre, le funzionalità implementate sono limitate alle funzionalità richieste dal problema centrale, pertanto ci si limita a mostrarne l'utilizzo pratico.

```scala
private lazy val engine = engineWithTheoryFile(/* ... */)
given Scala2P = engine

def compute(grammar: ProcessedGrammar): ParsingTable =
  registerScope:
    withKnowledge(grammarKnowledge(grammar)): () =>
      val X = variable("X"); val A = variable("A"); val B = variable("B")
      val parsingTableGoal = compoundTerm("parsing_table_cell", X, A, B)
      parsingTableGoal.solveAll.collectSuccess: s =>
        val row = s.getRegistered[AnyNonterminal](X)
        val col = s.get(A):
          case Registered[Terminal](t) => t
          case Int(1) => Eoi
        val value = s.getRegisteredList[AnySymbol](B)
        (row, col) -> value
      .toMap

private def grammarKnowledge(g: ProcessedGrammar)(using scope: RegisterScope) =
  given TermConversion[AnySymbol] = register
  val t = g.terminals.filter(!_.isSkipped).map: t =>
    compoundTerm("terminal", t)
  val p = g.productions.mapEntries: (head, body) =>
    compoundTerm("production", head, body)
  val s = compoundTerm("start_symbol", g.startSymbol)
  t ++ p :+ s
```

In tale estratto, relativo alla costruzione della tabella di parsing, si osservi l'utilizzo dei seguenti metodi:
* `engineWithTheoryFile`: inizializzazione dell'engine con una teoria di base.
* `registerScope`: dichiarazione di uno scope in cui è abilitato il metodo `register`, con pulizia finale automatica.
* `withKnowledge`: estensione della teoria dell'engine, per la durata dell'azione interna.
* `variable` e `compoundTerm`: creazione di termini Prolog.
* `Term.solveAll`: risoluzione di un goal e raggruppamento di tutte le soluzioni in un `Iterable`, ispirato alla versione vista durante il corso.
* `Iterable[SolveInfo].collectSuccess`: filtraggio delle soluzioni valide e trasformazione in istanze di uno specifico tipo.
* `SolveInfo.getRegistered`: estrazione di un oggetto con uno specifico tipo associato ad una variabile all'interno della soluzione.
* `SolveInfo.get`: estrazione di un valore in base a più possibili casi.
* `Registered.unapply`: caso di estrazione per oggetti con uno specifico tipo.
* `Int.unapply`: caso di estrazione per numeri interi.
* `SolveInfo.getRegisteredList`: estrazione di una lista Prolog per ottenere una collezione di oggetti con uno specifico tipo.
* `register`: registrazione di un oggetto all'interno dell'ambiente Prolog, restituendo il termine generato.

### Grammatica FINF

Si mostra di seguito un estratto della grammatica prodotta per il caso d'uso FINF, in modo da evidenziare l'espressività del DSL.

```scala
val program = -> (
  (LET ++ topDeclaration.* ++ IN).? ++ expression ++ SEMI
)
val topDeclaration = -> (
  recordDeclaration
| functionDeclaration
| valueDeclaration
)
/* ... */
val functionDeclaration = -> (
  functionSignature ++ functionBody
)
val functionSignature = -> (
  FUN ++ ID ++ COLON ++ typeRef ++ LPAREN ++ parameterList ++ RPAREN
)
val functionBody = -> (
  (LET ++ commonDeclaration.* ++ IN).? ++ expression ++ SEMI
)
/* ... */
val parameterList = -> (
  ( ID ++ COLON ++ typeRef ++ (COMMA ++ ID ++ COLON ++ typeRef).* ).?
)
/* ... */
val PLUS    = -> ("+")
val MINUS   = -> ("-")
/* ... */
val LET     = -> ("let")
val IN      = -> ("in")
/* ... */
val DIGITS  = -> ("0|[1-9][0-9]*".r)
val ID      = -> ("[a-zA-Z][a-zA-Z0-9]*".r)
val WHITESP = -> ("[\t \r\n]+".r, skip = true)
val COMMENT = -> ("""/\*.*?\*/""".r, skip = true)
```

## Merighi Daniele

File prodotti: `grammar/Element`, `grammar/Grammar`, `parser/Parser`, `parser/Parsing`, `parser/ParseError`, `Demo`, `ScaLL`.

Test prodotti: `grammar/GrammarTest`, `parser/ParserTest`, `parser/ParsingTest`.

Il mio contributo si è basato sui costrutti del DSL con cui l'utilizzatore descrive la propria grammatica e sull'algoritmo di parsing LL(1), comprensivo della costruzione del CST e del recupero degli errori.
In aggiunta, la demo a riga di comando mostra il montaggio completo della pipeline dal punto di vista di chi utilizza la libreria. 

### Costrutti del DSL
La definizione di una grammatica avviene estendendo il tipo `Grammar`, che espone come membri protetti le due varianti dell'operatore `->`.
Entrambe ricevono in modo contestuale il nome dell'identificatore che le invoca, catturato in fase di compilazione dalla libreria `sourcecode`: l'utilizzatore non ripete mai il nome del simbolo come stringa. 
La variante per i nonterminali riceve il corpo della regola _by-name_ e lo conserva come funzione, rimandandone la valutazione al momento in cui la grammatica verrà percorsa.
La variante per i terminali uniforma stringhe ed espressioni regolari a queste ultime e registra il terminale nella collezione ordinata mantenuta dalla grammatica.
```scala
open class Grammar:
  private var _terminals = Vector.empty[Terminal]

  protected def ->(body: => Element)(using name: sourcecode.Name): Nonterminal =
    Nonterminal(name.value, () => body)

  protected def ->(pattern: String | Regex, skip: Boolean = false)(using name: sourcecode.Name): Terminal =
    val regex = pattern match
      case s: String => Regex.quote(s).r
      case r: Regex  => r
    register(Terminal(name.value, regex, skip))
```
Gli operatori EBNF sono metodi di estensione su `Element`, i cui casi corrispondono uno a uno ai costrutti della notazione.
La precedenza degli operatori, discussa in fase di design, si traduce in pratica così: `a ++ b | c ++ d` viene letta come `(a ++ b) | (c ++ d)`, senza parentesi e con la stessa semantica dell'EBNF.
```scala
extension (element: Element)
  def ++(other: Element): Concat = Concat(element, other)
  def |(other: Element): Alternation = Alternation(element, other)
  def ? : Optional = Optional(element)
  def * : ZeroOrMore = ZeroOrMore(element)
  def + : OneOrMore = OneOrMore(element)

  def show: String = element match
    case Eps => "\u03b5"
    case Terminal(name, _, _) => name
    case Concat(first, second) => s"${first.showInConcat} ${second.showInConcat}"
    /* ... */
```
Il metodo `show` ricostruisce la notazione EBNF a partire dall'albero, delegando a due varianti private l'inserimento delle parantesi solo dove la precedenza lo richieda.

### Parser LL(1)
L'algoritmo in fase di design è definito in termini di una pila di simboli da riconoscere.
La pila prevista dall'algoritmo è quella delle chiamate: `parseSymbol` e `parseSequence` sono mutuamente ricorsive.
Il metodo `expand` concentra l'intero contenuto nella tabella di transizione, restituendo `None` quando nessuna mossa è applicabile.
Questa scelta di tipo rende il flusso di recupero una semplice alternativa al caso nominale, espressa da `getOrElse`, e non un ramo di controllo separato.
```scala
private def parseSymbol(symbol: AnySymbol)(using sync: Sync): Parsing[Seq[CSTNode]] =
  lookahead.flatMap: next =>
    expand(symbol, next).getOrElse:
      for
        _ <- record(unexpected(symbol, next))
        junk <- skipUntil(symbol.starters union sync)
        resumed <- lookahead
        node <- expand(symbol, resumed).getOrElse(pure(Seq(ErrorNode(symbol.starters, junk))))
      yield node

private def expand(symbol: AnySymbol, next: Option[Token.Valid])(using Sync): Option[Parsing[Seq[CSTNode]]] =
  (symbol, next) match
    case (terminal: Terminal, Some(token)) if token.terminal == terminal =>
      Some(advance andThen pure(Seq(LeafNode(token))))
    case (nonterminal: AnyNonterminal, _) =>
      table.get((nonterminal, next.terminal)).map: production =>
        parseSequence(production).map(nodesFor(nonterminal, _))
    case _ => None
```
Si osservi che il riconoscimento di un simbolo produce una sequenza di nodi e non un singolo nodo.
L'appiattimento previsto dal design è interamente contenuto in `nodesFor`, che sfrutta la firma a sequenza: non esiste una fase successiva che ripulisca l'albero.
```scala
private def nodesFor(nonterminal: AnyNonterminal, children: Seq[CSTNode]): Seq[CSTNode] =
  nonterminal match
    case rule: Nonterminal => Seq(RuleNode(rule, children))
    case _: InternalNonterminal => children
```
### Monade di parsing
L'analisi deve propagare lo stream residuo e accumulare gli errori durante l'intera ricorsione. 
Anziché passare esplicitamente entrambi in ogni firma, si introduce il tipo `Parsing`, una funzione dallo stream residuo a un risultato che ne riporta il valore prodotto, lo stream rimanente e gli errori incontrati. 
La sua `flatMap` inoltra lo stream dal primo passo al secondo e concatena gli errori.
```scala
private case class Step[A](value: A, rest: LazyList[Token], errors: Seq[ParseError])

private case class Parsing[A](run: LazyList[Token] => Step[A]):
  def flatMap[B](next: A => Parsing[B]): Parsing[B] = Parsing: tokens =>
    val step = run(tokens)
    val continuation = next(step.value).run(step.rest)
    continuation.copy(errors = step.errors concat continuation.errors)

  def map[B](f: A => B): Parsing[B] = flatMap(value => pure(f(value)))
  infix def andThen[B](second: => Parsing[B]): Parsing[B] = flatMap(_ => second)

private object Parsing:
  def pure[A](value: A): Parsing[A] = Parsing(Step(value, _, Seq.empty))
  def record(error: ParseError): Parsing[Unit] = Parsing(Step((), _, Seq(error)))
  def peek: Parsing[Option[Token]] = Parsing(tokens => Step(tokens.headOption, tokens, Seq.empty))
  def advance: Parsing[Unit] = Parsing(tokens => Step((), tokens.drop(1), Seq.empty))
```
Le quattro operazioni primitive sono le sole a costruire direttamente uno `Step`. Ogni altro metodo del parser le esprime componendole, tramite for-comprehension o `andThen` quando il valore intermedio non è rilevante.

### Recupero e segnalazione degli errori
L'insieme di sincronizzazione dipende dal contesto in cui il simbolo corrente è stato espanso ed è quindi un attributo ereditato.
Il parametro contestuale previsto dal design è dichiarato `using sync: Sync` e ricompare esplicitamente in un punto solo, `parseSequence`, dove viene esteso con i terminali iniziali dei simboli rimanenti.
```scala
private def parseSequence(symbols: SymbolSeq)(using sync: Sync): Parsing[Seq[CSTNode]] =
  symbols match
    case Seq() => pure(Seq.empty)
    case symbol +: rest =>
      for
        node <- parseSymbol(symbol)(using sync union rest.starters)
        others <- parseSequence(rest)
      yield node ++ others

extension (symbol: AnySymbol)
  private def starters: Sync = symbol match
    case terminal: Terminal => Set(terminal)
    case nonterminal: AnyNonterminal =>
      table.keys.collect { case (`nonterminal`, terminal) => terminal }.toSet
```
I terminali iniziali di un simbolo non vengono ricalcolati, ma letti dalle chiavi della tabella di parsing già costruita: una cella è definita esattamente per i terminali per cui quel simbolo può cominciare.

Lo scarto dei token e l'assorbimento degli errori lessicali sono due ricorsioni distinti sulla medesima primitiva. In particolare `lookahead` non restituisce mai un token di errore: lo registra, lo scarta e riprova, così che il resto del parser possa ragionare unicamente su token validi.
```scala
private def lookahead: Parsing[Option[Token.Valid]] =
  peek.flatMap:
    case Some(error: Token.Error) => record(LexicalError(error)) andThen advance andThen lookahead
    case next => pure(next.collect { case valid: Token.Valid => valid })

private def skipUntil(resume: Sync): Parsing[Seq[Token.Valid]] =
  lookahead.flatMap:
    case Some(token) if !resume.contains(token.terminal) =>
      advance andThen skipUntil(resume).map(token +: _)
    case _ => pure(Seq.empty)
```
### Analizzatore e demo
La demo a riga di comando analizza uno o più file `.finf` e ne riporta l'esito.
Non monta la pipeline, la richiede alla libreria sotto forma di analizzatore, costruito una sola volta e riutilizzato per ciascun file.
```scala
type Analyzer[A] = String => AnalysisReport[A]

def analyzer[A](grammar: Grammar, startSymbol: Nonterminal)(using decoder: AstDecoder[A]): Analyzer[A] =
  val lexer = Lexer(grammar.terminals)
  val processed = ProcessedGrammar.of(grammar, startSymbol)
  val table = ParsingTable.compute(processed)
  val parser = Parser(table, startSymbol)
  input =>
    val tokens = lexer.tokenize(input)
    val ParseReport(parseTree, parseErrors) = parser.parseAll(tokens)
    AnalysisReport(parseTree, parseErrors)
```
## Turchi Jacopo

File prodotti: `lexer/Lexer`, `lexer/Position`, `lexer/Token`, `ast/AstDecoder`, `ast/AstError`, `ast/CSTNode`, `ast/TypedExtractors`, `finf/ast/FinfNode`, `finf/ast/FinfDecoder`.

Test prodotti: `lexer/LexerLongestPrefixMatchTest`, `lexer/LexerPositionTrackingTest`, `lexer/LexerPrefixMatchFilterTest`, `ast/AstDecoderTest`, `ast/CSTNodeTest`, `ast/ExtractorsTest`.

Il mio contributo si è concentrato sullo sviluppo della pipeline di front-end per l'analisi lessicale 
e sull'implementazione del motore di decodifica da CST ad AST. 
Inoltre mi sono occupato di realizzare le regole di costruzione dell'AST, a partire dal CST, per il linguaggio FINF.
L'implementazione traduce i requisiti di assenza di side-effect e disaccoppiamento 
previsti nel design avvalendosi di costrutti avanzati di Scala 3, 
quali la valutazione pigra, la contextual abstraction e il design monadico.

### Lexer

L'analizzatore lessicale è stato progettato per operare in modo completamente privo di stato mutabile.
L'avanzamento lungo la stringa di input è tracciato tramite l'allocazione di istanze immutabili della classe Cursor.
Per garantire che il lexer consumi memoria ed esegua elaborazioni solo quando il parser richiede nuovi token, 
l'iterazione sull'input è implementata sfruttando il costrutto `LazyList.unfold`:

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

La funzione `nextValid` incapsula la ricorsione tail (@tailrec) e la logica di avanzamento. 
Tramite _unfold_, la lista pigra viene popolata generando il seed successivo (_Cursor_) in base al match corrente,
garantendo scalabilità anche su sorgenti di grandi dimensioni.
La risoluzione delle ambiguità (Longest-prefix-match) è implementata in stile dichiarativo
mediante una pipeline funzionale che fa uso di _flatMap_, _maxByOption_ e combinatori su tuple, 
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

L'estrazione del match è delega a `matchPrefixAt`, 
implementato come extension method sull'entità Terminal nel companion object Lexer. 
Questo metodo incapsula l'interoperabilità con le espressioni regolari della libreria standard `java.util.regex.Matcher`, 
verificando la corrispondenza esatta a partire dall'offset corrente tramite il metodo `lookingAt()`. 
I risultati vengono mappati in tuple contenenti il token valido e l'indice di dichiarazione. 
La risoluzione dei conflitti posizionali si riduce all'operazione 
`maxByOption((token, index) => (token.lexeme.length, -index))`, 
dove l'uso del segno negativo sull'indice garantisce che, a parità di lunghezza del lessema,
venga selezionato il terminale dichiarato per primo.

### AstDecoder

Per trasformare il codice da CST a AST serve una serie di passaggi concatenati che rischiano di bloccarsi al minimo errore.
Per questo, il sistema deve offrire degli strumenti che permettano agli utenti di personalizzare il processo.
Questa problematica è stata risolta implementando il pattern architetturale delle monadi attraverso il trait AstDecoder[A].
Il decoder espone le funzioni di ordine superiore _map_ e _flatMap_ 
che permettono di comporre decodificatori elementari in pipeline type-safe:

```scala
trait AstDecoder[A]:
  self =>
  def decode(node: CSTNode): Either[AstError, A]
  def map[B](f: A => B): AstDecoder[B] = node =>
    self.decode(node).map(f)
  def flatMap[B](f: A => AstDecoder[B]): AstDecoder[B] = node =>
    self.decode(node).flatMap(a => f(a).decode(node))
  def orElse[B >: A](fallback: => AstDecoder[B]): AstDecoder[B] = node =>
    self.decode(node).orElse(fallback.decode(node))
```

Per ridurre il boilerplate e agevolare l'uso dell'API, 
il design sfrutta le contextual abstractions (using e given) 
e gli extension methods nel companion object AstDecoder. 
Il metodo di estensione `as[A]` funge da entry-point contestuale: invocando direttamente `node.as[Expr]`, 
si delega al compilatore la risoluzione implicita della strategia di decodifica appropriata per il tipo atteso.

Un aspetto implementativo cruciale riguarda l'elaborazione delle sequenze piatte generate dal parsing LL(1). 
Per gestire e raggruppare queste liste di nodi senza ricorrere a mutabilità, 
il companion object introduce la funzione `decodeSequence`:

```scala
def decodeSequence[A](nodes: Seq[CSTNode])(extractChunk: PartialFunction[Seq[CSTNode], (Either[AstError, A], Seq[CSTNode])]): Either[AstError, Seq[A]] =
  if nodes.isEmpty then Right(Seq.empty)
  else extractChunk.lift(nodes) match
    case Some((decodedElement, remainingNodes)) =>
      for
        element <- decodedElement
        decodedRest <- decodeSequence(remainingNodes)(extractChunk)
      yield element +: decodedRest
    case None => Left(AstError.DecodingError("Invalid sequence structure"))
```

Questa funzione accetta una _PartialFunction_ definita dall'utente 
per isolare ed estrarre iterativamente segmenti logici significativi di nodi CST (chunk).
Grazie all'uso combinato di `lift(nodes)` (che converte la funzione parziale in un'estrazione sicura basata su _Option_) e della for-comprehension, 
i segmenti estratti vengono decodificati e concatenati ricorsivamente nella collezione AST finale.
Se il blocco di nodi non corrisponde ad alcun pattern valido,
la ricorsione fallisce in modo type-safe restituendo un `AstError.DecodingError`.

Infine, l'estensione `decodeAll[A]` applicata a `Seq[CSTNode]` implementa la logica di aggregazione esaustiva degli errori:

```scala
extension (nodes: Seq[CSTNode])
  def decodeAll[A](using decoder: AstDecoder[A]): Either[AstError, Seq[A]] =
    val (errors, validNodes) = nodes.partitionMap(_.as[A])
    errors match
      case Seq() => Right(validNodes)
      case Seq(single) => Left(single)
      case multiple => Left(AstError.AggregateError(multiple))
```

Utilizzando `partitionMap`, il sistema non si interrompe al primo errore, ma valuta l'intera collezione. 
In presenza di difetti multipli (es. una sequenza di dichiarazioni indipendenti malformate), 
i fallimenti vengono accumulati restituendo un `AggregateError` per una diagnostica simultanea.

### Estrattori tipizzati

Il disaccoppiamento tra la struttura vincolante del CSTNode (regole, foglie, errori)
e il pattern matching utente è stato ottenuto creando estrattori custom tipizzati (_unapply_ / _unapplySeq_).
In `TypedExtractors`, il meccanismo di estrazione nativo di Scala viene potenziato
definendo gli estrattori direttamente come extension methods sui tipi grammaticali Nonterminal e Terminal.

```scala
extension (symbol: Nonterminal)
  def unapplySeq(node: CSTNode): Option[Seq[CSTNode]] = node match
    case CSTNode.RuleNode(s, children) if s.name == symbol.name => Some(children)
    case _ => None
```

Questo meccanismo avanzato permette di utilizzare i terminali e i nonterminali della grammatica (come `VAL` o `valueDeclaration`) 
direttamente come case classes all'interno dei blocchi match, 
astraendo il programmatore dal dover navigare manualmente i RuleNode e i LeafNode del CST. [\[esempio di implementazione in FINF Decoder\]](#finf-decoder-code)

### FINF decoder

La logica astratta di decodifica trova applicazione pratica nella costruzione dell'AST per il linguaggio FINF.
I concetti del dominio sono modellati come Algebraic Data Types nel file `FinfNode` 
(es. i sealed trait _Expr_ e _Declaration_ estesi dalle relative case class).
La mappatura strutturale in `FinfDecoder` avviene istanziando il type class AstDecoder tramite blocchi _given_. 
Integrando gli estrattori tipizzati custom e la for-comprehension, la decodifica nasconde completamente la complessità del CST:

<a id="finf-decoder-code"></a>
```scala
given declDecoder: AstDecoder[Declaration] with
  def decode(node: CSTNode): Either[AstError, Declaration] = node match
    case topDeclaration(declNode)    => declNode.as[Declaration]
    case commonDeclaration(declNode) => declNode.as[Declaration]
    case valueDeclaration(VAL(_), ID(valName), COLON(_), typeNode, ASSIGN(_), valueNode, SEMI(_)) =>
      for
        decodedType  <- typeNode.as[TypeRef]
        decodedValue <- valueNode.as[Expr]
      yield ValDecl(valName, decodedType, decodedValue)
    /* ... */
    case _ => Left(AstError.UnexpectedNodeStructure(topDeclaration, node))
```

Come si osserva, il matching su `valueDeclaration` cattura selettivamente solo i nodi semanticamente rilevanti, 
ignorando token sintattici e punteggiatura grazie all'uso dei placeholder _ sui terminali (es. VAL(_), COLON(_)). 
La for-comprehension coordina l'estrazione ricorsiva sfruttando l'extension method `.as[T]`. 
Dato che l'AstDecoder opera come una monade su Either, 
la sequenza propaga automaticamente lo short-circuiting al primo fallimento su un sotto-nodo, 
garantendo costruzioni strettamente tipizzate e sicure.
