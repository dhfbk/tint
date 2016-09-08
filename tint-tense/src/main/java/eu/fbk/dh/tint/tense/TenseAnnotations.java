package eu.fbk.dh.tint.tense;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class TenseAnnotations {

    public static final String DH_TENSE = "tense";
    public static final Annotator.Requirement DH_TENSE_REQUIREMENT = new Annotator.Requirement(DH_TENSE);

    public static class TenseAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

}
