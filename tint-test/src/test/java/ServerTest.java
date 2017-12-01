import eu.fbk.dh.tint.runner.TintServer;

import java.util.Properties;

/**
 * Created by alessio on 18/10/16.
 */

public class ServerTest {

    public static void main(String[] args) {

        Properties customProperties = new Properties();

        // Geolocation settings (default: use Nominatim official API)
        customProperties.setProperty("geoloc.allowed_entity_type", "LOC");
        customProperties.setProperty("geoloc.use_local_geocoder", "true");
        customProperties.setProperty("geoloc.geocoder_url", "http://rhodes.fbk.eu/nominatim/search.php");
        customProperties.setProperty("geoloc.timeout", "0");

        // TreeTagger home folder for HeidelTime
        customProperties.setProperty("timex.treeTaggerHome", "/Volumes/LEXAR/Software/TreeTagger");

        // KD models folder
        customProperties.setProperty("keyphrase.languageFolder", "/Users/alessio/Downloads/languages");

        // The Wiki Machine
        customProperties.setProperty("ml.annotator", "ml-annotate");
        customProperties.setProperty("ml.address", "http://ml.apnetwork.it/annotate");
        customProperties.setProperty("ml.min_confidence", "0.5");

        // List of annotators
//        customProperties.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ner, depparse, fake_dep, ml, readability, ita_tense, geoloc, timex, keyphrase");
        customProperties.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ner, depparse, fake_dep, keyphrase");

        TintServer server = new TintServer("0.0.0.0", 8012, null, customProperties);
    }

}
