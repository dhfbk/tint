package eu.fbk.dh.tint.readability.it;

import com.google.common.collect.HashMultimap;
import com.itextpdf.layout.hyphenation.Hyphenator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.DescriptionForm;
import eu.fbk.dh.tint.readability.GlossarioEntry;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.utils.gson.JSONExclude;

import java.util.*;

/**
 * Created by alessio on 21/09/16.
 */

public abstract class ItalianReadability extends Readability {

    @JSONExclude ItalianReadabilityModel model;
    @JSONExclude int level1WordSize = 0, level2WordSize = 0, level3WordSize = 0;

    @JSONExclude StringBuilder buffer = new StringBuilder();
    @JSONExclude int lemmaIndex = 0;
    @JSONExclude HashMap<Integer, Integer> lemmaIndexes = new HashMap<>();
    @JSONExclude HashMap<Integer, Integer> tokenIndexes = new HashMap<>();
    TreeMap<Integer, DescriptionForm> forms = new TreeMap<>();

    @Override public void finalizeReadability() {
        super.finalizeReadability();

        double gulpease = 89 + (300 * getSentenceCount() - 10 * getDocLenLettersOnly()) / (getWordCount() * 1.0);
        labels.put("main", "Gulpease");
        measures.put("main", gulpease);
        measures.put("level1", 100.0 * level1WordSize / getContentEasyWordSize());
        measures.put("level2", 100.0 * level2WordSize / getContentWordSize());
        measures.put("level3", 100.0 * level3WordSize / getContentWordSize());

        String lemmaText = buffer.toString().trim();
        String text = annotation.get(CoreAnnotations.TextAnnotation.class);

        HashMap<String, GlossarioEntry> glossario = model.getGlossario();

        List<String> glossarioKeys = new ArrayList<>(glossario.keySet());
        Collections.sort(glossarioKeys, new StringLenComparator());

        for (String form : glossarioKeys) {

            int numberOfTokens = form.split("\\s+").length;
            List<Integer> allOccurrences = findAllOccurrences(text, form);
            List<Integer> allLemmaOccurrences = findAllOccurrences(lemmaText, form);

            for (Integer occurrence : allOccurrences) {
                addDescriptionForm(form, tokenIndexes, occurrence, numberOfTokens, forms, annotation, glossario);
            }
            for (Integer occurrence : allLemmaOccurrences) {
                addDescriptionForm(form, lemmaIndexes, occurrence, numberOfTokens, forms, annotation, glossario);
            }
        }

    }

    public ItalianReadability(Properties globalProperties, Properties localProperties, Annotation annotation) {
        super("it", annotation, localProperties);
        hyphenator = new Hyphenator("it", "it", 1, 1);
        model = ItalianReadabilityModel.getInstance(globalProperties, localProperties);

        measures.put("propositionsAvgYellow", 2.038);
        measures.put("propositionsAvgYellow", 2.699);
        measures.put("propositionsAvg", 0.0);
        measures.put("propositionsAvg", 5.0);

        measures.put("wordsAvgYellow", 9.845);
        measures.put("wordsAvgYellow", 10.153);
        measures.put("wordsAvg", 0.0);
        measures.put("wordsAvg", 12.0);
        
        measures.put("subordinateRatioYellow", 0.263);
        measures.put("subordinateRatioYellow", 0.325);
        measures.put("subordinateRatio", 0.0);
        measures.put("subordinateRatio", 1.0);

        measures.put("deepAvgYellow", 5.292);
        measures.put("deepAvgYellow", 6.532);
        measures.put("deepAvg", 0.0);
        measures.put("deepAvg", 10.0);

        measures.put("ttrValueYellow", 0.549);
        measures.put("ttrValueYellow", 0.719);
        measures.put("ttrValue", 0.0);
        measures.put("ttrValue", 1.0);

        measures.put("densityYellow", 0.566);
        measures.put("densityYellow", 0.566);
        measures.put("density", 0.0);
        measures.put("density", 1.0);
    }

    public static class StringLenComparator implements Comparator<String> {

        public int compare(String s1, String s2) {
            return s1.length() - s2.length();
        }
    }

    public static List<Integer> findAllOccurrences(String haystack, String needle) {

        List<Integer> ret = new ArrayList<>();

        int index = haystack.indexOf(needle);
        while (index >= 0) {
            try {
                String afterChar = haystack.substring(index + needle.length(), index + needle.length() + 1);
                if (!afterChar.matches("\\w+")) {
                    ret.add(index);
                }
            } catch (Exception e) {
                // ignore
            }
            index = haystack.indexOf(needle, index + 1);
        }

        return ret;
    }

    static public void addDescriptionForm(String form, HashMap<Integer, Integer> indexes, int start,
            int numberOfTokens, TreeMap<Integer, DescriptionForm> forms, Annotation annotation,
            HashMap<String, GlossarioEntry> glossario) {
        Integer lemmaIndex = indexes.get(start);
        if (lemmaIndex == null) {
            return;
        }

        CoreLabel firstToken = annotation.get(CoreAnnotations.TokensAnnotation.class).get(lemmaIndex);
        CoreLabel endToken = annotation.get(CoreAnnotations.TokensAnnotation.class)
                .get(lemmaIndex + numberOfTokens - 1);
        Integer beginOffset = firstToken.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
        Integer endOffset = endToken.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

        GlossarioEntry glossarioEntry = glossario.get(form);
        if (glossarioEntry == null) {
            return;
        }

        DescriptionForm descriptionForm = new DescriptionForm(
                beginOffset, endOffset, glossarioEntry);

        forms.put(beginOffset, descriptionForm);
    }

    @Override public void addingContentWord(CoreLabel token) {
        super.addingContentWord(token);
        HashMap<Integer, HashMultimap<String, String>> easyWords = model.getEasyWords();
        String simplePos = getGenericPos(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

        if (easyWords.get(3).get(simplePos).contains(lemma)) {
            level3WordSize++;
            token.set(ReadabilityAnnotations.DifficultyLevelAnnotation.class, 3);
        }
        if (easyWords.get(2).get(simplePos).contains(lemma)) {
            level2WordSize++;
            token.set(ReadabilityAnnotations.DifficultyLevelAnnotation.class, 2);
        }
        if (easyWords.get(1).get(simplePos).contains(lemma)) {
            level1WordSize++;
            token.set(ReadabilityAnnotations.DifficultyLevelAnnotation.class, 1);
        }
    }

    @Override public void addingEasyWord(CoreLabel token) {

    }

    @Override public void addingWord(CoreLabel token) {
        super.addingWord(token);
    }

    @Override public void addingToken(CoreLabel token) {
        lemmaIndexes.put(buffer.length(), lemmaIndex);
        tokenIndexes.put(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), lemmaIndex);
        lemmaIndex++;
        buffer.append(token.get(CoreAnnotations.LemmaAnnotation.class)).append(" ");

    }

    @Override public void addingSentence(CoreMap sentence) {

    }
}
