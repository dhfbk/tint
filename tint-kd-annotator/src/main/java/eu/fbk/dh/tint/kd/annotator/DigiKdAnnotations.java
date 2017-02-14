package eu.fbk.dh.tint.kd.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by giovannimoretti on 14/09/16.
 */
public class DigiKdAnnotations {

    @JSONLabel("keywords")
    public static class KeyphrasesAnnotation implements CoreAnnotation<List<DigiKdResult>> {

        @Override
        public Class<List<DigiKdResult>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }
}
