import eu.fbk.dh.tint.runner.TintServer;

import java.util.Properties;

public class ServerTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
//        properties.setProperty("annotators", "ita_toksent, pos, ita_upos, ita_splitter, ita_morpho, ita_lemma, ner");
        properties.setProperty("annotators", "ita_toksent");
        properties.setProperty("ita_toksent.model", "/Users/alessio/Desktop/DatiMoro/dati2-out/token-settings.xml");
        TintServer server = new TintServer("0.0.0.0", 8012, null, properties);
    }
}
