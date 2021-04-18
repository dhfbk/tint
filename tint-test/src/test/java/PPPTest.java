import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLUOutputter;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

public class PPPTest {
    public static void main(String[] args) {
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent, ppp");
        pipeline.setProperty("customAnnotatorClass.ppp", "eu.fbk.fcw.ppp.PPPAnnotator");
        pipeline.setProperty("ppp.language", "it");

        pipeline.load();

        Annotation annotation = pipeline.runRaw("buona fortuna !");

        try {
            String s = JSONOutputter.jsonPrint(annotation);
            System.out.println(s);
            CoNLLUOutputter.conllUPrint(annotation, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
