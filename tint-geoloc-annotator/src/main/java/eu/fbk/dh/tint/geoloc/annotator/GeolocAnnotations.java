package eu.fbk.dh.tint.geoloc.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;

/**
 * Created by giovannimoretti on 25/09/16.
 */
public class GeolocAnnotations {

    public static final String GEOLOC_ANNOTATION = "geoloc";
    public static final Annotator.Requirement GEOLOC_ANNOTATION_REQUIREMENT = new Annotator.Requirement(
            GEOLOC_ANNOTATION);

    public static class GeolocAnnotation implements CoreAnnotation<GeocodResult> {

        @Override
        public Class<GeocodResult> getType() {
            return GeocodResult.class;
        }

    }

    public static class GeolocMultiAnnotation implements CoreAnnotation<GeolocList<GeocodResult>> {

        @Override
        public Class<GeolocList<GeocodResult>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }

    }
}
