import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;

import java.io.IOException;

/**
 * Created by alessio on 04/08/16.
 */

public class TokenizerTest {

    public static void main(String[] args) {
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent");
        pipeline.setProperty("ita_toksent.model", "/Users/alessio/Dropbox/relation-extraction/token-settings-wemapp.xml");
        pipeline.setProperty("ita_toksent.newlineIsSentenceBreak", "two");
        String text = "Prova con due acapo\n\n\n \n\n Ciao\n\nBella lì\nEccoci";
        Annotation annotation = pipeline.runRaw(text);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println(sentence.get(CoreAnnotations.TokensAnnotation.class).size());
        }
    }
}
