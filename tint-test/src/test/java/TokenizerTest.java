import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;

public class TokenizerTest {

    public static void main(String[] args) {
        String text = "Terri Schiavo, la donna che in questi giorni aveva tenuto col fiato sospeso gli Stati Uniti d'America e causato numerosi dibattiti sul diritto a morire, è morta oggi a St. Petersburg (Florida), alle 9:00 locali, 13 giorni dopo il distacco dai macchinari che la tenevano in vita.";
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent");
        pipeline.load();

        Annotation annotation = pipeline.runRaw(text);

        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            System.out.println(token);
        }

    }
}
