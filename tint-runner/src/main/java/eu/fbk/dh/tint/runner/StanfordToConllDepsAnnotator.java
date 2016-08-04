package eu.fbk.dh.tint.runner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dkm.pikes.tintop.annotators.DepParseInfo;
import eu.fbk.dkm.pikes.tintop.annotators.PikesAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by alessio on 06/05/15.
 */

public class StanfordToConllDepsAnnotator implements Annotator {

    public StanfordToConllDepsAnnotator(String annotatorName, Properties props) {

    }

    @Override
    public void annotate(Annotation annotation) {
        if (annotation.has(CoreAnnotations.SentencesAnnotation.class)) {
            int sentOffset = 0;
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                SemanticGraph dependencies = sentence.get(
                        SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
                DepParseInfo info = new DepParseInfo(dependencies);
//                System.out.println(info);
//                System.out.println();
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                if (dependencies != null) {
                    for (int i = 0; i < tokens.size(); i++) {
                        CoreLabel token = tokens.get(i);
                        int j = i + sentOffset;

                        String label = info.getDepLabels().get(j + 1);
                        int head = info.getDepParents().get(j + 1) - 1 - sentOffset;
                        if (head < -1) {
                            head = -1;
                        }
//                        System.out.println(j);
//                        System.out.println(token);
//                        System.out.println(label);
//                        System.out.println(head);
//                        System.out.println();
                        token.set(CoreAnnotations.CoNLLDepTypeAnnotation.class, label);
                        token.set(CoreAnnotations.CoNLLDepParentIndexAnnotation.class, head);
                    }
                }
                sentOffset += tokens.size();
            }
        } else {
            throw new RuntimeException("unable to find words/tokens in: " + annotation);
        }
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        return Collections.singleton(PikesAnnotations.CONLLPARSE_REQUIREMENT);
    }

    @Override
    public Set<Requirement> requires() {
        return TOKENIZE_SSPLIT_POS_DEPPARSE;
    }
}
