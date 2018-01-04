package eu.fbk.dh.tint.simplifier.rules;

import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.pipeline.Annotation;

import java.util.Map;

/**
 * Created by alessio on 15/02/17.
 */

public interface SimplificationRule {

    String apply(Annotation annotation, Map<Integer, HashMultimap<Integer, Integer>> children);

}
