package eu.fbk.dh.tint.verb;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.PropertiesUtils;

import java.util.*;

/**
 * Created by alessio on 24/08/16.
 */

public class VerbAnnotator implements Annotator {

    // todo: try to deal with verb phrases ("cercare di fare", "provare a fare", etc.)

//    static Set<String> noWords = new HashSet<>();
//    private boolean preceededByNot;

    private static final boolean DEFAULT_USE_PREFIX = true;
    private static final boolean DEFAULT_MODAL_IS_PREFIX = true;
    private static final boolean DEFAULT_AUX_IS_PREFIX = true;
    private static final String DEFAULT_SKIP_TAGS = "B";
    private static final String DEFAULT_VERB_TAGS = "V";
    private static final String DEFAULT_AUX_TAGS = "VA";
    private static final String DEFAULT_MODAL_TAGS = "VM";

    private boolean usePrefix, modalUsePrefix, auxUsePrefix;
    private List<String> skipTags, verbTags, modalTags, auxTags;
    private VerbModel model;

    public VerbAnnotator(String annotatorName, Properties prop) {
        usePrefix = PropertiesUtils.getBoolean(prop.getProperty(annotatorName + ".use_prefix"), DEFAULT_USE_PREFIX);
        auxUsePrefix = PropertiesUtils.getBoolean(prop.getProperty(annotatorName + ".aux_is_prefix"), DEFAULT_AUX_IS_PREFIX);
        modalUsePrefix = PropertiesUtils.getBoolean(prop.getProperty(annotatorName + ".modal_is_prefix"), DEFAULT_MODAL_IS_PREFIX);
        String skipTagsText = prop.getProperty(annotatorName + ".skip_tags", DEFAULT_SKIP_TAGS);
        String verbTagsText = prop.getProperty(annotatorName + ".verb_tags", DEFAULT_VERB_TAGS);
        String auxTagsText = prop.getProperty(annotatorName + ".aux_tags", DEFAULT_AUX_TAGS);
        String modalTagsText = prop.getProperty(annotatorName + ".modal_tags", DEFAULT_MODAL_TAGS);

        // todo: add custom filename
        model = VerbModel.getInstance();

        skipTags = new ArrayList<>();
        verbTags = new ArrayList<>();
        modalTags = new ArrayList<>();
        auxTags = new ArrayList<>();

        splitParts(skipTagsText, skipTags);
        splitParts(verbTagsText, verbTags);
        splitParts(modalTagsText, modalTags);
        splitParts(auxTagsText, auxTags);
    }

    static private void splitParts(String text, List<String> tags) {
        String[] sParts = text.split("\\s*,\\s*");
        for (String sPart : sParts) {
            tags.add(sPart);
        }
    }

    @Override public void annotate(Annotation annotation) {
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

                List<CoreLabel> lastVerb = new ArrayList<>();

                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                boolean followedByExMark = tokens.get(tokens.size() - 1).word().equals("!");
//                boolean preceededByNot = false;

                List<VerbMultiToken> verbs = new ArrayList<>();

                for (int i = 0; i < tokens.size(); i++) {
                    CoreLabel token = tokens.get(i);

                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                    System.out.println(token);
//                    System.out.println(pos);
//                    System.out.println();
//                    String form = token.word().toLowerCase();
//                    if (noWords.contains(form)) {
//                        preceededByNot = true;
//                    }

                    if (isSatisfied(pos, verbTags, usePrefix) || isSatisfied(pos, modalTags, modalUsePrefix)) {
                        lastVerb.add(token);
                    }
                    if (isSatisfied(pos, skipTags, usePrefix)) {
                        continue;
                    }
                    if (isSatisfied(pos, auxTags, auxUsePrefix)) {
                        continue;
                    }

                    if (lastVerb.size() > 0) {
                        addVerbs(lastVerb, verbs, followedByExMark);
                        lastVerb = new ArrayList<>();
                    }
                }

                if (lastVerb.size() > 0) {
                    addVerbs(lastVerb, verbs, followedByExMark);
                }

                sentence.set(VerbAnnotations.VerbsAnnotation.class, verbs);
            }
        }
    }

    private void addVerbs(List<CoreLabel> lastVerb, List<VerbMultiToken> verbs, boolean followedByExMark) {
        VerbMultiToken multiToken = new VerbMultiToken();
        for (int i = 0; i < lastVerb.size(); i++) {
            CoreLabel verb = lastVerb.get(i);
            boolean last = (i == lastVerb.size() - 1);
            multiToken.addToken(model, verb, last);
        }
        verbs.add(multiToken);
    }

    private static boolean isSatisfied(String pos, List<String> tags, boolean usePrefix) {
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
