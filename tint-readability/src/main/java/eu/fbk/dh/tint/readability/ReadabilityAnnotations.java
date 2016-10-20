package eu.fbk.dh.tint.readability;

import edu.stanford.nlp.ling.CoreAnnotation;
import eu.fbk.dh.tint.json.JSONLabel;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class ReadabilityAnnotations {

    @JSONLabel("readability")
    public static class ReadabilityAnnotation implements CoreAnnotation<Readability> {

        public Class<Readability> getType() {
            return Readability.class;
        }
    }

    @JSONLabel("hyphenation")
    public static class HyphenationAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

}