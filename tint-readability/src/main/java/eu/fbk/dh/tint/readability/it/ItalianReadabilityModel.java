package eu.fbk.dh.tint.readability.it;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.readability.GlossarioEntry;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Created by alessio on 26/09/16.
 */
public class ItalianReadabilityModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItalianReadabilityModel.class);
    private static ItalianReadabilityModel ourInstance = null;
    private HashMap<String, GlossarioEntry> glossario = new HashMap<>();
    private HashMap<Integer, HashMultimap<String, String>> easyWords = new HashMap<>();

    public static ItalianReadabilityModel getInstance(Properties globalProperties, Properties localProperties) {
        if (ourInstance == null) {
            Properties stanfordProperties = PropertiesUtils
                    .dotConvertedProperties(localProperties, "glossario.stanford");
            for (String key : globalProperties.stringPropertyNames()) {
                if (stanfordProperties.getProperty(key) == null) {
                    stanfordProperties.setProperty(key, globalProperties.getProperty(key));
                }
            }

            String glossarioFileName = localProperties.getProperty("glossario");
            String easyWordsFileName = localProperties.getProperty("easyWords");

            Boolean parseGlossario = PropertiesUtils
                    .getBoolean(localProperties.getProperty("glossario.parse", "true"), true);

            StanfordCoreNLP pipeline = new StanfordCoreNLP(stanfordProperties);
            Gson gson = new Gson();

            // Loading simple words

            EasyLanguage easyLanguage = new EasyLanguage();
            LOGGER.info("Loading easy lemmas");
            try {
                InputStream stream = Readability.getStream(easyWordsFileName, "/models/easy-output.json");
                JsonReader reader = new JsonReader(new InputStreamReader(stream));
                easyLanguage = gson.fromJson(reader, EasyLanguage.class);
            } catch (Exception e) {
                LOGGER.warn("Unable to load easyWords file: {}", e.getMessage());
            }

            HashMap<Integer, HashMultimap<String, String>> easyWords = new HashMap<>();

            easyWords.put(1, HashMultimap.create());
            easyWords.get(1).putAll("S", Arrays.asList(easyLanguage.level1.n));
//        easyWords.get(1).putAll("A", Arrays.asList(easyLanguage.level1.a));
//        easyWords.get(1).putAll("B", Arrays.asList(easyLanguage.level1.r));
            easyWords.get(1).putAll("V", Arrays.asList(easyLanguage.level1.v));
            easyWords.put(2, HashMultimap.create());
            easyWords.get(2).putAll("S", Arrays.asList(easyLanguage.level2.n));
            easyWords.get(2).putAll("A", Arrays.asList(easyLanguage.level2.a));
            easyWords.get(2).putAll("B", Arrays.asList(easyLanguage.level2.r));
            easyWords.get(2).putAll("V", Arrays.asList(easyLanguage.level2.v));
            easyWords.put(3, HashMultimap.create());
            easyWords.get(3).putAll("S", Arrays.asList(easyLanguage.level3.n));
            easyWords.get(3).putAll("A", Arrays.asList(easyLanguage.level3.a));
            easyWords.get(3).putAll("B", Arrays.asList(easyLanguage.level3.r));
            easyWords.get(3).putAll("V", Arrays.asList(easyLanguage.level3.v));

            // Loading glossario

            HashMap<String, GlossarioEntry> glossario = new HashMap<>();
            LOGGER.info("Loading glossario");
            try {
                InputStream stream = Readability.getStream(glossarioFileName, "/models/glossario-parsed-edited.json");
                JsonReader reader = new JsonReader(new InputStreamReader(stream));
                GlossarioEntry[] entries = gson.fromJson(reader, GlossarioEntry[].class);
                for (GlossarioEntry entry : entries) {
                    for (String form : entry.getForms()) {

                        if (parseGlossario) {
                            Annotation annotation = new Annotation(form);
                            pipeline.annotate(annotation);
                            StringBuffer stringBuffer = new StringBuffer();
                            List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
                            for (CoreLabel token : tokens) {
                                stringBuffer.append(token.get(CoreAnnotations.LemmaAnnotation.class)).append(" ");
                            }

                            String pos = entry.getPos();
                            String annotatedPos = tokens.get(0).get(CoreAnnotations.PartOfSpeechAnnotation.class);
                            if (pos == null || annotatedPos.substring(0, 1).equals("S")) {
                                glossario.put(stringBuffer.toString().trim(), entry);
                            }
                        }

                        glossario.put(form, entry);
                    }
                }

            } catch (Exception e) {
                LOGGER.warn("Unable to load glossario file: {}", e.getMessage());
            }

            ourInstance = new ItalianReadabilityModel(glossario, easyWords);
        } else {
            LOGGER.info("Readability model already loaded");
        }
        return ourInstance;
    }

    private ItalianReadabilityModel(
            HashMap<String, GlossarioEntry> glossario,
            HashMap<Integer, HashMultimap<String, String>> easyWords) {
        this.glossario = glossario;
        this.easyWords = easyWords;
    }

    public HashMap<String, GlossarioEntry> getGlossario() {
        return glossario;
    }

    public HashMap<Integer, HashMultimap<String, String>> getEasyWords() {
        return easyWords;
    }
}
