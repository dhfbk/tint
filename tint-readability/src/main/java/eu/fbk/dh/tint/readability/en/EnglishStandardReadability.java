package eu.fbk.dh.tint.readability.en;

import edu.stanford.nlp.pipeline.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 21/09/16.
 */

public class EnglishStandardReadability extends EnglishReadability {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnglishStandardReadability.class);
    private static final Pattern startsWithLetter = Pattern.compile("^[a-zA-Z].*");
    private static final Set<String> immutablePos = new HashSet<>();

    static {
        immutablePos.add("POS");
        immutablePos.add("CC");
        immutablePos.add("CD");
        immutablePos.add("PDT");
        immutablePos.add("TO");
        immutablePos.add("IN");
    }

    @Override protected String getGenericPos(String pos) {
        if (immutablePos.contains(pos)) {
            return pos;
        }
        if (pos.equals("SYM")) {
            return "X";
        }
        if (pos.equals("MD")) {
            return "V";
        }

        Matcher matcher = startsWithLetter.matcher(pos);
        if (matcher.find()) {
            return super.getGenericPos(pos);
        }

        return "X";
    }

    public EnglishStandardReadability(Properties globalProperties, Properties localProperties, Annotation annotation) {
        super(globalProperties, localProperties, annotation);

        contentPosList.add("N");
        contentPosList.add("J");
        contentPosList.add("V");
        contentPosList.add("R");

        simplePosList.add("N");
        simplePosList.add("V");

        nonWordPosList.add("X");

        genericPosDescription.put("J", "Adjective");
        genericPosDescription.put("CC", "Conjunction");
        genericPosDescription.put("CD", "Number");
        genericPosDescription.put("D", "Determiner");
        genericPosDescription.put("X", "Punctuation");
        genericPosDescription.put("F", "Foreign word");
        genericPosDescription.put("IN", "Subordinating (prep. or conj.)");
        genericPosDescription.put("L", "List item marker");
        genericPosDescription.put("PDT", "Pre-determiner");
        genericPosDescription.put("POS", "Possessive");
        genericPosDescription.put("P", "Pronoun");
        genericPosDescription.put("R", "Adverb");
        genericPosDescription.put("N", "Noun");
        genericPosDescription.put("TO", "To");
        genericPosDescription.put("U", "Interjection");
        genericPosDescription.put("V", "Verb");
        genericPosDescription.put("W", "Wh-stuff");

    }

}
