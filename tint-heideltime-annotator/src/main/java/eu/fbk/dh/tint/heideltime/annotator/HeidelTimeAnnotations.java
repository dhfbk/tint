package eu.fbk.dh.tint.heideltime.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Pair;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by alessio on 30/06/17.
 */

public class HeidelTimeAnnotations {
    @JSONLabel("timexes")
    public static class TimexesAnnotation implements CoreAnnotation<List<HeidelTimeAnnotator.TimexObject>> {

        public Class<List<HeidelTimeAnnotator.TimexObject>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }

}
