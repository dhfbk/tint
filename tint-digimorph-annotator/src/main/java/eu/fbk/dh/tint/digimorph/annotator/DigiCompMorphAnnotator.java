package eu.fbk.dh.tint.digimorph.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Created by giovannimoretti on 15/02/17.
 */
public class DigiCompMorphAnnotator implements Annotator {

    @Override
    public void annotate(Annotation annotation) {
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    String[] morph_fatures = c.get(DigiMorphAnnotations.MorphoAnnotation.class).split(" ");
                    String lemma = c.get(CoreAnnotations.LemmaAnnotation.class);
                    if (morph_fatures.length > 1) {
                        List<String> comps = new ArrayList<>();
                        for (String m : morph_fatures) {
                            if (m.startsWith(lemma + "+") || m.startsWith(lemma + "~")) {
                                comps.add(m);
                            }
                        }
                        c.set(DigiMorphAnnotations.MorphoCompAnnotation.class, comps);
                    } else {

                        if (morph_fatures[0].startsWith(lemma + "+") || morph_fatures[0].startsWith(lemma + "~")) {
                            c.set(DigiMorphAnnotations.MorphoCompAnnotation.class,
                                    new ArrayList<String>(Arrays.asList(morph_fatures[0])));
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(DigiMorphAnnotations.MorphoCompAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.LemmaAnnotation.class,
                DigiMorphAnnotations.MorphoAnnotation.class
        )));
    }
}
