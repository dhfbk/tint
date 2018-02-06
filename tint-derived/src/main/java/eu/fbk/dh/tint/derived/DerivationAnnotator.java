package eu.fbk.dh.tint.derived;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;


public class DerivationAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DerivationAnnotator.class);
    private DerivedModel model;

    public DerivationAnnotator(String annotatorName, Properties props) {
        model = DerivedModel.getInstance();
    }

    @Override
    public void annotate(Annotation annotation) {
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            String lemma = token.lemma();
            if (model.getDerivations().containsKey(lemma.toLowerCase())) {
                token.set(DerivationAnnotations.DerivationAnnotation.class, model.getDerivations().get(lemma.toLowerCase()));
            }
        }
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(DerivationAnnotations.DerivationAnnotation.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.singleton(CoreAnnotations.LemmaAnnotation.class);
    }
}
