import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.JSONOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.runner.TintServer;

import java.util.Properties;

public class StanfordTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, depparse, readability");

        properties.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words-distsim.tagger");
        properties.setProperty("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
        properties.setProperty("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_UD.gz");
        properties.setProperty("readability.language", "en");
//        properties.setProperty("sutime.rules", "/Users/alessio/Desktop/ita-parser/defs.sutime.txt");
//        properties.setProperty("ner.docdate.usePresent", "true");
//        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

        TintServer server = new TintServer("0.0.0.0", 8012, null, properties);

//        String text = "See you in 2021.";
//        Annotation annotation = new Annotation(text);
//        pipeline.annotate(annotation);
//
//        try {
//            String s = JSONOutputter.jsonPrint(annotation);
//            System.out.println(s);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
