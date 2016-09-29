package eu.fbk.dh.tint.geoloc.annotator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovannimoretti on 25/09/16.
 */
public class GeolocConfiguration {

    private boolean use_local_geocoder_instance;
    private List<String> allowed_loc_type = new ArrayList<>();
    private String nominatin_url = "https://nominatim.openstreetmap.org/search.php?format=json&q=";
    private String local_geocoder_url = "";
    private Integer timeout = 1000;

    public GeolocConfiguration(List<String> allowed_entities, String geocoder_url, Boolean use_local_geocoder,
            Integer timeout) {
        this.setAllowed_loc_type(allowed_entities);
        this.setLocal_geocoder_url(geocoder_url);
        this.setUse_local_geocoder_instance(use_local_geocoder);
        this.setTimeout(timeout);
    }

    public boolean isUse_local_geocoder_instance() {
        return use_local_geocoder_instance;
    }

    public void setUse_local_geocoder_instance(boolean use_local_geocoder_instance) {
        this.use_local_geocoder_instance = use_local_geocoder_instance;
    }

    public List<String> getAllowed_loc_type() {
        return allowed_loc_type;
    }

    public void setAllowed_loc_type(List<String> allowed_loc_type) {
        this.allowed_loc_type = allowed_loc_type;
    }

    public String getNominatin_url() {
        return nominatin_url;
    }

    public void setNominatin_url(String nominatin_url) {
        this.nominatin_url = nominatin_url;
    }

    public String getLocal_geocoder_url() {
        return local_geocoder_url;
    }

    public void setLocal_geocoder_url(String local_geocoder_url) {
        this.local_geocoder_url = local_geocoder_url;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
