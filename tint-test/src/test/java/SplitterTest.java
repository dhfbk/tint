import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLUOutputter;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

public class SplitterTest {
    public static void main(String[] args) {
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent, pos, ita_upos, ita_splitter, ita_morpho, ita_lemma, depparse");
        pipeline.setProperty("customAnnotatorClass.ita_upos", "eu.fbk.fcw.pos.UPosAnnotator");
        pipeline.setProperty("customAnnotatorClass.ita_splitter", "eu.fbk.dh.tint.splitter.SplitterAnnotator");

        pipeline.load();

        Annotation annotation = pipeline.runRaw("Il ratto delle sabine è stato terribile.");

        try {
            String s = JSONOutputter.jsonPrint(annotation);
            System.out.println(s);
            CoNLLUOutputter.conllUPrint(annotation, System.out);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
