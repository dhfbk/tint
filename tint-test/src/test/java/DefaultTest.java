import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

public class DefaultTest {

    public static void main(String[] args) {
        String text = "Franz Kafka, nato a Praga il 3 luglio 1883, è uno dei primi , nella \" Lettera al padre \" , a restituirci , con un' immagine alquanto inedita , questo tipo di rapporto .";
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
