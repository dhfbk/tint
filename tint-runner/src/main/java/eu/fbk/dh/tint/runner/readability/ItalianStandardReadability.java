package eu.fbk.dh.tint.runner.readability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by alessio on 21/09/16.
 */

public class ItalianStandardReadability extends ItalianReadability {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItalianStandardReadability.class);

    public ItalianStandardReadability() {
        super();

        contentPosList.add("S");
        contentPosList.add("A");
        contentPosList.add("V");
        contentPosList.add("B");

        simplePosList.add("S");
        simplePosList.add("V");

        nonWordPosList.add("F");

        genericPosDescription.put("A", "Adjective");
        genericPosDescription.put("B", "Adverb");
        genericPosDescription.put("S", "Noun");
        genericPosDescription.put("E", "Preposition");
        genericPosDescription.put("C", "Conjunction");
        genericPosDescription.put("P", "Pronoun");
        genericPosDescription.put("R", "Determiner");
        genericPosDescription.put("F", "Punctuation");
        genericPosDescription.put("D", "Adj. (det.)");
        genericPosDescription.put("V", "Verb");
        genericPosDescription.put("X", "Other");
        genericPosDescription.put("N", "Number");

    }

}
