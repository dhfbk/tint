import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    public static void main(String[] args) {
        String sentenceText = "Barack Obama è stato eletto presidente degli Stati Uniti otto anni fa.";

        try {

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.loadSerializers();
            pipeline.setProperty("timex.treeTaggerHome", "/Volumes/LEXAR/Software/TreeTagger");
            pipeline.load();

            pipeline.setDocumentDate("2016-10-20");

            Annotation annotation = pipeline.runRaw(sentenceText);
            System.out.println(JSONOutputter.jsonPrint(annotation));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
