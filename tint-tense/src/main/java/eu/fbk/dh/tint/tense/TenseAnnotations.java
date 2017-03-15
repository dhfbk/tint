package eu.fbk.dh.tint.tense;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

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

    @JSONLabel("verbs")
    public static class VerbsAnnotation implements CoreAnnotation<List<TenseMultiToken>> {

        public Class<List<TenseMultiToken>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }

}
