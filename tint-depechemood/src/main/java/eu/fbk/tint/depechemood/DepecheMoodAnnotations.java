package eu.fbk.tint.depechemood;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.Map;

public class DepecheMoodAnnotations {
    @JSONLabel("mood")
    public static class DepecheMoodAnnotation implements CoreAnnotation<Map<String, Double>> {

        @Override
        public Class<Map<String, Double>> getType() {
            return ErasureUtils.<Class<Map<String, Double>>>uncheckedCast(Map.class);
        }
    }
}
