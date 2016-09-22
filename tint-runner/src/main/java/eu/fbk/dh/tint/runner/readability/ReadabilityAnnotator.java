package eu.fbk.dh.tint.runner.readability;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;
import java.util.Set;

/**
 * Created by alessio on 21/09/16.
 */

public class ReadabilityAnnotator implements Annotator {

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

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

            int sentenceID = sentence.get(CoreAnnotations.IndexAnnotation.class);
            int wordsNow = readability.getWordCount();
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                readability.addWord(lemma, pos);
            }
            int words = readability.getWordCount() - wordsNow;
            if (words > maxSentenceLength) {
                readability.addTooLongSentence(sentenceID);
            }
        }
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Requirement> requirementsSatisfied() {
        return null;
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
