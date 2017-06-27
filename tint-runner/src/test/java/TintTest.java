import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    public static void main(String[] args) {
        String sentenceText;
        sentenceText = "Il 12 gennaio 2017 non sono mica andato a fare la spesa. O forse non è vero.";
        sentenceText = "Vivo per mangiare.";
        sentenceText =
                "L’agevolazione, prevista dalla ultima Legge di Bilancio, riguarderà solo chi è residente all’estero da almeno 9 periodi d’imposta negli ultimi 10 anni ed è facilmente attivabile: punta quindi agli stranieri e tiene fuori coloro che in questi anni si sono trasferiti dall’Italia all’estero."
                        + "La convenienza, ovviamente, c’è soprattutto per coloro che hanno grossi patrimoni e redditi."
                        + " Ma anche per chi ha famiglie numerose con guadagni a molti zeri: insieme al contribuente-Paperone infatti potranno beneficiare del fisco-forfait anche i familiari, pagando un ulteriore `gettone´ al fisco da 25.000 euro.";
        sentenceText = "Il suo nome di battesimo era Lodovico.";
//        sentenceText = "Il fisco italiano strizza l’occhio agli stranieri ricchi. Da oggi chi ha da molti anni una residenza estera e si sposta in Italia può scegliere di attivare la «flat tax sui Paperoni», una tassa fissa da 100.000 euro l’anno. La norma è operativa da oggi con le istruzioni e una apposita check list messe a punto dall’Agenzia delle Entrate. La novità, secondo alcune stime, potrebbe interessare subito un migliaio di soggetti e punta a fare concorrenza a Paesi come Spagna e Gb (ora interessata dalla Brexit) che così hanno attratto emiri, calciatori, cantanti.";
//        sentenceText = "Appostamenti di caccia realizzati secondo le disposizioni provinciali vigenti in materia di protezione della fauna selvatica ed esercizio della caccia, realizzati in difformità rispetto ai criteri e alle tipologie approvati dalla sottocommissione della CUP con riferimento alle relazioni con il contesto, alle forme e ai materiali da impiegare nella realizzazione. Il proprietario dichiara di essere a conoscenza l'autorizzazione paesaggistica è efficace per cinque anni dal rilascio. Se l'autorizzazione è necessaria per l'esecuzione di un intervento soggetto a permesso di costruire o a SCIA, la scadenza dell'autorizzazione paesaggistica coincide con quella del titolo abilitativo edilizio, anche nel caso di proroga del titolo. In ogni caso la domanda di permesso di costruire o la SCIA sono richieste o presentate, rispettivamente, entro un anno dal rilascio dell'autorizzazione paesaggistica.";
        sentenceText = "PREMESSA\n"
                + "Nella gestione dei ristoranti, una delie principali sfide per il futuro è la trasformazione digitale.\n"
                + "Un fenomeno in continua evoluzione che ha già iniziato a muovere importanti passi. Basti pensare all'agenda cartacea delle prenotazioni spesso sostituita da un software, alla pubblicità resa più incisiva e d'effetto perché online, alla relazione con il cliente non più solo vis-à-vis, ma sempre più spesso gestita in rete dall'inizio alla fine. Si intuisce come e quanto la tecnologia sia un vero punto di forza su cui continuare a investire per raggiungere risultati oltremodo lusinghieri nella gestione dell'attività ristorativa, avendo come principale obiettivo la soddisfazione e la conseguente fidelizzazione de! cliente.\n"
                + "Trovando in rete informazioni sull'offerta gastronomica, l'utente viene stimolato a provare nuove pietanze e ingredienti e, addirittura, a richiedere preparazioni personalizzate. Inoltre grazie ai tool digitali, i ristoratori possono conoscere le preferenze dei clienti e usarle strategicamente a favore delta fidelizzazione.\n"
                + "L'IFPA in coerenza con il Progetto di Istituto intende dare ai propri studenti la possibilità di confrontarsi realmente, in un ambiente protetto quale la scuola, con le problematicità in continua evoluzione dei settore dell'hoteilerie, creando un'attività di accoglienza e ospitalità aperta al pubblico denominata \"Hospitality 3.0@ifpa\".\n"
                + "CARATTERISTICHE DEL PROGETTO: Hospitality_3.0@ifpa\n"
                + "L'intervento si attua mediante l'integrazione dei dispositivi e delle attività nel laboratorio di ospitalità a cui si aggiunge la creazione di un'aula polifunzionale adatta a favorire la ricerca, produzione, rielaborazione e adattamento di risorse educative digitali. Tale iniziativa prevede la gestione ; delle attività delle classi con i nuovi media e consente di attuare una didattica con strategie di active learning (Flipped classroom, Cooperative Learning, ecc.). Inoltre si prevede di implementare sul sito della scuola i menù, le ricette con la loro evoluzione storica, l'apporto nutrizionale dei singoli piatti e i costi, con la possibilità di gestire le prenotazioni online.\n"
                + "Si intende quindi realizzare un'attività di ristorazione aperta al pubblico (e la conseguente gestione operativa, economica e amministrativa) all'interno di un'Istituzione pubblica è legata all'imprescindibilità dell'azione educativa: tale attività favorisce notevolmente l'obiettivo di formare giovani in grado di inserirsi agevolmente nel mondo del lavoro, avendo ampiamente sperimentato le procedure operative, le criticità relazionali e le doti umane richieste. ACQUISTI previsti\n"
                + "Si è previsto l'acquisto di apparecchiature con campi di applicabilità flessibile per favorire un uso adeguato e sistematico della tecnologia nell'arco dell'anno (impiego degli stessi strumenti in più campi) avendo in tal modo la possibilità di allestire in entrambe le sedi: Aula polifunzionale con tavoli ad isole, attrezzati anche con la possibilità di utilizzare pc, tablet/notebook e altri devices anche personali degli studenti (o dei docenti in caso di attività formative ad essi rivolti). Arredi ergonomici che consentano un rapido cambiamento del setting dell'aula. Aula per piccoli gruppi, per attività specifiche dedicate agli alunni con BES.\n"
                + "OBIETTIVI specifici e risultati attesi\n"
                + "Con questa iniziativa si intendono perseguire obiettivi specifici, mediante l'utilizzo consapevole delle nuove tecnologie abbinate a nuove metodologie didattiche (Flipped Classroom, Cooperative Learning), sintetizzabili in:\n"
                + "•Fornire strumenti operativi concreti e conoscenze tecniche il cui utilizza e la cui necessità siano direttamente sperimentabili.\n"
                + "•Dotare gli allievi di mezzi linguistici, culturali e scientifici in grado di gestire ogni tipo di situazione che si verifichi nella gestione di un'impresa\n"
                + "della ristorazione, siano esse legate al ricevimento della clientela, alla produzione del cibo, al momento della vendita di sala e bar.\n"
                + "•Incrementare coscienza critica, senso civico e libertà di pensiero attraverso la concretezza del lavoro, le difficoltà ad esso legate e le gratificazioni\n"
                + "che ne conseguono.\n"
                + "•Favorire l'integrazione dell'utenza in difficoltà quali allievi con BES o stranieri, grazie alla condivisione di spazi, esperienze e obiettivi universalmente percepibili,\n"
                + "•Porsi al servizio del territorio fornendo una forma di ristorazione molto prossima alla realtà, ma del rutto particolare nelle modalità di %^\n"
                + "svolgimento./■!\n"
                + "•Creare una sorta di osservatorio sul mondo della ristorazione i cui input siano strumento per meglio rispondere alle esigenze del mercato e gli.U^\n"
                + "output siano per tutti {imprese per prime) strumento condivisibile di miglioramento. .■...METODOLOGIA didattica, innovazione curriculare, uso di contenuti digitali\n"
                + "11 progetto Hospitality_3.0@ifpa consente allo studente di sperimentare in contesto reale, il coordinamento di tutte le attività operative,<•■.,\n"
                + "amministrative ed economiche della struttura, necessarie a garantire il rispetto della nonnativa sanitaria e di sicurezza, contabilità, gestione delle' |\n"
                + "licenze, adempimenti fiscali.;\n"
                + "Modulo certificato ai sentì dell'art. 9, comma 4, della l.p. 23/1992 e approvato con delibera n. 1877 del 2 novembre 2016 ijp^\n"
                + "Dopo aver declinato le fasi dell'attività da svolgere, gli allievi vengono organizzali secondo ì! criterio \"chi fa che cosa\" dai docenti responsabili che assumono il ruolo di caposervizio. Sia in cucina che in sala bar si vengono cosi a formare le \"brigate\", struttura organizzativa del personale tipica dell'hotellerie classica. Tale organizzazione si discosta dalla didattica tradizionale ed assume una forma di autenticità che consente di vivere, tutti insieme, la realtà operativa in cui ci si trova.\n"
                + "Ogni attività dovrà essere svolta nel rispetto della programmazione didattica condivisa dai Dipartimenti di materia e dal Consiglio di Classe.\n"
                + "Il progetto Hospitality_3.0@ifpa coinvolge tutte le discipline del piano di studi. In particolare, concorrono attivamente al progetto:\n"
                + "11 Lab. di Accoglienza e Ospitalità: dove si impara a gestire le prenotazioni, accogliere il cliente, trattare le operazioni contabili compreso\n"
                + "l'incasso, eseguire operazioni di front desk. Il Lab. di Accoglienza e Ospitalità insieme al bar, diventa una vera palestra di vendita del prodotto\n"
                + "ristorativo che coinvolge abilità tecniche, conoscenze professionali e culturali, competenze linguistiche e, più in generale, relazionali.\n"
                + "Il Lab. di Gastronomia e Arte Bianca: dove si svolge l'attività di preparazione dei pasti con la possibilità di spaziare dalla promozione della cucina\n"
                + "tìpica, a quella internazionale, alle nuove tendenze fortemente tecnologiche. L'attività di gastronomia prevede altresì la gestione della materia\n"
                + "prima e degli ambienti secondo le normative igienico sanitarie vigenti.\n"
                + "Le Scienze degli Alimenti, forniscono apponi sulla merceologia dei prodotti, lo studio delle tecniche di cottura, l'analisi delle procedure dì corretta prassi igienica (HACCP).\n"
                + "Le lingue straniere (inglese e tedesco), considerate vera chiave per il successo professionale.\n"
                + "Le discipline più propriamente d'aula, poiranno dedicare parte dei loro curricula allo sviluppo di competenze direttamente collegabili all'attività svolta nel ristorante di applicazione.\n";

        sentenceText = "Non fossi stato mangiato da un canguro.";

        try {

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
//            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, depparse, readability");
            pipeline.setProperty("annotators", "ita_toksent, udpipe, ita_morpho, ita_lemma, verb, readability");
            pipeline.setProperty("timex.treeTaggerHome", "/Volumes/LEXAR/Software/TreeTagger");
            pipeline.setProperty("customAnnotatorClass.udpipe", "eu.fbk.fcw.udpipe.api.UDPipeAnnotator");
            pipeline.setProperty("customAnnotatorClass.verb", "eu.fbk.dh.tint.verb.VerbAnnotator");
            pipeline.setProperty("udpipe.server", "gardner");
            pipeline.setProperty("udpipe.port", "50020");
            pipeline.load();

            Annotation annotation = pipeline.runRaw(sentenceText);

            System.out.println(JSONOutputter.jsonPrint(annotation));
//            System.out.println(annotation.get(ReadabilityAnnotations.ReadabilityAnnotation.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
