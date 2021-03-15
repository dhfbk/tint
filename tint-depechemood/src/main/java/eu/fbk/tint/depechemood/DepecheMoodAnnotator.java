package eu.fbk.tint.depechemood;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.utils.core.PropertiesUtils;

import java.net.URL;
import java.util.*;

public class DepecheMoodAnnotator implements Annotator {

    public static String DEFAULT_LANGUAGE = "en";
    public static int DEFAULT_MINFREQ = 10;

    private String lang;
    private int minFreq;

    private List<String> labels = new ArrayList<>();
    private Map<String, Map<String, Double>> conversionMap = new HashMap<>();

    public DepecheMoodAnnotator(String annotatorName, Properties props) throws Exception {
        Properties newProps = PropertiesUtils.dotConvertedProperties(props, annotatorName);
        lang = newProps.getProperty("language", DEFAULT_LANGUAGE);
        minFreq = PropertiesUtils.getInteger(newProps.getProperty("min_freq"), DEFAULT_MINFREQ);

        // TODO: bad
        URL resource = Resources.getResource("DepecheMood_english_lemma_full.tsv");
        if (lang.equals("it")) {
            resource = Resources.getResource("DepecheMood_italian_lemma_full.tsv");
        }

        boolean firstLine = true;
        labels = new ArrayList<>();
        for (String line : Resources.readLines(resource, Charsets.UTF_8)) {
            String[] parts = line.split("\t");
            if (line.startsWith("#")) {
                continue;
            }
            if (firstLine) {
                for (int i = 0; i < parts.length; i++) {
                    if (i == 0) {
                        continue;
                    }
                    if (i == parts.length - 1) {
                        continue;
                    }
                    labels.add(parts[i]);
                }
                firstLine = false;
                continue;
            }
            int freq = Integer.parseInt(parts[parts.length - 1]);
            if (freq < minFreq) {
                continue;
            }
            String lemma = parts[0];
            conversionMap.put(lemma, new HashMap<>());
            for (int i = 0; i < labels.size(); i++) {
                String label = labels.get(i);
                conversionMap.get(lemma).put(label, Double.parseDouble(parts[i + 1]));
            }
        }

    }

    @Override
    public void annotate(Annotation annotation) {
        Map<String, Double> documentMap = new HashMap<>();
        int documentQty = 0;
        for (String label : labels) {
            documentMap.put(label, 0.0d);
        }
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Map<String, Double> sentenceMap = new HashMap<>();
            int sentenceQty = 0;
            for (String label : labels) {
                sentenceMap.put(label, 0.0d);
            }
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                boolean contentWord = token.get(ReadabilityAnnotations.ContentWord.class);
                if (!contentWord) {
                    continue;
                }
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                lemma = lemma.toLowerCase();
                if (conversionMap.containsKey(lemma)) {
                    token.set(DepecheMoodAnnotations.DepecheMoodAnnotation.class, conversionMap.get(lemma));
                    sentenceQty++;
                    documentQty++;
                    for (String key : conversionMap.get(lemma).keySet()) {
                        sentenceMap.put(key, sentenceMap.get(key) + conversionMap.get(lemma).get(key));
                        documentMap.put(key, documentMap.get(key) + conversionMap.get(lemma).get(key));
                    }
                }
            }
            for (String key : sentenceMap.keySet()) {
                sentenceMap.put(key, sentenceMap.get(key) / sentenceQty);
            }
            sentence.set(DepecheMoodAnnotations.DepecheMoodAnnotation.class, sentenceMap);
        }
        for (String key : documentMap.keySet()) {
            documentMap.put(key, documentMap.get(key) / documentQty);
        }
        annotation.set(DepecheMoodAnnotations.DepecheMoodAnnotation.class, documentMap);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(DepecheMoodAnnotations.DepecheMoodAnnotation.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class,
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.LemmaAnnotation.class,
                ReadabilityAnnotations.ReadabilityAnnotation.class
        )));
    }
}
