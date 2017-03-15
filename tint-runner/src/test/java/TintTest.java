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
        sentenceText = "Appostamenti di caccia realizzati secondo le disposizioni provinciali vigenti in materia di protezione della fauna selvatica ed esercizio della caccia, realizzati in difformità rispetto ai criteri e alle tipologie approvati dalla sottocommissione della CUP con riferimento alle relazioni con il contesto, alle forme e ai materiali da impiegare nella realizzazione. Il proprietario dichiara di essere a conoscenza l'autorizzazione paesaggistica è efficace per cinque anni dal rilascio. Se l'autorizzazione è necessaria per l'esecuzione di un intervento soggetto a permesso di costruire o a SCIA, la scadenza dell'autorizzazione paesaggistica coincide con quella del titolo abilitativo edilizio, anche nel caso di proroga del titolo. In ogni caso la domanda di permesso di costruire o la SCIA sono richieste o presentate, rispettivamente, entro un anno dal rilascio dell'autorizzazione paesaggistica.";

        try {

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators",
                    "ita_toksent, pos, ita_morpho, ita_lemma, depparse, verb, readability");
//            pipeline.setProperty("annotators", "ita_toksent, udpipe, verb, readability");
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
