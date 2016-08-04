package eu.fbk.dh.tint.digimorph.annotator;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.digimorph.DigiMorph;

import java.util.*;

/**
 * Created by giovannimoretti on 18/05/16.
 */
public class DigiMorphAnnotator implements Annotator {

    DigiMorph dm;

    public DigiMorphAnnotator(String annotatorName, Properties prop) {
        String model_path = prop.getProperty(annotatorName + ".model");
        this.dm = DigiMorphModel.getInstance(model_path);
    }

    public void annotate(Annotation annotation) {


        List<String> token_word = new LinkedList<String>();

        if (annotation.has(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    token_word.add(c.word());
                }
            }
            token_word = dm.getMorphology(token_word);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    c.set(CoreAnnotations.MorphoCaseAnnotation.class, token_word.get(0));
                    token_word.remove(0);
                }
            }

        }
    }

    public Set<Requirement> requirementsSatisfied() {
        return Collections.singleton(DigiMorphAnnotations.DH_MORPHOLOGY_REQUIREMENT);
    }

    public Set<Requirement> requires() {
        return TOKENIZE_AND_SSPLIT;
    }

}


