import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;

public class TokenizerTest {

    public static void main(String[] args) {
        String text = "Secondo i dati ufficiali pubblicati alle 9:00, Mahmoud Ahmadinejad avrebbe ricevuto il 63% delle preferenze, con Musavi fermo al 34%.";
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent");
        pipeline.load();

        Annotation annotation = pipeline.runRaw(text);

        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            System.out.println(token);
        }

    }
}
