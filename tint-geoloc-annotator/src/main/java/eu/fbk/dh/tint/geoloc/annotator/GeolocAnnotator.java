package eu.fbk.dh.tint.geoloc.annotator;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by giovannimoretti on 25/09/16.
 */
public class GeolocAnnotator implements Annotator {

    GeolocConfiguration geoloc_conf;
    private static final Logger LOGGER = LoggerFactory.getLogger(GeolocAnnotator.class);

    public GeolocAnnotator(String annotatorName, Properties prop) {
        List<String> allowed_entities = Arrays
                .asList(prop.getProperty(annotatorName + ".allowed_entity_type").split(","));
        String geocoder_url = prop.getProperty(annotatorName + ".geocoder_url");
        Boolean use_local_geocoder = Boolean.parseBoolean(prop.getProperty(annotatorName + ".use_local_geocoder"));
        Integer timeout = 1050;
        if (use_local_geocoder) {
            timeout = Integer.parseInt(prop.getProperty(annotatorName + ".timeout"));
        }
        this.geoloc_conf = GeolocModel.getInstance(allowed_entities, geocoder_url, use_local_geocoder, timeout);
    }

    @Override
    public void annotate(Annotation annotation) {
        if (annotation.has(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    if (this.geoloc_conf.getAllowed_loc_type().contains(c.ner())) {
                        String geocoder_url = this.geoloc_conf.getNominatin_url();

                        if (geoloc_conf.isUse_local_geocoder_instance()) {
                            geocoder_url = this.geoloc_conf.getLocal_geocoder_url() + "?format=json&q=";
                        }
                        String coordinates = "";
                        try {
                            URL geocoder_address = new URL(geocoder_url + URLEncoder.encode(c.word()));
                            LOGGER.info(geocoder_address.toString());
                            URLConnection gconn = geocoder_address.openConnection();
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(gconn.getInputStream(), "UTF-8"));
                            String inputLine;
                            StringBuilder a = new StringBuilder();
                            while ((inputLine = in.readLine()) != null) {
                                a.append(inputLine);
                            }
                            in.close();
                            JsonParser parser = new JsonParser();
                            JsonArray locations = (JsonArray) parser.parse(a.toString());
                            if (locations.size() > 0) {
                                String lat = locations.get(0).getAsJsonObject().get("lat").getAsString();
                                String lon = locations.get(0).getAsJsonObject().get("lon").getAsString();
                                coordinates = lat + "," + lon;
                            }
                            Thread.sleep(geoloc_conf.getTimeout());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        c.set(GeolocAnnotation.GEOLOC_ANNOTATION.class, coordinates);

                    }
                }
            }
        }
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        return Collections.singleton(GeolocAnnotation.GEOLOC_ANNOTATION_REQUIREMENT);
    }

    @Override
    public Set<Requirement> requires() {
        return Collections.unmodifiableSet(
                new ArraySet<Requirement>(NER_REQUIREMENT));
    }
}
