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


## Decodifica CST-AST