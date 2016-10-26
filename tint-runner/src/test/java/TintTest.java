import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    public static void main(String[] args) {
        String sentenceText = "Barack Obama è stato eletto\n\n\npresidente degli Stati . Uniti otto anni fa.";

        try {

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
//            pipeline.loadSerializers();
            pipeline.setProperty("annotators", "ita_toksent");
            pipeline.setProperty("ita_toksent.tokenizeOnlyOnSpace", "true");
//            pipeline.setProperty("ita_toksent.ssplitOnlyOnNewLine", "true");
            pipeline.load();

            Annotation annotation = pipeline.runRaw(sentenceText);
            System.out.println(JSONOutputter.jsonPrint(annotation));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
