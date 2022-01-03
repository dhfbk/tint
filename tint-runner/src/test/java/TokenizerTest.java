import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;

/**
 * Created by alessio on 04/08/16.
 */

public class TokenizerTest {

    public static void main(String[] args) {
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent");
        pipeline.setProperty("ita_toksent.model", "/Users/alessio/Dropbox/relation-extraction/token-settings-wemapp.xml");
        pipeline.setProperty("ita_toksent.newlineIsSentenceBreak", "two");
        String text = "elettronica   della   Ditta   Mac   Stampa   .s.rl.   acquisita   in   data   10/01/2020";
        Annotation annotation = pipeline.runRaw(text);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                System.out.println(token);
            }
            System.out.println();
        }
    }
}
