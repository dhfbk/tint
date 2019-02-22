package eu.fbk.dh.tint.readability.gl;

import com.itextpdf.layout.hyphenation.Hyphenation;
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

abstract class GalicianReadability extends Readability {

    @JSONExclude GalicianReadabilityModel model;
    @JSONExclude int level1WordSize = 0, level2WordSize = 0, level3WordSize = 0;

    public static void main(String[] args) {
        Hyphenator hyphenator = new Hyphenator("gl", "es", 1, 1);
        Hyphenation produción = hyphenator.hyphenate("produción");
        System.out.println(produción);
    }

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

    }

    public GalicianReadability(Properties globalProperties, Properties localProperties, Annotation annotation) {
        super("gl", annotation, localProperties);
        hyphenator = new Hyphenator("es", "es", 1, 1);
        model = GalicianReadabilityModel.getInstance(globalProperties, localProperties);

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
    }

    @Override public void addingEasyWord(CoreLabel token) {

    }

    @Override public void addingWord(CoreLabel token) {
        super.addingWord(token);
    }

    @Override public void addingToken(CoreLabel token) {
    }

    @Override public void addingSentence(CoreMap sentence) {

    }
}
