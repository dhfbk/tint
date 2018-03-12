import com.google.common.io.CharStreams;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Created by alessio on 04/08/16.
 */

public class TestTokenizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTokenizer.class);

    public static void main(String[] args) {
        try {
            String text = CharStreams.toString(new BufferedReader(new FileReader(new File("/Users/alessio/Desktop/GIA.txt"))));

//            text = "Sei un cavolo di cazzabubbo.lo stronzo!";

            Properties props;
            Annotation annotation;

            props = new Properties();
            props.setProperty("annotators", "ita_toksent");
            props.setProperty("ita_toksent.model", "/Users/alessio/Desktop/token-settings.xml");
            props.setProperty("customAnnotatorClass.ita_toksent", "eu.fbk.dh.tint.tokenizer.annotators.ItalianTokenizerAnnotator");

            StanfordCoreNLP ITApipeline = new StanfordCoreNLP(props);
            annotation = new Annotation(text);
            ITApipeline.annotate(annotation);
            System.out.println(ITApipeline.timingInformation());

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                System.out.println(sentence.get(CoreAnnotations.TextAnnotation.class));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
