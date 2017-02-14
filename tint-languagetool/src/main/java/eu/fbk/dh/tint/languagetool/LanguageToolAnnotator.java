package eu.fbk.dh.tint.languagetool;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by alessio on 02/12/16.
 */

public class LanguageToolAnnotator implements Annotator {

    private String server = "localhost";
    private String port = "8081";
    private String protocol = "http";
    private String language = "it";

    public static String urlTemplate = "%s://%s:%s/v2/check?language=%s&text=";

    public LanguageToolAnnotator(String annotatorName, Properties prop) {
        server = prop.getProperty(annotatorName + ".server", server);
        port = prop.getProperty(annotatorName + ".port", port);
        protocol = prop.getProperty(annotatorName + ".protocol", protocol);
        language = prop.getProperty(annotatorName + ".language", language);
    }

    @Override public void annotate(Annotation annotation) {
        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        if (text == null) {
            return;
        }

        Reader json;
        try {
            json = request(text);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JsonParser parser = new JsonParser();
        Gson gson = new Gson();

        JsonArray matchList = parser.parse(json).getAsJsonObject().get("matches").getAsJsonArray();
        List<Match> matches = new ArrayList<>();
        for (JsonElement jsonElement : matchList) {
            Match match = gson.fromJson(jsonElement.getAsJsonObject(), Match.class);
            matches.add(match);
        }

        annotation.set(LanguageToolAnnotations.LanguageToolMultiAnnotation.class, matches);
    }

    synchronized Reader request(String word) throws IOException {
        String url = String.format(urlTemplate, protocol, server, port, language);

        URL address = new URL(url + URLEncoder.encode(word, "UTF-8"));
        URLConnection conn = address.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        return in;
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(LanguageToolAnnotations.LanguageToolMultiAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.emptySet();
    }

    public static void main(String[] args) {
        String text = "Inserite qui il vostro testo... oppure controlate direttamente questo ed avrete un assaggio di quali errori possono essere identificati con LanguageTool.";

        Properties properties = new Properties();
        properties.setProperty("annotators", "languagetool");
        properties
                .setProperty("customAnnotatorClass.languagetool", "eu.fbk.dh.tint.languagetool.LanguageToolAnnotator");
        StanfordCoreNLP stanfordCoreNLP = new StanfordCoreNLP(properties);

        Annotation annotation = new Annotation(text);
        stanfordCoreNLP.annotate(annotation);

    }
}
