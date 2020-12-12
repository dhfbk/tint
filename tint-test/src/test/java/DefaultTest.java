import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

public class DefaultTest {

    public static void main(String[] args) {
        String text = "Dischetti levatrucco make up MAREB in cotone idrofilo 100PZ  Dischetti per togliete il trucco Materiale 100% cotone idrofilo Quantità 100 dischetti Codice:210140.";
        TintPipeline pipeline = new TintPipeline();
        pipeline.load();

        Annotation annotation = pipeline.runRaw(text);

        try {
            String s = JSONOutputter.jsonPrint(annotation);
            System.out.println(s);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
