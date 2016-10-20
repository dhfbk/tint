package eu.fbk.dh.tint.tense;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;
import eu.fbk.dh.tint.json.JSONLabel;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class TenseAnnotations {

    public static final String DH_TENSE = "tense";
    public static final Annotator.Requirement DH_TENSE_REQUIREMENT = new Annotator.Requirement(DH_TENSE);

    @JSONLabel("tense")
    public static class TenseAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

}
