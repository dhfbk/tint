import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    public static void main(String[] args) {
        String sentenceText;
        sentenceText = "Il 12 gennaio 2017 sarei potuto andare a fare la spesa.";

        try {

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, udpipe, ita_tense");
            pipeline.setProperty("timex.treeTaggerHome", "/Volumes/LEXAR/Software/TreeTagger");
            pipeline.setProperty("customAnnotatorClass.udpipe", "eu.fbk.fcw.udpipe.api.UDPipeAnnotator");
            pipeline.setProperty("udpipe.server", "gardner");
            pipeline.setProperty("udpipe.port", "50020");
            pipeline.load();

            Annotation annotation = pipeline.runRaw(sentenceText);
//            System.out.println(JSONOutputter.jsonPrint(annotation));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
