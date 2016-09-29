package eu.fbk.dh.tint.geoloc.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;

/**
 * Created by giovannimoretti on 25/09/16.
 */
public class GeolocAnnotation {
    public static final String GEOLOC_ANNOTATION = "geoloc_annotation";
    public static final Annotator.Requirement GEOLOC_ANNOTATION_REQUIREMENT = new Annotator.Requirement(GEOLOC_ANNOTATION);

    public static class GEOLOC_ANNOTATION implements CoreAnnotation<String> {
        @Override
        public Class<String> getType() {
            return String.class;
        }

    }
}
