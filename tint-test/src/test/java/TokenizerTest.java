import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;

public class TokenizerTest {

    public static void main(String[] args) {
        String text = "Zingaretti ha fondato una S.p.A. Tu no.";
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent");
        pipeline.setProperty("ita_toksent.ssplitOnlyOnNewLine", "0");
        pipeline.load();

        Annotation annotation = pipeline.runRaw(text);

        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            System.out.println(token);
        }

    }
}
