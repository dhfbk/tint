package eu.fbk.dh.tint.tokenizer.annotators;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.tokenizer.ItalianTokenizer;
import eu.fbk.dh.tint.tokenizer.models.ItalianTokenizerModel;
import eu.fbk.utils.core.PropertiesUtils;
import eu.fbk.utils.corenlp.Utils;

import java.io.File;
import java.util.*;

/**
 * Created by alessio on 14/07/16.
 */

public class ItalianTokenizerAnnotator implements Annotator {

    boolean newlineIsSentenceBreak, tokenizeOnlyOnSpace, ssplitOnlyOnNewLine;
    ItalianTokenizer tokenizer;

    public ItalianTokenizerAnnotator(String annotatorName, Properties props) {
        String modelFile = props.getProperty(annotatorName + ".model", null);

        newlineIsSentenceBreak = PropertiesUtils
                .getBoolean(props.getProperty(annotatorName + ".newlineIsSentenceBreak"), true);
        tokenizeOnlyOnSpace = PropertiesUtils
                .getBoolean(props.getProperty(annotatorName + ".tokenizeOnlyOnSpace"), false);
        ssplitOnlyOnNewLine = PropertiesUtils
                .getBoolean(props.getProperty(annotatorName + ".ssplitOnlyOnNewLine"), false);
        if (ssplitOnlyOnNewLine) {
            newlineIsSentenceBreak = true;
        }

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
        List<List<CoreLabel>> sTokens = tokenizer
                .parse(text, newlineIsSentenceBreak, tokenizeOnlyOnSpace, ssplitOnlyOnNewLine);
        Utils.addBasicAnnotations(annotation, sTokens, text);
    }

    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return new HashSet<>(Arrays.asList(
                CoreAnnotations.TextAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.CharacterOffsetBeginAnnotation.class,
                CoreAnnotations.CharacterOffsetEndAnnotation.class,
                CoreAnnotations.BeforeAnnotation.class,
                CoreAnnotations.AfterAnnotation.class,
                CoreAnnotations.TokenBeginAnnotation.class,
                CoreAnnotations.TokenEndAnnotation.class,
                CoreAnnotations.PositionAnnotation.class,
                CoreAnnotations.IndexAnnotation.class,
                CoreAnnotations.OriginalTextAnnotation.class,
                CoreAnnotations.ValueAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class,
                CoreAnnotations.SentenceIndexAnnotation.class
        ));
    }

    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.emptySet();
    }

}
