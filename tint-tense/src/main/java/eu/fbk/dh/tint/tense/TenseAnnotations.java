package eu.fbk.dh.tint.tense;

import edu.stanford.nlp.ling.CoreAnnotation;
import eu.fbk.utils.gson.JSONLabel;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class TenseAnnotations {

    @JSONLabel("tense")
    public static class TenseAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

}
