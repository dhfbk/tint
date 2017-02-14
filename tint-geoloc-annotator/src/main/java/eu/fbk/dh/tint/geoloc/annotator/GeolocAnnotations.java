package eu.fbk.dh.tint.geoloc.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by giovannimoretti on 25/09/16.
 */
public class GeolocAnnotations {

    //    @JSONLabel(value = "geocodedlocation", serializer = { GeocodResult.GeocodResultSerializer.class })
    @JSONLabel(value = "geocodedlocation")
    public static class GeolocAnnotation implements CoreAnnotation<GeocodResult> {

        @Override
        public Class<GeocodResult> getType() {
            return GeocodResult.class;
        }

    }

    public static class GeolocMultiAnnotation implements CoreAnnotation<List<GeocodResult>> {

        @Override
        public Class<List<GeocodResult>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }

    }
}
