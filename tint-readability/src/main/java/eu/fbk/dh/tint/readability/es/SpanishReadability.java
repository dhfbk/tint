package eu.fbk.dh.tint.readability.es;

import com.itextpdf.layout.hyphenation.Hyphenator;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.utils.gson.JSONExclude;

import java.util.Properties;

/**
 * Created by alessio on 21/09/16.
 */

abstract class SpanishReadability extends Readability {

    @JSONExclude SpanishReadabilityModel model;
    @JSONExclude int level1WordSize = 0, level2WordSize = 0, level3WordSize = 0;
//
//    @JSONExclude StringBuilder buffer = new StringBuilder();
//    @JSONExclude int lemmaIndex = 0;
//    @JSONExclude HashMap<Integer, Integer> lemmaIndexes = new HashMap<>();
//    @JSONExclude HashMap<Integer, Integer> tokenIndexes = new HashMap<>();
//    TreeMap<Integer, DescriptionForm> forms = new TreeMap<>();

    @Override public void finalizeReadability() {
        super.finalizeReadability();


        double fleschSzigriszt =
                206.835 - (62.3 * getHyphenCount() / getHyphenWordCount()) - (1.0 * getWordCount()
                        / getSentenceCount());
        double fernandezHuerta =
                206.84 - 0.6 * (100.0 * getHyphenCount() / getHyphenWordCount()) - 1.02 * (100.0 * getSentenceCount()
                        / getWordCount());
        labels.put("main", "Flesch-Szigriszt");
        measures.put("main", fleschSzigriszt);
        measures.put("fernandez-huerta", fernandezHuerta);
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

    public SpanishReadability(Properties globalProperties, Properties localProperties, Annotation annotation) {
        super("es", annotation, localProperties);
        hyphenator = new Hyphenator("es", "es", 1, 1);
        model = SpanishReadabilityModel.getInstance(globalProperties, localProperties);
    }

    @Override public void addingContentWord(CoreLabel token) {
        super.addingContentWord(token);

        token.set(ReadabilityAnnotations.DifficultyLevelAnnotation.class, 4);
        String lemma = token.lemma();
        if (model.getLevel3Lemmas().contains(lemma)) {
            level3WordSize++;
            token.set(ReadabilityAnnotations.DifficultyLevelAnnotation.class, 3);
        }
        if (model.getLevel2Lemmas().contains(lemma)) {
            level2WordSize++;
            token.set(ReadabilityAnnotations.DifficultyLevelAnnotation.class, 2);
        }
        if (model.getLevel1Lemmas().contains(lemma)) {
            level1WordSize++;
            token.set(ReadabilityAnnotations.DifficultyLevelAnnotation.class, 1);
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
