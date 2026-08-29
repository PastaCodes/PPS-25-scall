# Processo di sviluppo

Il gruppo adotta come processo di sviluppo un approccio scrum-like adattato alla dimensione ridotta del team.
Il lavoro è organizzato in quattro sprint, ciascuno dei quali ha una durata di dieci giorni.
Si pianifica di effettuare i primi due sprint nel mese di luglio e gli ultimi due nel mese di agosto, 
tenendo conto degli impegni personali e accademici del gruppo.

L’organizzazione dei membri all’interno del processo di sviluppo ha previsto i seguenti ruoli:
* Esperto del dominio, assunto da Marco Buda
* Product Owner, assunto da Jacopo Turchi
* Scrum Master, assunto da Daniele Merighi

Nel primo meeting viene redatto il product backlog, mantenendo distinte le feature fondamentali da quelle opzionali.
Successivamente per ogni sprint viene effettuata una riunione in cui:
* si effettua lo sprint review.
* si analizzano i task avanzati dallo sprint precedente e quelli restanti. 
* si stende lo sprint backlog.
* si suddividono i task in maniera tale che ogni sviluppatore impieghi circa 15 ore di lavoro per completare il suo compito.
* si scrive un breve report sull'incontro.

## Metodologie di sviluppo
Viene seguito, come visto a lezione col Prof. Pianini, un approccio semplificato del git flow.
Il processo di sviluppo si basa su un branch di `develop` da cui si diramano i vari branch di featuring e fix.
Una volta raggiunta una versione stabile del codice viene effettuato il merge di quest'ultimo nel branch `main`.
I messaggi di commit seguono lo standard _Conventional Commits_ nella forma `<type>[optional scope]: <description>`.

Vengono adottati anche alcuni costrutti del GitHub flow, come le pull request e le issue, al fine di revisionare il codice.
Ognuna deve essere approvata da un membro diverso dall'autore, che si occupa di unire il codice su `develop`.
Questo approccio agevola lo sviluppo contemporaneo da parte di più persone.

## Strumenti utilizzati
* Scala
* Prolog
* Sbt
* Git

