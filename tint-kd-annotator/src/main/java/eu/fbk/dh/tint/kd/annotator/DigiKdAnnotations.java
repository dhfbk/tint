package eu.fbk.dh.tint.kd.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;

/**
 * Created by giovannimoretti on 14/09/16.
 */
public class DigiKdAnnotations {
    public static final String DH_KEYPHRASE = "dh_keyphrase";
    public static final Annotator.Requirement DH_KEYPHRASE_REQUIREMENT = new Annotator.Requirement(DH_KEYPHRASE);

    public static class DH_KEYPHRASE implements CoreAnnotation<List<DigiKdResult>> {
        @Override
        public Class<List<DigiKdResult>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }
}
