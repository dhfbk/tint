package eu.fbk.dh.tint.runner;

import edu.stanford.nlp.ling.CoreAnnotation;
import eu.fbk.utils.gson.JSONLabel;

/**
 * Created by giovannimoretti on 25/09/16.
 */
public class TimingAnnotations {

    @JSONLabel(value = "timings")
    public static class TimingAnnotation implements CoreAnnotation<String> {

        @Override
        public Class<String> getType() {
            return String.class;
        }

    }
}
