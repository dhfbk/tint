package eu.fbk.dh.tint.geoloc.annotator;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
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
    private static final String DEFAULT_ENTITY_TYPES = "LOC";
    private static final String DEFAULT_NO_ENTITY = "O";

    private boolean setRaw = false;

    public GeolocAnnotator(String annotatorName, Properties prop) {
        List<String> allowed_entities = new ArrayList<>();
        String entityTypes = prop.getProperty(annotatorName + ".allowed_entity_type", DEFAULT_ENTITY_TYPES);
        for (String entity : entityTypes.split(",")) {
            entity = entity.trim();
            allowed_entities.add(entity);
        }

        String geocoder_url = prop.getProperty(annotatorName + ".geocoder_url");
        Boolean use_local_geocoder = PropertiesUtils
                .getBoolean(prop.getProperty(annotatorName + ".use_local_geocoder"), false);
        Integer timeout = 1050;
        if (use_local_geocoder) {
            timeout = PropertiesUtils.getInteger(prop.getProperty(annotatorName + ".timeout"), timeout);
        }
        this.geoloc_conf = GeolocModel.getInstance(allowed_entities, geocoder_url, use_local_geocoder, timeout);

        setRaw = PropertiesUtils.getBoolean(prop.getProperty(annotatorName + ".set_raw"), setRaw);
    }

    synchronized String request(String word) throws IOException {
        String geocoder_url = this.geoloc_conf.getNominatin_url();
        if (geoloc_conf.isUse_local_geocoder_instance()) {
            geocoder_url = this.geoloc_conf.getLocal_geocoder_url() + "?format=json&q=";
        }

        URL geocoder_address = new URL(geocoder_url + URLEncoder.encode(word, "UTF-8"));
        LOGGER.debug(geocoder_address.toString());
        URLConnection gconn = geocoder_address.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(gconn.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder a = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            a.append(inputLine);
        }
        in.close();
        return a.toString();
    }

    @Override
    public void annotate(Annotation annotation) {
        List<GeocodResult> res = new ArrayList<>();
        Map<String, GeocodResult> cache = new HashMap<>();

        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

                String lastNer = DEFAULT_NO_ENTITY;
                Map<Integer, Integer> tokensToEvaluate = new HashMap<>();
                Integer lastTokenToEvaluate = null;

                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                Map<Integer, CoreLabel> tokensIndex = new HashMap<>();

                for (CoreLabel c : tokens) {
                    tokensIndex.put(c.index(), c);

                    String ner = c.ner();
                    if (ner == null) {
                        ner = DEFAULT_NO_ENTITY;
                    }
                    if (!this.geoloc_conf.getAllowed_loc_type().contains(ner)) {
                        ner = DEFAULT_NO_ENTITY;
                    }

                    if (ner.equals(DEFAULT_NO_ENTITY)) {
                        lastTokenToEvaluate = null;
                    } else {
                        if (lastNer.equals(ner)) {
                            tokensToEvaluate.put(lastTokenToEvaluate, c.index());
                        } else {
                            lastTokenToEvaluate = c.index();
                            tokensToEvaluate.put(lastTokenToEvaluate, lastTokenToEvaluate);
                        }
                    }
                    lastNer = ner;
                }

                for (Integer index : tokensToEvaluate.keySet()) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = index; i <= tokensToEvaluate.get(index); i++) {
                        CoreLabel token = tokensIndex.get(i);
                        builder.append(token.word()).append(" ");
                    }
                    int start = tokensIndex.get(index).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    int end = tokensIndex.get(tokensToEvaluate.get(index))
                            .get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                    String word = builder.toString().trim();
                    GeocodResult result = cache.get(word);

                    if (result == null) {
                        String response;

                        try {
                            response = request(word);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }

                        String lat;
                        String lon;

                        JsonParser parser = new JsonParser();
                        JsonArray locations = (JsonArray) parser.parse(response);
                        if (locations.size() > 0) {
                            lat = locations.get(0).getAsJsonObject().get("lat").getAsString();
                            lon = locations.get(0).getAsJsonObject().get("lon").getAsString();
                            result = new GeocodResult(Double.parseDouble(lon),
                                    Double.parseDouble(lat), start, end);
                            if (setRaw) {
                                result.setRaw(locations);
                            }
                            result.setText(word);
                            cache.put(word, result);
                        }
                    }

                    if (result != null) {
                        res.add(result);

                        for (int i = index; i <= tokensToEvaluate.get(index); i++) {
                            CoreLabel token = tokensIndex.get(i);
                            token.set(GeolocAnnotations.GeolocAnnotation.class, result);
                        }
                    }

                    try {
                        Thread.sleep(geoloc_conf.getTimeout());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        annotation.set(GeolocAnnotations.GeolocMultiAnnotation.class, res);
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                GeolocAnnotations.GeolocAnnotation.class,
                GeolocAnnotations.GeolocMultiAnnotation.class
        )));
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.singleton(CoreAnnotations.NamedEntityTagAnnotation.class);
    }
//    @Override
//    public Set<Requirement> requirementsSatisfied() {
//        return Collections.singleton(GeolocAnnotations.GEOLOC_ANNOTATION_REQUIREMENT);
//    }
//
//    @Override
//    public Set<Requirement> requires() {
//        return Collections.unmodifiableSet(
//                new ArraySet<Requirement>(NER_REQUIREMENT));
//    }
}
