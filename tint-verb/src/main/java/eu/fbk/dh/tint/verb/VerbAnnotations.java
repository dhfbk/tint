package eu.fbk.dh.tint.verb;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class VerbAnnotations {

    @JSONLabel("verbs")
    public static class VerbsAnnotation implements CoreAnnotation<List<VerbMultiToken>> {

        public Class<List<VerbMultiToken>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }

}
