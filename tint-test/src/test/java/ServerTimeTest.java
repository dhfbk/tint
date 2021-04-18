import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintServer;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.apache.log4j.BasicConfigurator;

import java.util.Properties;

public class ServerTimeTest {

    public static void main(String[] args) {
        BasicConfigurator.configure();

        Properties properties = new Properties();
        properties.setProperty("annotators", "ita_toksent, pos, ita_splitter, ita_morpho, ita_lemma, ner");
//        properties.setProperty("annotators", "ita_toksent, pos, ita_splitter, ita_morpho, ita_lemma, parse");
//        properties.setProperty("parse.model", "/Users/alessio/Desktop/ita-parser/constituency.ser.gz");

//        properties.setProperty("ita_toksent.tokenizeOnlyOnSpace", "true");
//        properties.setProperty("ita_toksent.ssplitOnlyOnNewLine", "true");

//        properties.setProperty("sutime.rules", "/Users/alessio/Desktop/ita-parser/sutime/defs.sutime.txt,/Users/alessio/Desktop/ita-parser/sutime/sutime.italian.rules");

        TintServer server = new TintServer("0.0.0.0", 8013, null, properties);

        TintPipeline pipeline = new TintPipeline();
        pipeline.addProperties(properties);
        Annotation annotation = pipeline.runRaw("Ci siamo visti domenica scorsa.");
        try {
            String out = JSONOutputter.jsonPrint(annotation);
            System.out.println(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
