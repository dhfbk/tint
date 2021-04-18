import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLUOutputter;
import eu.fbk.dh.kd.annotator.DigiKDAnnotations;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintServer;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

import java.util.Properties;

public class KDTest {
    public static void main(String[] args) {
        String text = "Il primo pensiero che vorrei condividere, nel chiedere la vostra fiducia, riguarda la nostra responsabilità nazionale. Il principale dovere cui siamo chiamati, tutti, io per primo come Presidente del Consiglio, è di combattere con ogni mezzo la pandemia e di salvaguardare le vite dei nostri concittadini. Una trincea dove combattiamo tutti insieme. Il virus è nemico di tutti. Ed è nel commosso ricordo di chi non c’è più che cresce il nostro impegno. Prima di illustrarvi il mio programma, vorrei rivolgere un altro pensiero, partecipato e solidale, a tutti coloro che soffrono per la crisi economica che la pandemia ha scatenato, a coloro che lavorano nelle attività più colpite o fermate per motivi sanitari. Conosciamo le loro ragioni, siamo consci del loro enorme sacrificio e li ringraziamo. Ci impegniamo a fare di tutto perché possano tornare, nel più breve tempo possibile, nel riconoscimento dei loro diritti, alla normalità delle loro occupazioni. Ci impegniamo a informare i cittadini con sufficiente anticipo, per quanto compatibile con la rapida evoluzione della pandemia, di ogni cambiamento nelle regole.\n" +
                "\n" +
                "Il Governo farà le riforme ma affronterà anche l’emergenza. Non esiste un prima e un dopo. Siamo consci dell’insegnamento di Cavour:”… le riforme compiute a tempo, invece di indebolire l’autorità, la rafforzano”. Ma nel frattempo dobbiamo occuparci di chi soffre adesso, di chi oggi perde il lavoro o è costretto a chiudere la propria attività.\n" +
                "\n" +
                "Nel ringraziare, ancora una volta il Presidente della Repubblica per l’onore dell’incarico che mi è stato assegnato, vorrei dirvi che non vi è mai stato, nella mia lunga vita professionale, un momento di emozione così intensa e di responsabilità così ampia. Ringrazio altresì il mio predecessore Giuseppe Conte che ha affrontato una situazione di emergenza sanitaria ed economica come mai era accaduto dall’Unità d’Italia.   \n" +
                "\n" +
                "Si è discusso molto sulla natura di questo governo. La storia repubblicana ha dispensato una varietà infinita di formule. Nel rispetto che tutti abbiamo per le istituzioni e per il corretto funzionamento di una democrazia rappresentativa, un esecutivo come quello che ho l’onore di presiedere, specialmente in una situazione drammatica come quella che stiamo vivendo, è semplicemente il governo del Paese. Non ha bisogno di alcun aggettivo che lo definisca. Riassume la volontà, la consapevolezza, il senso di responsabilità delle forze politiche che lo sostengono alle quali è stata chiesta una rinuncia per il bene di tutti, dei propri elettori come degli elettori di altri schieramenti, anche dell’opposizione, dei cittadini italiani tutti. Questo è lo spirito repubblicano di un governo che nasce in una situazione di emergenza raccogliendo l’alta indicazione del capo dello Stato. \n" +
                "\n" +
                "La crescita di un’economia di un Paese non scaturisce solo da fattori economici. Dipende dalle istituzioni, dalla fiducia dei cittadini verso di esse, dalla condivisione di valori e di speranze. Gli stessi fattori determinano il progresso di un Paese. \n" +
                "\n" +
                "Si è detto e scritto che questo governo è stato reso necessario dal fallimento della politica. Mi sia consentito di non essere d’accordo. Nessuno fa un passo indietro rispetto alla propria identità ma semmai, in un nuovo e del tutto inconsueto perimetro di collaborazione, ne fa uno avanti nel rispondere alle necessità del Paese, nell’avvicinarsi ai problemi quotidiani delle famiglie e delle imprese che ben sanno quando è il momento di lavorare insieme, senza pregiudizi e rivalità. \n" +
                "\n" +
                "Nei momenti più difficili della nostra storia, l’espressione più alta e nobile della politica si è tradotta in scelte coraggiose, in visioni che fino a un attimo prima sembravano impossibili. Perché prima di ogni nostra appartenenza, viene il dovere della cittadinanza. \n" +
                "Siamo cittadini di un Paese che ci chiede di fare tutto il possibile, senza perdere tempo, senza lesinare anche il più piccolo sforzo, per combattere la pandemia e contrastare la crisi economica. E noi oggi, politici e tecnici che formano questo nuovo esecutivo siamo tutti semplicemente cittadini italiani, onorati di servire il proprio Paese, tutti ugualmente consapevoli del compito che ci è stato affidato. \n" +
                "\n" +
                "Questo è lo spirito repubblicano del mio governo. \n" +
                "\n" +
                "La durata dei governi in Italia è stata mediamente breve ma ciò non ha impedito, in momenti anche drammatici della vita della nazione, di compiere scelte decisive per il futuro dei nostri figli e nipoti. Conta la qualità delle decisioni, conta il coraggio delle visioni, non contano i giorni. Il tempo del potere può essere sprecato anche nella sola preoccupazione di conservarlo. Oggi noi abbiamo, come accadde ai governi dell’immediato Dopoguerra, la possibilità, o meglio la responsabilità, di avviare una Nuova Ricostruzione. L’Italia si risollevò dal disastro della Seconda Guerra Mondiale con orgoglio e determinazione e mise le basi del miracolo economico grazie a investimenti e lavoro. Ma soprattutto grazie alla convinzione che il futuro delle generazioni successive sarebbe stato migliore per tutti. Nella fiducia reciproca, nella fratellanza nazionale, nel perseguimento di un riscatto civico e morale. A quella Ricostruzione collaborarono forze politiche ideologicamente lontane se non contrapposte. Sono certo che anche a questa Nuova Ricostruzione nessuno farà mancare, nella distinzione di ruoli e identità, il proprio apporto. Questa è la nostra missione di italiani: consegnare un Paese migliore e più giusto ai figli e ai nipoti. \n" +
                "\n" +
                "Spesso mi sono chiesto se noi, e mi riferisco prima di tutto alla mia generazione, abbiamo fatto e stiamo facendo per loro tutto quello che i nostri nonni e padri fecero per noi, sacrificandosi oltre misura. È una domanda che ci dobbiamo porre quando non facciamo tutto il necessario per promuovere al meglio il capitale umano, la formazione, la scuola, l’università e la cultura. Una domanda alla quale dobbiamo dare risposte concrete e urgenti quando deludiamo i nostri giovani costringendoli ad emigrare da un Paese che troppo spesso non sa valutare il merito e non ha ancora realizzato una effettiva parità di genere. Una domanda che non possiamo eludere quando aumentiamo il nostro debito pubblico senza aver speso e investito al meglio risorse che sono sempre scarse. Ogni spreco oggi è un torto che facciamo alle prossime generazioni, una sottrazione dei loro diritti. Esprimo davanti a voi, che siete i rappresentanti eletti degli italiani, l’auspicio che il desiderio e la necessità di costruire un futuro migliore orientino saggiamente le nostre decisioni. Nella speranza che i giovani italiani che prenderanno il nostro posto, anche qui in questa aula, ci ringrazino per il nostro lavoro e non abbiano di che rimproverarci per il nostro egoismo. \n" +
                "\n" +
                "Questo governo nasce nel solco dell’appartenenza del nostro Paese, come socio fondatore, all’Unione europea, e come protagonista dell’Alleanza Atlantica, nel solco delle grandi democrazie occidentali, a difesa dei loro irrinunciabili principi e valori. Sostenere questo governo significa condividere l’irreversibilità della scelta dell’euro, significa condividere la prospettiva di un’Unione europea sempre più integrata che approderà a un bilancio pubblico comune capace di sostenere i Paesi nei periodi di recessione. Gli Stati nazionali rimangono il riferimento dei nostri cittadini, ma nelle aree definite dalla loro debolezza cedono sovranità nazionale per acquistare sovranità condivisa. Anzi, nell’appartenenza convinta al destino dell’Europa siamo ancora più italiani, ancora più vicini ai nostri territori di origine o residenza. Dobbiamo essere orgogliosi del contributo italiano alla crescita e allo sviluppo dell’Unione europea. Senza l’Italia non c’è l’Europa. Ma, fuori dall’Europa c’è meno Italia. Non c’è sovranità nella solitudine. C’è solo l’inganno di ciò che siamo, nell’oblio di ciò che siamo stati e nella negazione di quello che potremmo essere. Siamo una grande potenza economica e culturale. Mi sono sempre stupito e un po’ addolorato in questi anni, nel notare come spesso il giudizio degli altri sul nostro Paese sia migliore del nostro. Dobbiamo essere più orgogliosi, più giusti e più generosi nei confronti del nostro Paese. E riconoscere i tanti primati, la profonda ricchezza del nostro capitale sociale, del nostro volontariato, che altri ci invidiano.  \n" +
                "\n" +
                "Lo stato del Paese dopo un anno di pandemia \n" +
                "\n" +
                "Da quando è esplosa l’epidemia, ci sono stati - i dati ufficiali sottostimano il fenomeno - 92.522 morti, 2.725.106 cittadini colpiti dal virus, in questo momento 2.074 sono i ricoverati in terapia intensiva. Ci sono 259 morti tra gli operatori sanitari e 118.856 sono quelli contagiati, a dimostrazione di un enorme sacrificio sostenuto con generosità e impegno. Cifre che hanno messo a dura prova il sistema sanitario nazionale, sottraendo personale e risorse alla prevenzione e alla cura di altre patologie, con conseguenze pesanti sulla salute di tanti italiani. \n" +
                "\n" +
                "L’aspettativa di vita, a causa della pandemia, è diminuita: fino a 4 - 5 anni nelle zone di maggior contagio; un anno e mezzo - due in meno per tutta la popolazione italiana. Un calo simile non si registrava in Italia dai tempi delle due guerre mondiali. \n" +
                "\n" +
                "La diffusione del virus ha comportato gravissime conseguenze anche sul tessuto economico e sociale del nostro Paese. Con rilevanti impatti sull’occupazione, specialmente quella dei giovani e delle donne. Un fenomeno destinato ad aggravarsi quando verrà meno il divieto di licenziamento.\n" +
                "\n" +
                "Si è anche aggravata la povertà. I dati dei centri di ascolto Caritas, che confrontano il periodo maggio-settembre del 2019 con lo stesso periodo del 2020, mostrano che da un anno all’altro l’incidenza dei “nuovi poveri” passa dal 31% al 45%: quasi una persona su due che oggi si rivolge alla Caritas lo fa per la prima volta. Tra i nuovi poveri aumenta in particolare il peso delle famiglie con minori, delle donne, dei giovani, degli italiani, che sono oggi la maggioranza (52% rispetto al 47,9 % dello scorso anno) e delle persone in età lavorativa, di fasce di cittadini finora mai sfiorati dall’indigenza.\n" +
                "\n" +
                "Il numero totale di ore di Cassa integrazione per emergenza sanitaria dal 1 aprile al 31 dicembre dello scorso anno supera i 4 milioni. Nel 2020 gli occupati sono scesi di 444 mila unità ma il calo si è accentrato su contratti a termine (-393 mila) e lavoratori autonomi (-209). La pandemia finora ha colpito soprattutto giovani e donne, una disoccupazione selettiva ma che presto potrebbe iniziare a colpire anche i lavoratori con contratti a tempo indeterminato.\n" +
                "\n" +
                "Gravi e con pochi precedenti storici gli effetti sulla diseguaglianza. In assenza di interventi pubblici il coefficiente di Gini, una misura della diseguaglianza nella distribuzione del reddito, sarebbe aumentato, nel primo semestre del 2020 (secondo una recente stima), di 4 punti percentuali, rispetto al 34.8% del 2019. Questo aumento sarebbe stato maggiore di quello cumulato durante le due recenti recessioni. L’aumento nella diseguaglianza è stato tuttavia attenuato dalle reti di protezione presenti nel nostro sistema di sicurezza sociale, in particolare dai provvedimenti che dall’inizio della pandemia li hanno rafforzati. Rimane però il fatto che il nostro sistema di sicurezza sociale è squilibrato, non proteggendo a sufficienza i cittadini con impieghi a tempo determinato e i lavoratori autonomi.\n" +
                "\n" +
                "Le previsioni pubblicate la scorsa settimana dalla Commissione europea indicano che sebbene nel 2020 la recessione europea sia stata meno grave di quanto ci si aspettasse - e che quindi già fra poco più di un anno si dovrebbero recuperare i livelli di attività economica pre-pandemia - in Italia questo non accadrà prima della fine del 2022, in un contesto in cui, prima della pandemia, non avevamo ancora recuperato pienamente gli effetti delle crisi del 2008-09 e del 2011-13.\n" +
                "\n" +
                "La diffusione del Covid ha provocato ferite profonde nelle nostre comunità, non solo sul piano sanitario ed economico, ma anche su quello culturale ed educativo. Le ragazze e i ragazzi hanno avuto, soprattutto quelli nelle scuole secondarie di secondo grado, il servizio scolastico attraverso la Didattica a Distanza che, pur garantendo la continuità del servizio, non può non creare disagi ed evidenziare diseguaglianze. Un dato chiarisce meglio la dinamica attuale: a fronte di 1.696.300 studenti delle scuole secondarie di secondo grado, nella prima settimana di febbraio solo 1.039.372 studenti (il 61,2% del totale) ha avuto assicurato il servizio attraverso la Didattica a Distanza. \n" +
                "\n" +
                "Le priorità per ripartire\n" +
                "\n" +
                "Questa situazione di emergenza senza precedenti impone di imboccare, con decisione e rapidità, una strada di unità e di impegno comune.\n" +
                "\n" +
                "Il piano di vaccinazione. Gli scienziati in soli 12 mesi hanno fatto un miracolo: non era mai accaduto che si riuscisse a produrre un nuovo vaccino in meno di un anno. La nostra prima sfida è, ottenutene le quantità sufficienti, distribuirlo rapidamente ed efficientemente. \n" +
                "\n" +
                "Abbiamo bisogno di mobilitare tutte le energie su cui possiamo contare, ricorrendo alla protezione civile, alle forze armate, ai tanti volontari. Non dobbiamo limitare le vaccinazioni all’interno di luoghi specifici, spesso ancora non pronti: abbiamo il dovere di renderle possibili in tutte le strutture disponibili, pubbliche e private. Facendo tesoro dell’esperienza fatta con i tamponi che, dopo un ritardo iniziale, sono stati permessi anche al di fuori della ristretta cerchia di ospedali autorizzati. E soprattutto imparando da Paesi che si sono mossi più rapidamente di noi disponendo subito di quantità di vaccini adeguate. La velocità è essenziale non solo per proteggere gli individui e le loro comunità sociali, ma ora anche per ridurre le possibilità che sorgano altre varianti del virus.\n" +
                "\n" +
                "Sulla base dell’esperienza dei mesi scorsi dobbiamo aprire un confronto a tutto campo sulla riforma della nostra sanità. Il punto centrale è rafforzare e ridisegnare la sanità territoriale, realizzando una forte rete di servizi di base (case della comunità, ospedali di comunità, consultori, centri di salute mentale, centri di prossimità contro la povertà sanitaria). È questa la strada per rendere realmente esigibili i “Livelli essenziali di assistenza” e affidare agli ospedali le esigenze sanitarie acute, post acute e riabilitative. La “casa come principale luogo di cura” è oggi possibile con la telemedicina, con l’assistenza domiciliare integrata.\n" +
                "\n" +
                "La scuola: non solo dobbiamo tornare rapidamente a un orario scolastico normale, anche distribuendolo su diverse fasce orarie, ma dobbiamo fare il possibile, con le modalità più adatte, per recuperare le ore di didattica in presenza perse lo scorso anno, soprattutto nelle regioni del Mezzogiorno in cui la didattica a distanza ha incontrato maggiori difficoltà.\n" +
                "\n" +
                "Occorre rivedere il disegno del percorso scolastico annuale. Allineare il calendario scolastico alle esigenze derivanti dall’esperienza vissuta dall’inizio della pandemia. Il ritorno a scuola deve avvenire in sicurezza.\n" +
                "\n" +
                "È necessario investire in una transizione culturale a partire dal patrimonio identitario umanistico riconosciuto a livello internazionale. Siamo chiamati a disegnare un percorso educativo che combini la necessaria adesione agli standard qualitativi richiesti, anche nel panorama europeo, con innesti di nuove materie e metodologie, e coniugare le competenze scientifiche con quelle delle aree umanistiche e del multilinguismo.\n" +
                "\n" +
                "Infine è necessario investire nella formazione del personale docente per allineare l’offerta educativa alla domanda delle nuove generazioni.\n" +
                "\n" +
                "In questa prospettiva particolare attenzione va riservata agli ITIS (istituti tecnici). In Francia e in Germania, ad esempio, questi istituti sono un pilastro importante del sistema educativo. È stato stimato in circa 3 milioni, nel quinquennio 2019-23, il fabbisogno di diplomati di istituti tecnici nell’area digitale e ambientale. Il Programma Nazionale di Ripresa e Resilienza assegna 1,5 md agli ITIS, 20 volte il finanziamento di un anno normale pre-pandemia. Senza innovare l’attuale organizzazione di queste scuole, rischiamo che quelle risorse vengano sprecate.\n" +
                "\n" +
                "La globalizzazione, la trasformazione digitale e la transizione ecologica stanno da anni cambiando il mercato del lavoro e richiedono continui adeguamenti nella formazione universitaria. Allo stesso tempo occorre investire adeguatamente nella ricerca, senza escludere la ricerca di base, puntando all’eccellenza, ovvero a una ricerca riconosciuta a livello internazionale per l’impatto che produce sulla nuova conoscenza e sui nuovi modelli in tutti i campi scientifici. Occorre infine costruire sull’esperienza di didattica a distanza maturata nello scorso anno sviluppandone le potenzialità con l’impiego di strumenti digitali che potranno essere utilizzati nella didattica in presenza.\n" +
                "\n" +
                "Oltre la pandemia\n" +
                "\n" +
                "Quando usciremo, e usciremo, dalla pandemia, che mondo troveremo? Alcuni pensano che la tragedia nella quale abbiamo vissuto per più di 12 mesi sia stata simile ad una lunga interruzione di corrente. Prima o poi la luce ritorna, e tutto ricomincia come prima. La scienza, ma semplicemente il buon senso, suggeriscono che potrebbe non essere così. \n" +
                "\n" +
                "Il riscaldamento del pianeta ha effetti diretti sulle nostre vite e sulla nostra salute, dall’inquinamento, alla fragilità idrogeologica, all’innalzamento del livello dei mari che potrebbe rendere ampie zone di alcune città litoranee non più abitabili. Lo spazio che alcune megalopoli hanno sottratto alla natura potrebbe essere stata una delle cause della trasmissione del virus dagli animali all'uomo. \n" +
                "\n" +
                "Come ha detto Papa Francesco \"Le tragedie naturali sono la risposta della terra al nostro maltrattamento. E io penso che se chiedessi al Signore che cosa pensa, non credo mi direbbe che è una cosa buona: siamo stati noi a rovinare l'opera del Signore”.\n" +
                "\n" +
                "Proteggere il futuro dell’ambiente, conciliandolo con il progresso e il benessere sociale, richiede un approccio nuovo: digitalizzazione, agricoltura, salute, energia, aerospazio, cloud computing, scuole ed educazione, protezione dei territori, biodiversità, riscaldamento globale ed effetto serra, sono diverse facce di una sfida poliedrica che vede al centro l’ecosistema in cui si svilupperanno tutte le azioni umane. \n" +
                "\n" +
                "Anche nel nostro Paese alcuni modelli di crescita dovranno cambiare. Ad esempio il modello di turismo, un’attività che prima della pandemia rappresentava il 14 per cento del totale delle nostre attività economiche. Imprese e lavoratori in quel settore vanno aiutati ad uscire dal disastro creato dalla pandemia. Ma senza scordare che il nostro turismo avrà un futuro se non dimentichiamo che esso vive della nostra capacità di preservare, cioè almeno non sciupare, città d’arte, luoghi e tradizioni che successive generazioni attraverso molti secoli hanno saputo preservare e ci hanno tramandato.\n" +
                "\n" +
                "Uscire dalla pandemia non sarà come riaccendere la luce. Questa osservazione, che gli scienziati non smettono di ripeterci, ha una conseguenza importante. Il governo dovrà proteggere i lavoratori, tutti i lavoratori, ma sarebbe un errore proteggere indifferentemente tutte le attività economiche. Alcune dovranno cambiare, anche radicalmente. E la scelta di quali attività proteggere e quali accompagnare nel cambiamento è il difficile compito che la politica economica dovrà affrontare nei prossimi mesi.\n" +
                "\n" +
                "La capacità di adattamento del nostro sistema produttivo e interventi senza precedenti hanno permesso di preservare la forza lavoro in un anno drammatico: sono stati sette milioni i lavoratori che hanno fruito di strumenti di integrazione salariale per un totale di 4 miliardi di ore. Grazie a tali misure, supportate anche dalla Commissione europea mediante il programma SURE, è stato possibile limitare gli effetti negativi sull'occupazione. A pagare il prezzo più alto sono stati i giovani, le donne e i lavoratori autonomi. È innanzitutto a loro che bisogna pensare quando approntiamo una strategia di sostegno delle imprese e del lavoro, strategia che dovrà coordinare la sequenza degli interventi sul lavoro, sul credito e sul capitale. \n" +
                "\n" +
                "Centrali sono le politiche attive del lavoro. Affinché esse siano immediatamente operative è necessario migliorare gli strumenti esistenti, come l’assegno di riallocazione, rafforzando le politiche di formazione dei lavoratori occupati e disoccupati. Vanno anche rafforzate le dotazioni di personale e digitali dei centri per l’impiego in accordo con le regioni. Questo progetto è già parte del Programma Nazionale di Ripresa e Resilienza ma andrà anticipato da subito.\n" +
                "\n" +
                "Il cambiamento climatico, come la pandemia, penalizza alcuni settori produttivi senza che vi sia un’espansione in altri settori che possa compensare. Dobbiamo quindi essere noi ad assicurare questa espansione e lo dobbiamo fare subito. \n" +
                "\n" +
                "La risposta della politica economica al cambiamento climatico e alla pandemia dovrà essere una combinazione di politiche strutturali che facilitino l’innovazione, di politiche finanziarie che facilitino l’accesso delle imprese capaci di crescere al capitale e al credito e di politiche monetarie e fiscali espansive che agevolino gli investimenti e creino domanda per le nuove attività sostenibili che sono state create.\n" +
                "\n" +
                "Vogliamo lasciare un buon pianeta, non solo una buona moneta.\n" +
                " \n" +
                "Parità di genere\n" +
                "\n" +
                "La mobilitazione di tutte le energie del Paese nel suo rilancio non può prescindere dal coinvolgimento delle donne. Il divario di genere nei tassi di occupazione in Italia rimane tra i più alti di Europa: circa 18 punti su una media europea di 10. Dal dopoguerra ad oggi, la situazione è notevolmente migliorata, ma questo incremento non è andato di pari passo con un altrettanto evidente miglioramento delle condizioni di carriera delle donne. L’Italia presenta oggi uno dei peggiori gap salariali tra generi in Europa, oltre una cronica scarsità di donne in posizioni manageriali di rilievo.\n" +
                "\n" +
                "Una vera parità di genere non significa un farisaico rispetto di quote rosa richieste dalla legge: richiede che siano garantite parità di condizioni competitive tra generi. Intendiamo lavorare in questo senso, puntando a un riequilibrio del gap salariale e un sistema di welfare che permetta alle donne di dedicare alla loro carriera le stesse energie dei loro colleghi uomini, superando la scelta tra famiglia o lavoro.\n" +
                "\n" +
                "Garantire parità di condizioni competitive significa anche assicurarsi che tutti abbiano eguale accesso alla formazione di quelle competenze chiave che sempre più permetteranno di fare carriera - digitali, tecnologiche e ambientali. Intendiamo quindi investire, economicamente ma soprattutto culturalmente, perché sempre più giovani donne scelgano di formarsi negli ambiti su cui intendiamo rilanciare il Paese. Solo in questo modo riusciremo a garantire che le migliori risorse siano coinvolte nello sviluppo del Paese.\n" +
                "\n" +
                "Il Mezzogiorno\n" +
                "\n" +
                "Aumento dell’occupazione, in primis, femminile, è obiettivo imprescindibile: benessere, autodeterminazione, legalità, sicurezza sono strettamente legati all’aumento dell’occupazione femminile nel Mezzogiorno. Sviluppare la capacità di attrarre investimenti privati nazionali e internazionali è essenziale per generare reddito, creare lavoro, invertire il declino demografico e lo spopolamento delle aree interne. Ma per raggiungere questo obiettivo occorre creare un ambiente dove legalità e sicurezza siano sempre garantite. Vi sono poi strumenti specifici quali il credito d’imposta e altri interventi da concordare in sede europea.\n" +
                "\n" +
                "Per riuscire a spendere e spendere bene, utilizzando gli investimenti dedicati dal Next Generation EU occorre irrobustire le amministrazioni meridionali, anche guardando con attenzione all’esperienza di un passato che spesso ha deluso la speranza.\n" +
                "\n" +
                "Gli investimenti pubblici\n" +
                "\n" +
                "In tema di infrastrutture occorre investire sulla preparazione tecnica, legale ed economica dei funzionari pubblici per permettere alle amministrazioni di poter pianificare, progettare ed accelerare gli investimenti con certezza dei tempi, dei costi e in piena compatibilità con gli indirizzi di sostenibilità e crescita indicati nel Programma nazionale di Ripresa e Resilienza. Particolare attenzione va posta agli investimenti in manutenzione delle opere e nella tutela del territorio, incoraggiando l’utilizzo di tecniche predittive basate sui più recenti sviluppi in tema di Intelligenza artificiale e tecnologie digitali. Il settore privato deve essere invitato a partecipare alla realizzazione degli investimenti pubblici apportando più che finanza, competenza, efficienza e innovazione per accelerare la realizzazione dei progetti nel rispetto dei costi previsti.\n" +
                "\n" +
                "Next Generation EU\n" +
                "\n" +
                "La strategia per i progetti del Next Generation EU non può che essere trasversale e sinergica, basata sul principio dei co-benefici, cioè con la capacità di impattare simultaneamente più settori, in maniera coordinata.  \n" +
                "\n" +
                "Dovremo imparare a prevenire piuttosto che a riparare, non solo dispiegando tutte le tecnologie a nostra disposizione ma anche investendo sulla consapevolezza  delle nuove generazioni che “ogni azione ha una conseguenza”.\n" +
                "\n" +
                "Come si è ripetuto più volte, avremo a disposizione circa 210 miliardi lungo un periodo di sei anni.\n" +
                "\n" +
                "Queste risorse dovranno essere spese puntando a migliorare il potenziale di crescita della nostra economia. La quota di prestiti aggiuntivi che richiederemo tramite la principale componente del programma, lo Strumento per la ripresa e resilienza, dovrà essere modulata in base agli obiettivi di finanza pubblica.\n" +
                "\n" +
                "Il precedente Governo ha già svolto una grande mole di lavoro sul Programma di ripresa e resilienza (PNRR). Dobbiamo approfondire e completare quel lavoro che, includendo le necessarie interlocuzioni con la Commissione Europea, avrebbe una scadenza molto ravvicinata, la fine di aprile. \n" +
                "\n" +
                "Gli orientamenti che il Parlamento esprimerà nei prossimi giorni a commento della bozza di Programma presentata dal Governo uscente saranno di importanza fondamentale nella preparazione della sua versione finale. Voglio qui riassumere l’orientamento del nuovo Governo.\n" +
                "\n" +
                "Le Missioni del Programma potranno essere rimodulate e riaccorpate, ma resteranno quelle enunciate nei precedenti documenti del Governo uscente, ovvero l’innovazione, la digitalizzazione, la competitività e la cultura; la transizione ecologica; le infrastrutture per la mobilità sostenibile; la formazione e la ricerca; l’equità sociale, di genere, generazionale e territoriale; la salute e la relativa filiera produttiva.\n" +
                "\n" +
                "Dovremo rafforzare il Programma prima di tutto per quanto riguarda gli obiettivi strategici e le riforme che li accompagnano.\n" +
                "\n" +
                "Obiettivi strategici\n" +
                "\n" +
                "Il Programma è finora stato costruito in base ad obiettivi di alto livello e aggregando proposte progettuali in missioni, componenti e linee progettuali. Nelle prossime settimane rafforzeremo la dimensione strategica del Programma, in particolare con riguardo agli obiettivi riguardanti la produzione di energia da fonti rinnovabili, l’inquinamento dell’aria e delle acque, la rete ferroviaria veloce, le reti di distribuzione dell’energia per i veicoli a propulsione elettrica, la produzione e distribuzione di idrogeno, la digitalizzazione, la banda larga e le reti di comunicazione 5G.\n" +
                "\n" +
                "Il ruolo dello Stato e il perimetro dei suoi interventi dovranno essere valutati con attenzione. Compito dello Stato è utilizzare le leve della spesa per ricerca e sviluppo, dell’istruzione e della formazione, della regolamentazione, dell’incentivazione e della tassazione.\n" +
                "\n" +
                "In base a tale visione strategica, il Programma nazionale di Ripresa e Resilienza indicherà obiettivi per il prossimo decennio e più a lungo termine, con una tappa intermedia per l’anno finale del Next Generation EU, il 2026. Non basterà elencare progetti che si vogliono completare nei prossimi anni. Dovremo dire dove vogliamo arrivare nel 2026 e a cosa puntiamo per il 2030 e il 2050, anno in cui l’Unione Europea intende arrivare a zero emissioni nette di CO2 e gas clima-alteranti.\n" +
                "\n" +
                "Selezioneremo progetti e iniziative coerenti con gli obiettivi strategici del Programma, prestando grande attenzione alla loro fattibilità nell’arco dei sei anni del programma. Assicureremo inoltre che l’impulso occupazionale del Programma sia sufficientemente elevato in ciascuno dei sei anni, compreso il 2021. \n" +
                "\n" +
                "Chiariremo il ruolo del terzo settore e del contributo dei privati al Programma Nazionale di Ripresa e Resilienza attraverso i meccanismi di finanziamento a leva (fondo dei fondi). \n" +
                "\n" +
                "Sottolineeremo il ruolo della scuola che tanta parte ha negli obiettivi di coesione sociale e territoriale e quella dedicata all'inclusione sociale e alle politiche attive del lavoro.\n" +
                "\n" +
                "Nella sanità dovremo usare questi progetti per porre le basi, come indicato sopra, per rafforzare la medicina territoriale e la telemedicina.\n" +
                "\n" +
                "La governance del Programma di ripresa e resilienza è incardinata nel Ministero dell’Economia e Finanza con la strettissima collaborazione dei Ministeri competenti che definiscono le politiche e i progetti di settore. Il Parlamento verrà costantemente informato sia sull’impianto complessivo, sia sulle politiche di settore.\n" +
                "\n" +
                "Infine il capitolo delle riforme che affronterò ora separatamente.\n" +
                "\n" +
                "Le riforme\n" +
                "\n" +
                "Il Next generation EU prevede riforme. Alcune riguardano problemi aperti da decenni ma che non per questo vanno dimenticati. Fra questi la certezza delle norme e dei piani di investimento pubblico, fattori che limitano gli investimenti, sia italiani che esteri. inoltre la concorrenza: chiederò all’Autorità garante per la concorrenza e il mercato, di produrre in tempi brevi come previsto dalla Legge Annuale sulla Concorrenza (Legge 23 luglio 2009, n. 99) le sue proposte in questo campo.\n" +
                "\n" +
                "Negli anni recenti i nostri tentativi di riformare il Paese non sono stati del tutto assenti, ma i loro effetti concreti sono stati limitati. Il problema sta forse nel modo in cui spesso abbiamo disegnato le riforme: con interventi parziali dettati dall’urgenza del momento, senza una visione a tutto campo che richiede tempo e competenza. Nel caso del fisco, per fare un esempio, non bisogna dimenticare che il sistema tributario è un meccanismo complesso, le cui parti si legano una all’altra. Non è una buona idea cambiare le tasse una alla volta. Un intervento complessivo rende anche più difficile che specifici gruppi di pressione riescano a spingere il governo ad adottare misure scritte per avvantaggiarli. \n" +
                "\n" +
                "Inoltre, le esperienze di altri Paesi insegnano che le riforme della tassazione dovrebbero essere affidate a esperti, che conoscono bene cosa può accadere se si cambia un’imposta. Ad esempio la Danimarca, nel 2008, nominò una Commissione di esperti in materia fiscale. La Commissione incontrò i partiti politici e le parti sociali e solo dopo presentò la sua relazione al Parlamento. Il progetto prevedeva un taglio della pressione fiscale pari a 2 punti di Pil. L’aliquota marginale massima dell’imposta sul reddito veniva ridotta, mentre la soglia di esenzione veniva alzata. \n" +
                "\n" +
                "Un metodo simile fu seguito in Italia all’inizio degli anni Settanta del secolo scorso quando il governo affidò ad una commissione di esperti, fra i quali Bruno Visentini e Cesare Cosciani, il compito di ridisegnare il nostro sistema tributario, che non era stato più modificato dai tempi della riforma Vanoni del 1951. Si deve a quella commissione l’introduzione dell’imposta sul reddito delle persone fisiche e del sostituto d’imposta per i redditi da lavoro dipendente. Una riforma fiscale segna in ogni Paese un passaggio decisivo. Indica priorità, dà certezze, offre opportunità, è l’architrave della politica di bilancio\n" +
                "\n" +
                "In questa prospettiva va studiata una revisione profonda dell’Irpef con il duplice obiettivo di semplificare e razionalizzare la struttura del prelievo, riducendo gradualmente il carico fiscale e preservando la progressività. Funzionale al perseguimento di questi ambiziosi obiettivi sarà anche un rinnovato e rafforzato impegno nell’azione di contrasto all’evasione fiscale.\n" +
                "\n" +
                "L’altra riforma che non si può procrastinare è quella della pubblica amministrazione. Nell’emergenza l’azione amministrativa, a livello centrale e nelle strutture locali e periferiche, ha dimostrato capacità di resilienza e di adattamento grazie a un impegno diffuso nel lavoro a distanza e a un uso intelligente delle tecnologie a sua disposizione. La fragilità del sistema delle pubbliche amministrazioni e dei servizi di interesse collettivo è, tuttavia, una realtà che deve essere rapidamente affrontata. \n" +
                "\n" +
                "Particolarmente urgente è lo smaltimento dell’arretrato accumulato durante la pandemia. Agli uffici verrà chiesto di predisporre un piano di smaltimento dell’arretrato e comunicarlo ai cittadini.\n" +
                "\n" +
                "La riforma dovrà muoversi su due direttive: investimenti in connettività con anche la realizzazione di piattaforme efficienti e di facile utilizzo da parte dei cittadini; aggiornamento continuo delle competenze dei dipendenti pubblici, anche selezionando nelle assunzioni le migliori competenze e attitudini in modo rapido, efficiente e sicuro, senza costringere a lunghissime attese decine di migliaia di candidati.\n" +
                "\n" +
                "Nel campo della giustizia le azioni da svolgere sono principalmente quelle che si collocano all’interno del contesto e delle aspettative dell’Unione europea. Nelle Country Specific Recommendations indirizzate al nostro Paese negli anni 2019 e 2020, la Commissione, pur dando atto dei progressi compiuti negli ultimi anni, ci esorta: ad aumentare l’efficienza del sistema giudiziario civile, attuando e favorendo l’applicazione dei decreti di riforma in materia di insolvenza, garantendo un funzionamento più efficiente dei tribunali, favorendo lo smaltimento dell’arretrato e una migliore gestione dei carichi di lavoro, adottando norme procedurali più semplici, coprendo i posti vacanti del personale amministrativo, riducendo le differenze che sussistono nella gestione dei casi da tribunale a tribunale e infine favorendo la repressione della corruzione.\n" +
                "\n" +
                "Nei nostri rapporti internazionali questo governo sarà convintamente europeista e atlantista, in linea con gli ancoraggi storici dell’Italia: Unione europea, Alleanza Atlantica, Nazioni Unite. Ancoraggi che abbiamo scelto fin dal dopoguerra, in un percorso che ha portato benessere, sicurezza e prestigio internazionale. Profonda è la nostra vocazione a favore di un multilateralismo efficace, fondato sul ruolo insostituibile delle Nazioni Unite. Resta forte la nostra attenzione e proiezione verso le aree di naturale interesse prioritario, come i Balcani, il Mediterraneo allargato, con particolare attenzione alla Libia e al Mediterraneo orientale, e all’Africa.\n" +
                "\n" +
                "Gli anni più recenti hanno visto una spinta crescente alla costruzione in Europa di reti di rapporti bilaterali e plurilaterali privilegiati. Proprio la pandemia ha rivelato la necessità di perseguire uno scambio più intenso con i partner con i quali la nostra economia è più integrata. Per l’Italia ciò comporterà la necessità di meglio strutturare e rafforzare il rapporto strategico e imprescindibile con Francia e Germania. Ma occorrerà anche consolidare la collaborazione con Stati con i quali siamo accomunati da una specifica sensibilità mediterranea e dalla condivisione di problematiche come quella ambientale e migratoria: Spagna, Grecia, Malta e Cipro. Continueremo anche a operare affinché si avvii un dialogo più virtuoso tra l’Unione europea e la Turchia, partner e alleato NATO.\n" +
                "\n" +
                "L’Italia si adopererà per alimentare meccanismi di dialogo con la Federazione Russa. Seguiamo con preoccupazione ciò che sta accadendo in questo e in altri Paesi dove i diritti dei cittadini sono spesso violati. Seguiamo anche con preoccupazione l’aumento delle tensioni in Asia intorno alla Cina.\n" +
                "\n" +
                "Altra sfida sarà il negoziato sul nuovo Patto per le migrazioni e l’asilo, nel quale perseguiremo un deciso rafforzamento dell’equilibrio tra responsabilità dei Paesi di primo ingresso e solidarietà effettiva. Cruciale sarà anche la costruzione di una politica europea dei rimpatri dei non aventi diritto alla protezione internazionale, accanto al pieno rispetto dei diritti dei rifugiati.\n" +
                "\n" +
                "L’avvento della nuova Amministrazione USA prospetta un cambiamento di metodo, più cooperativo nei confronti dell’Europa e degli alleati tradizionali. Sono fiducioso che i nostri rapporti e la nostra collaborazione non potranno che intensificarsi.\n" +
                "\n" +
                "Dal dicembre scorso e fino alla fine del 2021, l’Italia esercita per la prima volta la Presidenza del G20. Il programma, che coinvolgerà l’intera compagine governativa, ruota intorno a tre pilastri: People, Planet, Prosperity. L’Italia avrà la responsabilità di guidare il Gruppo verso l’uscita dalla pandemia, e di rilanciare una crescita verde e sostenibile a beneficio di tutti. Si tratterà di ricostruire e di ricostruire meglio.\n" +
                "\n" +
                "Insieme al Regno Unito - con cui quest’anno abbiamo le Presidenze parallele del G7 e del G20 - punteremo sulla sostenibilità e la “transizione verde” nella prospettiva della prossima Conferenza delle Parti sul cambiamento climatico (Cop 26), con una particolare attenzione a coinvolgere attivamente le giovani generazioni, attraverso l’evento “Youth4Climate”.\n" +
                "\n" +
                "***\n" +
                "\n" +
                "Questo è il terzo governo della legislatura. Non c’è nulla che faccia pensare che possa far bene senza il sostegno convinto di questo Parlamento. È un sostegno che non poggia su alchimie politiche ma sullo spirito di sacrificio con cui donne e uomini hanno affrontato l’ultimo anno, sul loro vibrante desiderio di rinascere, di tornare più forti e sull’entusiasmo dei giovani che vogliono un Paese capace di realizzare i loro sogni. Oggi, l’unità non è un’opzione, l’unità è un dovere. Ma è un dovere guidato da ciò che son certo ci unisce tutti: l’amore per l’Italia.";

        Properties properties = new Properties();
//        properties.setProperty("annotators", "ita_toksent, pos, ita_splitter, ita_morpho, ita_lemma, keyphrase, depparse");
//        properties.setProperty("customAnnotatorClass.ita_splitter", "eu.fbk.dh.tint.splitter.SplitterAnnotator");

        properties.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, keyphrase");
        properties.setProperty("keyphrase.language", "ITALIAN");
//        properties.setProperty("keyphrase.languageFolder", "/Users/alessio/.kd/languages");
//        properties.setProperty("keyphrase.numberOfConcepts", "10");
//        properties.setProperty("keyphrase.skip_keyword_with_proper_noun", "false");
//        properties.setProperty("keyphrase.skip_proper_noun", "false");
//        properties.setProperty("keyphrase.local_frequency_threshold", "2");
//        properties.setProperty("keyphrase.skipFrequencyAbsorption", "true");

//        TintServer server = new TintServer("0.0.0.0", 8012, null, properties);
        TintPipeline pipeline = new TintPipeline();
        pipeline.addProperties(properties);
        pipeline.load();

        Annotation annotation = pipeline.runRaw(text);
        System.out.println(annotation.get(DigiKDAnnotations.KeyphrasesAnnotation.class));

        try {
//            String s = JSONOutputter.jsonPrint(annotation);
//            System.out.println(s);
//            CoNLLUOutputter.conllUPrint(annotation, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
