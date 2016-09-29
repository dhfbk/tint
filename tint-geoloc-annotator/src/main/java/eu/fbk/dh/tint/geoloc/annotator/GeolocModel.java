package eu.fbk.dh.tint.geoloc.annotator;

import java.util.List;

/**
 * Created by giovannimoretti on 25/09/16.
 */
public class GeolocModel {

    private static GeolocConfiguration geoloc_conf;

    public static GeolocConfiguration getInstance(List<String> allowed_entities, String geocoder_url,
            Boolean use_local_geocoder, Integer timeout) {
        if (geoloc_conf == null) {
            geoloc_conf = new GeolocConfiguration(allowed_entities, geocoder_url, use_local_geocoder, timeout);
        }

        return geoloc_conf;
    }
}
