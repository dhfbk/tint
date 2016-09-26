import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.runner.JSONOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by alessio on 25/09/16.
 */

public class StanfordTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StanfordTest.class);

    public static void main(String[] args) throws IOException {
        String text = "Yesterday Barack Obama was the president of the US.";
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        System.out.println(JSONOutputter.jsonPrint(document));
    }
}
