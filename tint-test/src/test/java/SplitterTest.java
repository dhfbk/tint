import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoNLLUOutputter;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

public class SplitterTest {
    public static void main(String[] args) {
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent, pos, ita_upos, ita_splitter, ita_morpho, ita_lemma, ner");
        pipeline.setProperty("customAnnotatorClass.ita_upos", "eu.fbk.fcw.pos.UPosAnnotator");
        pipeline.setProperty("customAnnotatorClass.ita_splitter", "eu.fbk.dh.tint.splitter.SplitterAnnotator");

        pipeline.load();

        Annotation annotation = pipeline.runRaw("Franz Kafka, nato a Praga il 3 luglio 1883, è uno dei primi , nella \" Lettera al padre \" , a restituirci , con un' immagine alquanto inedita , questo tipo di rapporto .");

        try {
            String s = JSONOutputter.jsonPrint(annotation);
            System.out.println(s);
            CoNLLUOutputter.conllUPrint(annotation, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
