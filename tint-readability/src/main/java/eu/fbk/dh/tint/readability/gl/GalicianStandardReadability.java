package eu.fbk.dh.tint.readability.gl;

import edu.stanford.nlp.pipeline.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by alessio on 21/09/16.
 */

public class GalicianStandardReadability extends GalicianReadability {

    private static final Logger LOGGER = LoggerFactory.getLogger(GalicianStandardReadability.class);

    public GalicianStandardReadability(Properties globalProperties, Properties localProperties, Annotation annotation) {
        super(globalProperties, localProperties, annotation);

        contentPosList.add("N");
        contentPosList.add("A");
        contentPosList.add("V");
        contentPosList.add("R");

        simplePosList.add("N");
        simplePosList.add("V");

        nonWordPosList.add("F");

        genericPosDescription.put("A", "Adjective");
        genericPosDescription.put("C", "Conjunction");
        genericPosDescription.put("D", "Determiner");
        genericPosDescription.put("F", "Punctuation");
        genericPosDescription.put("I", "Interjection");
        genericPosDescription.put("R", "Adverb");
        genericPosDescription.put("N", "Noun");
        genericPosDescription.put("S", "Preposition");
        genericPosDescription.put("P", "Pronoun");
        genericPosDescription.put("V", "Verb");
        genericPosDescription.put("X", "Other");
        genericPosDescription.put("Z", "Number");
        genericPosDescription.put("W", "Date");

    }

}
