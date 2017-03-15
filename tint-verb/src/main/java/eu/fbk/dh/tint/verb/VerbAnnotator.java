package eu.fbk.dh.tint.verb;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by alessio on 24/08/16.
 */

public class VerbAnnotator implements Annotator {

    // todo: try to deal with verb phrases ("cercare di fare", "provare a fare", etc.)

    static Set<String> noWords = new HashSet<>();
    private boolean preceededByNot;

    private static final boolean DEFAULT_USE_PREFIX = true;
    private static final String DEFAULT_SKIP_TAGS = "B";
    private static final String DEFAULT_VERB_TAGS = "V";

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbAnnotator.class);

    private boolean usePrefix;
    private List<String> skipTags;
    private List<String> verbTags;

    public VerbAnnotator(String annotatorName, Properties prop) {
        usePrefix = PropertiesUtils.getBoolean(prop.getProperty(annotatorName + ".use_prefix"), DEFAULT_USE_PREFIX);
        String skipTagsText = prop.getProperty(annotatorName + ".skip_tags", DEFAULT_SKIP_TAGS);
        String verbTagsText = prop.getProperty(annotatorName + ".verb_tags", DEFAULT_VERB_TAGS);

        // todo: add property for transitive verbs file list
        // todo: load this part once

        skipTags = new ArrayList<>();
        verbTags = new ArrayList<>();

        String[] sParts = skipTagsText.split("\\s*,\\s*");
        for (String sPart : sParts) {
            skipTags.add(sPart);
        }

        String[] vParts = verbTagsText.split("\\s*,\\s*");
        for (String vPart : vParts) {
            verbTags.add(vPart);
        }

    }

    @Override public void annotate(Annotation annotation) {
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

                List<CoreLabel> lastVerb = new ArrayList<>();

                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                boolean followedByExMark = tokens.get(tokens.size() - 1).word().equals("!");
                boolean preceededByNot = false;

                List<VerbMultiToken> verbs = new ArrayList<>();

                for (int i = 0; i < tokens.size(); i++) {
                    CoreLabel token = tokens.get(i);

                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String form = token.word().toLowerCase();
                    if (noWords.contains(form)) {
                        preceededByNot = true;
                    }

                    if (isSatisfied(pos, verbTags)) {
                        lastVerb.add(token);
                        continue;
                    }
                    if (isSatisfied(pos, skipTags)) {
                        continue;
                    }

                    if (lastVerb.size() > 0) {
                        addVerbs(lastVerb, verbs);
                        lastVerb = new ArrayList<>();
                    }
                }

                if (lastVerb.size() > 0) {
                    addVerbs(lastVerb, verbs);
                }

                sentence.set(VerbAnnotations.VerbsAnnotation.class, verbs);
            }
        }
    }

    private void addVerbs(List<CoreLabel> lastVerb, List<VerbMultiToken> verbs) {
        VerbMultiToken multiToken = new VerbMultiToken();
        for (CoreLabel verb : lastVerb) {
//            System.out.println(verb + " " + verb.get(CoreAnnotations.PartOfSpeechAnnotation.class));
            multiToken.addToken(verb);
        }
        verbs.add(multiToken);
//        System.out.println();
    }

    private boolean isSatisfied(String pos, List<String> tags) {
        boolean ret = false;
        pos = pos.toLowerCase();

        if (usePrefix) {
            for (String tag : tags) {
                tag = tag.toLowerCase();
                if (pos.startsWith(tag)) {
                    ret = true;
                }
            }
        } else {
            for (String tag : tags) {
                tag = tag.toLowerCase();
                if (pos.equals(tag)) {
                    ret = true;
                }
            }
        }

        return ret;
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(VerbAnnotations.VerbsAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.LemmaAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));
    }
}
