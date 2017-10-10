package eu.fbk.dh.tint.readability.gl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.fbk.dh.tint.readability.Readability.getStream;

/**
 * Created by alessio on 26/09/16.
 */
public class GalicianReadabilityModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(GalicianReadabilityModel.class);
    private static GalicianReadabilityModel ourInstance = null;
    private static Pattern POS_PATTERN = Pattern.compile("(.*)_([A-Z+]+)");
    private static Set<String> allowedPos = new HashSet<>();
    static {
        allowedPos.add("ADV");
        allowedPos.add("VERB");
        allowedPos.add("NOUN");
        allowedPos.add("ADJ");
    }

    private static final int LIMIT_EASY = 500;
    private static final int LIMIT_MEDIUM = 2500;
    private static final int LIMIT_HARD = 5000;

    private Set<String> level1Lemmas = new HashSet<>();
    private Set<String> level2Lemmas = new HashSet<>();
    private Set<String> level3Lemmas = new HashSet<>();

    public static void main(String[] args) {
        GalicianReadabilityModel instance = GalicianReadabilityModel.getInstance(new Properties(), new Properties());
        System.out.println(instance.level1Lemmas);
    }

    public static GalicianReadabilityModel getInstance(Properties globalProperties, Properties localProperties) {
        if (ourInstance == null) {
            String freqLemmaFile = localProperties.getProperty("lemmasFile");

            LOGGER.info("Loading lemmas");
            Set<String> level1Lemmas = new HashSet<>();
            Set<String> level2Lemmas = new HashSet<>();
            Set<String> level3Lemmas = new HashSet<>();

            try {
                InputStream stream = getStream(freqLemmaFile, "/models/stats_treegal.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                int i = 0;
                while ((line = reader.readLine()) != null) {

                    line = line.trim();

                    String[] parts = line.split("\\s+");
                    if (parts.length < 2) {
                        continue;
                    }

                    String lemmaPos = parts[1];
                    Matcher matcher = POS_PATTERN.matcher(lemmaPos);
                    if (!matcher.find()) {
                        continue;
                    }
                    String lemma = matcher.group(1);
                    String pos = matcher.group(2);

                    if (!allowedPos.contains(pos)) {
                        continue;
                    }

                    boolean done = false;
                    if (i < LIMIT_EASY) {
                        level1Lemmas.add(lemma);
                        done = true;
                    }
                    if (i < LIMIT_MEDIUM) {
                        level2Lemmas.add(lemma);
                        done = true;
                    }
                    if (i < LIMIT_HARD) {
                        level3Lemmas.add(lemma);
                        done = true;
                    }

                    if (!done) {
                        break;
                    }

                    i++;
                }
                reader.close();
            } catch (Exception e) {
                LOGGER.warn("Unable to load easyWords file: {}", e.getMessage());
            }

            ourInstance = new GalicianReadabilityModel(level1Lemmas, level2Lemmas, level3Lemmas);
        } else {
            LOGGER.info("Readability model already loaded");
        }
        return ourInstance;
    }

    private GalicianReadabilityModel(Set<String> level1Lemmas, Set<String> level2Lemmas,
                                     Set<String> level3Lemmas) {
        this.level1Lemmas = level1Lemmas;
        this.level2Lemmas = level2Lemmas;
        this.level3Lemmas = level3Lemmas;
    }

    public Set<String> getLevel1Lemmas() {
        return level1Lemmas;
    }

    public Set<String> getLevel2Lemmas() {
        return level2Lemmas;
    }

    public Set<String> getLevel3Lemmas() {
        return level3Lemmas;
    }
}
