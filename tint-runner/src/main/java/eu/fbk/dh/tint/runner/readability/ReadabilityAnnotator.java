package eu.fbk.dh.tint.runner.readability;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by alessio on 21/09/16.
 */

public class ReadabilityAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadabilityAnnotator.class);
    public static String DEFAULT_MAX_SENTENCE_LENGTH = "25";
    private String language;
    private int maxSentenceLength;

    public ReadabilityAnnotator(String annotatorName, Properties props) {
        language = props.getProperty(annotatorName + ".language", null);
        maxSentenceLength = Integer
                .parseInt(props.getProperty(annotatorName + ".maxSentenceLength", DEFAULT_MAX_SENTENCE_LENGTH));
    }

    /**
     * Given an Annotation, perform a task on this Annotation.
     *
     * @param annotation
     */
    @Override public void annotate(Annotation annotation) {

        Readability readability = null;
        if (language == null) {
            LOGGER.warn("Language variable is not defined, readability will be empty");
            return;
        }

        switch (language) {
        case "it":
            readability = new ItalianStandardReadability();
            break;
//        case "es":
//            readability = new SpanishReadability();
//            break;
//        default:
//            readability = new EnglishReadability();
        }

        if (readability == null) {
            return;
        }

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        int tokenCount = 0;
        readability.setSentenceCount(sentences.size());
        for (CoreMap sentence : sentences) {
            int sentenceID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
            int wordsNow = readability.getWordCount();
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                readability.addWord(token);
                tokenCount++;
            }
            int words = readability.getWordCount() - wordsNow;
            if (words > maxSentenceLength) {
                readability.addTooLongSentence(sentenceID);
            }
        }
        readability.setTokenCount(tokenCount);

        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        readability.setDocLenWithSpaces(text.length());
        readability.setDocLenWithoutSpaces(text.replaceAll("\\s+", "").length());

        annotation.set(ReadabilityAnnotations.ReadabilityAnnotation.class, readability);
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Requirement> requirementsSatisfied() {
        return Collections.emptySet();
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Requirement> requires() {
        return TOKENIZE_SSPLIT_POS_LEMMA;
    }
}
