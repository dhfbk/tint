package eu.fbk.dh.tint.tokenizer.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.tokenizer.models.ItalianTokenizerModel;
import eu.fbk.dh.tint.tokenizer.token.ItalianTokenizer;
import eu.fbk.dkm.pikes.tintop.annotators.Defaults;

import java.io.File;
import java.util.*;

/**
 * Created by alessio on 14/07/16.
 */

public class ItalianTokenizerAnnotator implements Annotator {

    boolean newlineIsSentenceBreak;
    ItalianTokenizer tokenizer;

    public ItalianTokenizerAnnotator(String annotatorName, Properties props) {
        String modelFile = props.getProperty(annotatorName + ".model", null);
        newlineIsSentenceBreak = Defaults
                .getBoolean(props.getProperty(annotatorName + ".newlineIsSentenceBreak"), true);

        File model = null;
        if (modelFile != null) {
            model = new File(modelFile);
        }
        tokenizer = ItalianTokenizerModel.getInstance(model).getTokenizer();
    }

    /**
     * Given an Annotation, perform a task on this Annotation.
     *
     * @param annotation
     */
    @Override public void annotate(Annotation annotation) {
        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        List<List<CoreLabel>> sTokens = tokenizer.parse(text, newlineIsSentenceBreak);

        List<CoreMap> sentences = new ArrayList<>();
        ArrayList<CoreLabel> tokens = new ArrayList<>();

        int sIndex = 0;
        int tokenIndex = 0;

        for (List<CoreLabel> sentence : sTokens) {
            if (sentence.size() == 0) {
                continue;
            }

            CoreMap sent = new ArrayCoreMap(1);
            for (CoreLabel coreLabel : sentence) {
                coreLabel.setSentIndex(sIndex);
            }

            int begin = sentence.get(0).beginPosition();
            int end = sentence.get(sentence.size() - 1).endPosition();

            sent.set(CoreAnnotations.TokensAnnotation.class, sentence);

            sent.set(CoreAnnotations.SentenceIndexAnnotation.class, sIndex++);
            sent.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, begin);
            sent.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, end);

            sent.set(CoreAnnotations.TokenBeginAnnotation.class, tokenIndex);
            tokenIndex += sentence.size();
            sent.set(CoreAnnotations.TokenEndAnnotation.class, tokenIndex);
            sent.set(CoreAnnotations.TextAnnotation.class, text.substring(begin, end));

            sentences.add(sent);
            tokens.addAll(sentence);
        }

        annotation.set(CoreAnnotations.TokensAnnotation.class, tokens);
        annotation.set(CoreAnnotations.SentencesAnnotation.class, sentences);

    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Requirement> requirementsSatisfied() {
        return TOKENIZE_AND_SSPLIT;
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Requirement> requires() {
        return Collections.emptySet();
    }
}
