# Retrospettiva

Il gruppo ha adottato il processo di sviluppo, descritto nel primo capitolo, per tutta la durata del progetto.
I punti di forza di questo approccio sono stati:
* **Sprint**: Definire, a ogni meeting, task chiare e prendere decisioni condivise sulla direzione nelle scelte di design,
  ha permesso al team di rimanere sincronizzato e coeso.
* **Pull request**: insieme a scambi frequenti di informazioni, hanno permesso di ottenere gli stessi vantaggi 
  che l'uso di meeting brevi e quotidiani concederebbe normalmente nell'approccio scrum classico.
* **GitFlow**: l'utilizzo di branch separati per l'implementazione di nuove feature o di fix
  ha concesso di minimizzare i conflitti, mantenendo un git tree pulito.

### Sprint 1

Durante il primo sprint abbiamo analizzato il dominio della nostra libreria, 
al fine di realizzare il product backlog.
Successivamente abbiamo deciso come organizzare i successivi sprint.
Inoltre è stato scritto il primo sprint backlog, concentrandoci sugli item a priorità maggiore.
Le rispettive task sono state poi assegnate agli sviluppatori.

### Sprint 2

Prima di iniziare con il secondo sprint, una parte del tempo della riunione è stato dedicato
allo stabilire come dividere in cartelle la libreria.
Successivamente, non essendo rimaste task inconcluse dal precedente sprint,
è stato redatto il nuovo sprint backlog e assegnate di conseguenza le task agli sviluppatori.
I membri che dovevano stabilire interfacce per ADT comuni, si sono organizzati in separata sede
con riunioni secondarie.

### Sprint 3

Dopo un resoconto dell'andamento dello sprint precedente, sono nati dei nuovi task per 
rifinire alcuni elementi sviluppati. Questi compiti sono stati integrati ai nuovi
nello sprint backlog 3 e assegnati come in precedenza.

### Sprint 4

Durante lo sprint 4, dopo un breve confronto,
ci siamo resi conto che sarebbe utile sviluppare una CLI.
Inoltre, essendo che il prodotto rispettava quasi tutti i requisiti descritti, si è deciso di concentrare le energie
sul controllare e documentari i file, oltre al terminare gli ultimi task previsti.
Infine, è stato assegnato il compito di realizzare il fat jar per la release del codice.

---

Il product backlog e i vari sprint backlog sono disponibili in [backlogs.md](https://github.com/PastaCodes/PPS-25-scall/blob/main/process/backlogs.md).

## Commenti finali

Quest'esperienza ci ha fatto apprezzare e allo stesso tempo detestare certi aspetti della metodologia scrum.
Per quanto possa essere utile per mantenere il gruppo sincronizzato e sul pezzo, presenta anche
delle pesantezze ritenute un po' forzate che smorzano il flusso lavorativo dello sviluppatore.
<br>
Similmente anche il TDD, risultato molto utile per ridurre il numero di errori e bug
ritrovati nel software, ha però allungato i tempi di sviluppo.
<br>
Un aspetto particolarmente positivo del progetto ha riguardato la programmazione funzionale.
È stata una boccata d'aria fresca nel nostro modo di pensare, risultando in un'occasione di provare
qualcosa di nuovo e motivante.
<br>
Il dominio applicativo, nonostante fosse stato scelto all'unanimità a inizio progetto,
ha fatto nascere col tempo opinioni differenti.
<br>
Infine, per quanto riguarda la programmazione logica, è risultata essere una challenge stimolante,
penalizzata soltanto dalla libreria difficilmente utilizzabile.

---

[Indice](../index.md) |
[Capitolo precedente](../6_testing/index.md)
