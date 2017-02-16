package eu.fbk.dh.tint.languagetool;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by giovannimoretti on 25/09/16.
 */
public class LanguageToolAnnotations {

    @JSONLabel(value = "languagetool")
    public static class LanguageToolMultiAnnotation implements CoreAnnotation<List<Match>> {

        @Override
        public Class<List<Match>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }

    }
}
