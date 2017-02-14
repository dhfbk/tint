package eu.fbk.dh.tint.inverse.digimorph.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.digimorph.annotator.DigiMorphAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by giovannimoretti on 31/01/17.
 */

public class DigiInverseMorphAnnotator implements Annotator {

    public void annotate(Annotation annotation) {
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    String[] morph_features = c.get(DigiMorphAnnotations.MorphoAnnotation.class).split(" ");

                    c.set(DigiInverseMorphAnnotations.InverseMorphoAnnotation.class, morph_features[0]);

                }
            }
        }

    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(DigiInverseMorphAnnotations.InverseMorphoAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.singleton(DigiMorphAnnotations.MorphoAnnotation.class);
    }
}