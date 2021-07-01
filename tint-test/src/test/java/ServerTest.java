import eu.fbk.dh.tint.runner.TintServer;

import java.util.Properties;

public class ServerTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("annotators", "ita_toksent, pos, ita_upos, ita_splitter, ita_morpho, ita_lemma, ner, wemapp");
        properties.setProperty("customAnnotatorClass.wemapp", "edu.stanford.nlp.pipeline.TokensRegexAnnotator");
        properties.setProperty("wemapp.rules", "example.rules");
        properties.setProperty("ita_toksent.model", "/Users/alessio/Desktop/token-settings-teresa.xml");
        TintServer server = new TintServer("0.0.0.0", 8012, null, properties);
    }
}
