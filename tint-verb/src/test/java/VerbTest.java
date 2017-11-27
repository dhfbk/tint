import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.verb.VerbAnnotations;
import eu.fbk.fcw.udpipe.api.UDPipeAnnotations;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by alessio on 21/04/17.
 */

public class VerbTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbTest.class);

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.setProperty("annotators", "ita_toksent, udpipe, ita_verb");
            properties.setProperty("customAnnotatorClass.udpipe", "eu.fbk.fcw.udpipe.api.UDPipeAnnotator");
            properties.setProperty("customAnnotatorClass.ita_toksent",
                    "eu.fbk.dh.tint.tokenizer.annotators.ItalianTokenizerAnnotator");
            properties.setProperty("customAnnotatorClass.ita_verb",
                    "eu.fbk.dh.tint.verb.VerbAnnotator");

            properties.setProperty("udpipe.server", "gardner");
            properties.setProperty("udpipe.port", "50020");
            properties.setProperty("udpipe.keepOriginal", "1");

//        properties.setProperty("udpipe.model", "/Users/alessio/Desktop/model");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

            Annotation annotation = new Annotation("Il caporale alpino Giampietro Civati caduto in combattimento il 5 dicembre 1944, come racconta Silvestri, ha scritto questo mirabile testamento: «sono figlio d’Italia, d’anni 21, non di Graziani e nemmeno di Badoglio, ma sono italiano e seguo la via che salverà l’onore d’Italia».");
            pipeline.annotate(annotation);
            String out = JSONOutputter.jsonPrint(annotation);
            System.out.println(out);
//            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//                System.out.println(sentence.get(VerbAnnotations.VerbsAnnotation.class));
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
