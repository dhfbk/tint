import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.geoloc.annotator.GeolocAnnotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TintTest.class);

    public static void main(String[] args) {
        String sentenceText =
                "Pensieri molto allegri. e sulla debolezza dei vinti. La pace che si firma è una pace che si puntella sull'arbitrio del vincitore  l'esportazione di merci verso la Germania; la causa va ricercata anche nel mancato  coinvolgimento, da parte del governo italiano, di esponenti politici e sociali delle terre  redente nella formulazione dei trattati. Quando leggiamo nella stampa francese che nell'Alsazia-Lorena ritornano  in questi giorni i compratori di Magonza e Francoforte per l'acquisto dei soliti  vini da taglio e che, in base al trattato, si riorganizza l'esportazione dei vini  e di alcune manifatture verso la Germania, noi ci chiediamo con un senso  di invidia e di stizza perché noi, italiani delle terre redente, non ci troviamo  nella stessa situazione in confronto dei paesi della defunta Monarchia. E qui ci preoccupa naturalmente in prima linea la crisi stessa dei vini. \n"
                        + "Benché le cause della crisi siano molteplici e risiedano sovrattutto nelle  condizioni del cambio, è tuttavia innegabile che la mancanza di una disposizione precisa nel trattato difficoltò la ripresa dei mercati. Perché nel patto  di S. Germain non c'è stato assicurato un trattamento di favore? Il Governo  non ignorava il nostro postulato; ricorderemo in proposito che un deputato  trentino ebbe occasione di parlarne all'on. Sonnino già nella primavera del  1915, durante il periodo di neutralità e che lo stesso postulato riappare nei  memoriali e nelle pubblicazioni degli emigrati trentini durante la guerra; che  infine già da Berna il 2 novembre, prima dunque ancora dell'occupazione  di Trento, i deputati appena usciti dall'Austria ne telegrafavano al Ministero  degli esteri e pochi giorno dopo giunti a Roma ne parlavano all'on. Orlando  e all'on. Sonnino, lasciando a quest'ultimo anche un memoriale; più tardi  v'insistettero la consulta trentina, il consiglio d'agricoltura e ne trattò l'on. \n"
                        + "Tambosi a Parigi. Non v'ha dubbio tuttavia che se il paese avesse potuto imaginare che un  postulato il quale sembrava così facile non avrebbe potuto ottenere soddisfacimento, si sarebbe riscosso dal suo torpore ed avrebbe ben fatta sentire  diversamente la sua voce. Ma chi era pienamente informato, chi sapeva esattamente a qual punto  stavano le trattative? E qui noi mettiamo il dito nella piaga. La mancata  clausola sui vini non è che un esempio, e noi temiamo che nel trattato  troveremo ben altre omissioni. Ciò deve ascriversi sovrattutto al fatto che  nessun rappresentante del paese né politico, né tecnico, né commerciale, né  agricoltore venne chiamato dal Governo a Parigi, a tempo debito e munito  delle necessarie attribuzioni. Anzi, e questo è peggio, nel nostro paese né in  Italia, con partecipazione della regione nostra, non sorse alcuna commissione  a preparare e studiare le clausole finanziarie del trattato. In ogni più piccolo staterello sorto sulle rovine dell'Austria, si costituirono  larghe e molteplici commissioni di deputati, industriali, commercianti, tecnici,  finanzieri per preparare i trattati. I deputati redenti hanno potuto vedere  quasi tutti i loro colleghi della Camera austriaca in quanto già non sedevano  sui banchi dei rispettivi governi, partecipare o in commissioni preliminari  in casa propria o in appositi comitati presso le loro delegazioni di Parigi ai  lavori per la pace e hanno dovuto assistere a questo fenomeno con un senso  d'umiliazione, perché viceversa il loro governo nazionale si ricordava della  deputazione delle terre redente per consultarla solo incidentalmente. Non facciamo questione di persone; avremmo potuto ammettere che  si fossero scelti almeno altri rappresentanti come periti in argomento. Ciò  avvenne in parte per la Venezia Giulia, per noi, niente. Per iniziativa dei deputati il Comando supremo e con lui l'on. Orlando  acconsentirono alla costituzione del cosiddetto comitato di tutela in Vienna,  che nella mente dei promotori doveva diventare il rappresentante degl'italiani  nella liquidazione a[ustro] u[ngarica] e, durante l'armistizio, servire d'organo  di studio per la delegazione di Parigi. Ebbene, a fatica, appena dopo alcuni mesi preziosi si riuscì a convincere  la delegazione che, invece di rivolgersi all'uno o all'altro per informazioni,  procedendo a tastoni, poteva dirigersi al comitato, che aveva a sua disposizione  la maggior parte d'impiegati italiani ch'erano prima nei ministeri austriaci. Ma la cosa più dolorosa è che, nonostante molte e ripetute insistenze tra  delegazione e comitato non si è mai potuti arrivare ad una reciproca collaborazione sul terreno della difesa dei nostri interessi. Il caso dell'articolo 266  del trattato a cui s'è riferito recentemente il nostro collaboratore viennese, è  sintomatico e, a ragion veduta, meriterà un maggior rilievo. E auguriamoci che nel testo del trattato che, quando Dio vorrà, potremo  leggere, non si trovino altre simili sorprese. Frattanto però un senso di amarezza rimane nel nostro animo per la superficialità, l'assenza di ogni metodo e la mancanza di ogni criterio democratico  che dominarono in questioni di così vitale interesse per la nostra regione. Il Trentino per questo trattamento soffre non solo nei suoi interessi, ma  anche nella sua dignità.";

        try {

//            LOGGER.info(sentenceText);

            Properties prop = new Properties();
            prop.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma,ner,geoloc");
            prop.setProperty("geoloc.allowed_entity_type", "LOC"); //comma separated list of entity types identifying locations
            prop.setProperty("geoloc.use_local_geocoder", "true"); //boolean value to use local geocoder
            prop.setProperty("geoloc.geocoder_url", "http://rhodes.fbk.eu/nominatim/search.php"); //url of local instance of a nominatim geocoder
            prop.setProperty("geoloc.timeout", "0"); //timeout to set query rate

            prop.setProperty("customAnnotatorClass.geoloc", "eu.fbk.dh.tint.geoloc.annotator.GeolocAnnotator");


            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.addProperties(prop);
            pipeline.load();

            Annotation annotation = pipeline.runRaw(sentenceText);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    System.out.println(token);
                    System.out.println(token.ner());
                    System.out.println(token.get(GeolocAnnotation.GEOLOC_ANNOTATION.class));

                }

            }


//            OutputStream outputStream = System.out;
//            InputStream inputStream = new ByteArrayInputStream(sentenceText.getBytes(StandardCharsets.UTF_8));
//            pipeline.run(inputStream, outputStream, TintRunner.OutputFormat.JSON);

//            for (Class<?> myClass : annotation.keySetNotNull()) {
//                Object o = annotation.get((Class) myClass);
//
//                System.out.println(o.getClass().getMethod("json"));
//            }

//            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                    System.out.println(token);
//                    System.out.println(token.ner());
//                    System.out.println();
//                }
//
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
