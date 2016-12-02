import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by alessio on 04/08/16.
 */

public class TestTokenizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTokenizer.class);

    public static void main(String[] args) {
        String text = "Questa frase finisce.'\n'E questa inizia. Questi invece sono vicini: '' Ciao #bersani";

        Properties props;
        Annotation annotation;

        props = new Properties();
        props.setProperty("annotators", "ita_toksent");
        props.setProperty("customAnnotatorClass.ita_toksent", "eu.fbk.dh.tint.tokenizer.annotators.ItalianTokenizerAnnotator");

        StanfordCoreNLP ITApipeline = new StanfordCoreNLP(props);
        annotation = new Annotation(text);
        ITApipeline.annotate(annotation);
        System.out.println(ITApipeline.timingInformation());

        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            System.out.println(token);
        }

    }
}
