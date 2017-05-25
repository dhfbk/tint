package eu.fbk.dh.tint.readability.en;

import com.itextpdf.layout.hyphenation.Hyphenator;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.utils.gson.JSONExclude;

import java.util.Properties;

/**
 * Created by alessio on 21/09/16.
 */

abstract class EnglishReadability extends Readability {

    @JSONExclude EnglishReadabilityModel model;
    @JSONExclude int level1WordSize = 0, level2WordSize = 0, level3WordSize = 0;
//
//    @JSONExclude StringBuilder buffer = new StringBuilder();
//    @JSONExclude int lemmaIndex = 0;
//    @JSONExclude HashMap<Integer, Integer> lemmaIndexes = new HashMap<>();
//    @JSONExclude HashMap<Integer, Integer> tokenIndexes = new HashMap<>();
//    TreeMap<Integer, DescriptionForm> forms = new TreeMap<>();

    @Override public void finalizeReadability() {
        super.finalizeReadability();


        double flesch = 206.835 - (84.6 * getHyphenCount() / getHyphenWordCount()) - (1.015 * getWordCount()
                / getSentenceCount());
        double fleschKincaid =
                (0.39 * getWordCount() / getSentenceCount()) + (11.8 * getHyphenCount() / getHyphenWordCount()) - 15.59;
        labels.put("main", "Flesch");
        measures.put("main", flesch);
        measures.put("flesch-kincaid", fleschKincaid);
        measures.put("level1", 100.0 * level1WordSize / getContentWordSize());
        measures.put("level2", 100.0 * level2WordSize / getContentWordSize());
        measures.put("level3", 100.0 * level3WordSize / getContentWordSize());
//
//        String lemmaText = buffer.toString().trim();
//        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
//
//        HashMap<String, GlossarioEntry> glossario = model.getGlossario();
//
//        List<String> glossarioKeys = new ArrayList<>(glossario.keySet());
//        Collections.sort(glossarioKeys, new StringLenComparator());
//
//        for (String form : glossarioKeys) {
//
//            int numberOfTokens = form.split("\\s+").length;
//            List<Integer> allOccurrences = findAllOccurrences(text, form);
//            List<Integer> allLemmaOccurrences = findAllOccurrences(lemmaText, form);
//
//            for (Integer occurrence : allOccurrences) {
//                addDescriptionForm(form, tokenIndexes, occurrence, numberOfTokens, forms, annotation, glossario);
//            }
//            for (Integer occurrence : allLemmaOccurrences) {
//                addDescriptionForm(form, lemmaIndexes, occurrence, numberOfTokens, forms, annotation, glossario);
//            }
//        }

    }

    public EnglishReadability(Properties globalProperties, Properties localProperties, Annotation annotation) {
        super("en", annotation, localProperties);
        hyphenator = new Hyphenator("en", "en", 1, 1);
        model = EnglishReadabilityModel.getInstance(globalProperties, localProperties);

//        System.out.println(model.getLevel1Lemmas().size());
//        System.out.println(model.getLevel2Lemmas().size());
//        System.out.println(model.getLevel3Lemmas().size());

//        minYellowValues.put("propositionsAvg", 2.038);
//        maxYellowValues.put("propositionsAvg", 2.699);
//        minValues.put("propositionsAvg", 0.0);
//        maxValues.put("propositionsAvg", 5.0);
//
//        minYellowValues.put("wordsAvg", 9.845);
//        maxYellowValues.put("wordsAvg", 10.153);
//        minValues.put("wordsAvg", 0.0);
//        maxValues.put("wordsAvg", 12.0);

//        minYellowValues.put("coordinateRatio", 0.737);
//        maxYellowValues.put("coordinateRatio", 0.675);
//        minValues.put("coordinateRatio", 0.0);
//        maxValues.put("coordinateRatio", 1.0);

//        minYellowValues.put("subordinateRatio", 0.263);
//        maxYellowValues.put("subordinateRatio", 0.325);
//        minValues.put("subordinateRatio", 0.0);
//        maxValues.put("subordinateRatio", 1.0);

        minYellowValues.put("deepAvg", 5.292);
        maxYellowValues.put("deepAvg", 6.532);
        minValues.put("deepAvg", 0.0);
        maxValues.put("deepAvg", 10.0);

        minYellowValues.put("deepMax", 9.0);
        maxYellowValues.put("deepMax", 12.0);
        minValues.put("deepMax", 0.0);
        maxValues.put("deepMax", 20.0);

        minYellowValues.put("ttrValue", 0.549);
        maxYellowValues.put("ttrValue", 0.719);
        minValues.put("ttrValue", 0.0);
        maxValues.put("ttrValue", 1.0);

        minYellowValues.put("density", 0.566);
        maxYellowValues.put("density", 0.566);
        minValues.put("density", 0.0);
        maxValues.put("density", 1.0);
    }

    @Override public void addingContentWord(CoreLabel token) {
        super.addingContentWord(token);

        String lemma = token.word();
        if (model.getLevel1Lemmas().contains(lemma)) {
            level1WordSize++;
        }
        if (model.getLevel2Lemmas().contains(lemma)) {
            level2WordSize++;
        }
        if (model.getLevel3Lemmas().contains(lemma)) {
            level3WordSize++;
        }
//        System.out.println("Adding content word (lemma): " + lemma);
//        System.out.println(model.getLevel1Lemmas().contains(lemma));
//        System.out.println(model.getLevel2Lemmas().contains(lemma));
//        System.out.println(model.getLevel3Lemmas().contains(lemma));
//        System.out.println();

//        HashMap<Integer, HashMultimap<String, String>> easyWords = model.getEasyWords();
//        String simplePos = getGenericPos(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
//        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
//
//        if (easyWords.get(1).get(simplePos).contains(lemma)) {
//            level1WordSize++;
//        }
//        if (easyWords.get(2).get(simplePos).contains(lemma)) {
//            level2WordSize++;
//        }
//        if (easyWords.get(3).get(simplePos).contains(lemma)) {
//            level3WordSize++;
//        }
    }

    @Override public void addingEasyWord(CoreLabel token) {

    }

    @Override public void addingWord(CoreLabel token) {
        super.addingWord(token);
    }

    @Override public void addingToken(CoreLabel token) {
//        lemmaIndexes.put(buffer.length(), lemmaIndex);
//        tokenIndexes.put(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), lemmaIndex);
//        lemmaIndex++;
//        buffer.append(token.get(CoreAnnotations.LemmaAnnotation.class)).append(" ");
    }

    @Override public void addingSentence(CoreMap sentence) {

    }
}
