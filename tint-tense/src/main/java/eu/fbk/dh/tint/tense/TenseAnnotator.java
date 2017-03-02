package eu.fbk.dh.tint.tense;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.digimorph.annotator.DigiMorphAnnotations;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by alessio on 24/08/16.
 */

public class TenseAnnotator implements Annotator {

    // todo: try to deal with verb phrases ("cercare di fare", "provare a fare", etc.)

    static Set<String> noWords = new HashSet<>();
    static Map<String, String> multiTenses = new HashMap<>();
    private boolean preceededByNot;

    enum Form {ACTIVE, PASSIVE}

    static {
        noWords.add("non");
        noWords.add("né");
        noWords.add("nemmeno");
        noWords.add("neanche");

        multiTenses.put("indicpres", "indpasspross");
        multiTenses.put("indicimperf", "indtrapasspross");
        multiTenses.put("indicpass", "indtrapassrem");
        multiTenses.put("indicfut", "indfutant");
    }

    private class VerbToken {

        private CoreLabel token;
        private List<String> compatibleMorpho;

        public VerbToken(CoreLabel token) {
            this.token = token;
            this.compatibleMorpho = token.get(DigiMorphAnnotations.MorphoCompAnnotation.class);
        }

        public CoreLabel getToken() {
            return token;
        }

        public void setToken(CoreLabel token) {
            this.token = token;
        }

        public List<String> getCompatibleMorpho() {
            return compatibleMorpho;
        }

        public void setCompatibleMorpho(List<String> compatibleMorpho) {
            this.compatibleMorpho = compatibleMorpho;
        }

        private Set<String> getTense() {
            Set<String> ret = new HashSet<>();
            List<String> compatibleMorpho = getCompatibleMorpho();
            for (String m : compatibleMorpho) {
                String[] parts = m.split("\\+");
                if (parts.length > 3) {
                    ret.add(parts[2] + parts[3]);
                }
            }

            // todo: check imp
            if (ret.size() > 1 && ret.contains("imppres")) {
                ret.remove("imppres");
            }

            return ret;
        }

    }

    private class VerbGroup {

        private List<VerbToken> tokens = new ArrayList<>();
        private boolean followedByExMark = false;
        private boolean preceededByNot = false;

        public void addToken(VerbToken token) {
            tokens.add(token);
        }

        public List<VerbToken> getTokens() {
            return tokens;
        }

        public void setTokens(List<VerbToken> tokens) {
            resetTokens();
            for (VerbToken token : tokens) {
                addToken(token);
            }
        }

        private void resetTokens() {
            this.tokens = new ArrayList<>();
        }

        public boolean isFollowedByExMark() {
            return followedByExMark;
        }

        public void setFollowedByExMark(boolean followedByExMark) {
            this.followedByExMark = followedByExMark;
        }

        public boolean isPreceededByNot() {
            return preceededByNot;
        }

        public void setPreceededByNot(boolean preceededByNot) {
            this.preceededByNot = preceededByNot;
        }

        public Set<String> getPatterns() {

            Set<String> ret = new HashSet<>();

            StringBuffer pattern = new StringBuffer();
            for (int i = 0; i < tokens.size(); i++) {
                VerbToken token = tokens.get(i);

                if (i < tokens.size() - 1) {
                    String lemma = token.getToken().lemma();
                    if (lemma.equals("venire")) {
                        lemma = "essere";
                    }
                    pattern.append(lemma).append("+");
                } else {
                    Set<String> tenses = token.getTense();
                    for (String tense : tenses) {
                        ret.add(pattern.toString() + tense);
                    }
                }
            }

            return ret;
        }

        public String getLemma() {
            VerbToken lastToken = tokens.get(tokens.size() - 1);
            return lastToken.getToken().lemma();
        }
    }

    private static final boolean DEFAULT_USE_PREFIX = true;
    private static final String DEFAULT_SKIP_TAGS = "B";
    private static final String DEFAULT_VERB_TAGS = "V";

    private static final Logger LOGGER = LoggerFactory.getLogger(TenseAnnotator.class);

    private boolean usePrefix;
    private List<String> skipTags;
    private List<String> verbTags;
    private Set<String> transitiveVerbs;

    public TenseAnnotator(String annotatorName, Properties prop) {
        String tvFile = prop.getProperty(annotatorName + ".transitive_verbs", null);
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

        // Load transitive verbs
        transitiveVerbs = new HashSet<>();
        InputStream stream = null;
        if (tvFile != null) {
            try {
                stream = new FileInputStream(tvFile);
            } catch (FileNotFoundException e) {
                // continue
            }
        }
        if (stream == null) {
            stream = this.getClass().getResourceAsStream("/transitiveVerbs.txt");
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    transitiveVerbs.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void annotate(Annotation annotation) {
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

                List<CoreLabel> lastVerb = new ArrayList<>();

                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                boolean followedByExMark = tokens.get(tokens.size() - 1).word().equals("!");
                boolean preceededByNot = false;

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
                        for (CoreLabel verb : lastVerb) {
                            System.out.println(verb + " " + verb.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                        }
                        System.out.println();
//                        String tense = analyzeVerb(lastVerb, followedByExMark, preceededByNot);
//                        tokens.get(i - 1).set(TenseAnnotations.TenseAnnotation.class, tense);
                        preceededByNot = false;
                        lastVerb = new ArrayList<>();
                    }
                }

                if (lastVerb.size() > 0) {
                    for (CoreLabel verb : lastVerb) {
                        System.out.println(verb + " " + verb.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                    }
                    System.out.println();
//                    String tense = analyzeVerb(lastVerb, followedByExMark, preceededByNot);
//                    tokens.get(tokens.size() - 1).set(TenseAnnotations.TenseAnnotation.class, tense);
//                    lastVerb = new ArrayList<>();
//                    preceededByNot = false;
                }
            }
        }
    }

    static Set<String> getFromMap(Set<String> needle, Map<String, String> haystack) {
        Set<String> ret = new HashSet<>();
        for (String s : needle) {
            String v = haystack.get(s);
            if (v != null) {
                ret.add(v);
            }
        }
        return ret;
    }

    private String analyzeVerb(List<CoreLabel> verb, boolean followedByExMark, boolean preceededByNot) {

        String lemma = verb.get(verb.size() - 1).get(CoreAnnotations.LemmaAnnotation.class);

        CoreLabel firstToken = verb.get(0);
        String morpho = firstToken.get(DigiMorphAnnotations.MorphoAnnotation.class);
        String firstLemma = firstToken.getString(CoreAnnotations.LemmaAnnotation.class);
        String firstPos = firstToken.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
        List<String> compatibleMorpho = getCompatibleMorpho(morpho, firstLemma);

        if (compatibleMorpho.size() == 0) {
            return null;
        }

        String thisMorpho = compatibleMorpho.get(0);
        String[] parts = thisMorpho.split("\\+");
        if (parts.length <= 3) {
            return null;
        }

        String tense = parts[3];

        if (firstPos.equals("VA") &&
                isTransitive(lemma) &&
                (firstLemma.equals("essere") || firstLemma.equals("venire"))
                ) {
            tense = "pass";
        }

        return tense;

//        VerbGroup verbGroup = new VerbGroup();
//        verbGroup.setFollowedByExMark(followedByExMark);
//        verbGroup.setPreceededByNot(preceededByNot);
//
//        for (CoreLabel token : verb) {
//
//            String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
//            String morpho = token.get(DigiMorphAnnotations.MorphoAnnotation.class);
//
//            // todo: this should be done in DigiMorph
//            List<String> compatibleMorpho = getCompatibleMorpho(morpho, lemma);
//            token.set(DigiMorphAnnotations.MorphoCompAnnotation.class, compatibleMorpho);
//
//            VerbToken verbToken = new VerbToken(token);
//            verbGroup.addToken(verbToken);
//        }
//
//        Set<String> patterns = verbGroup.getPatterns();
//
//        // todo: choose imperative when sentence ends by exclamation mark
//        if (patterns.size() > 1 && patterns.contains("imppres")) {
//            patterns.remove("imppres");
//        }
//
//        String lemma = verbGroup.getLemma();
//        boolean isTransitive = isTransitive(lemma);
//        Form form;
//
//        for (String pattern : patterns) {
//
//            String[] pieces = pattern.split("\\+");
//
//            switch (pattern) {
//            case "stare+essere+partpass":
//                // should always be passive
//                form = Form.PASSIVE;
//
//                System.out.println("Form: " + form);
//                System.out.println("Lemma: " + lemma);
//                System.out.println("Tense: " + verbGroup.getTokens().get(0).getTense());
//                System.out.println();
//                break;
//            case "essere+essere+partpass":
//                // should always be passive
//                form = Form.PASSIVE;
//
//                System.out.println("Form: " + form);
//                System.out.println("Lemma: " + lemma);
//                System.out.println("Tense: " + getFromMap(verbGroup.getTokens().get(0).getTense(), multiTenses));
//                System.out.println();
//                break;
//            case "avere+partpass":
//                form = Form.ACTIVE;
//                System.out.println("Form: " + form);
//                System.out.println("Lemma: " + lemma);
//                System.out.println("Tense: " + getFromMap(verbGroup.getTokens().get(0).getTense(), multiTenses));
//                System.out.println();
//                break;
//            case "essere+partpass":
//                Set<String> tenses = verbGroup.getTokens().get(0).getTense();
//                if (isTransitive(lemma)) {
//                    form = Form.PASSIVE;
//                } else {
//                    form = Form.ACTIVE;
//                    tenses = getFromMap(tenses, multiTenses);
//                }
//
//                System.out.println("Form: " + form);
//                System.out.println("Lemma: " + lemma);
//                System.out.println("Tense: " + tenses);
//                System.out.println();
//                break;
//            default:
//                if (pieces.length == 1) {
//                    System.out.println("ACTIVE");
//                    System.out.println("Lemma: " + lemma);
//                    System.out.println("Tense: " + pattern);
//                    System.out.println();
//                    break;
//                }
//
//                LOGGER.error("Pattern not found: {}", pattern);
//
//                System.out.println("NOT FOUND");
//                System.out.println("Pattern: " + pattern);
//                System.out.println("Lemma: " + lemma);
//                System.out.println("Transitive: " + isTransitive(lemma));
//                System.out.println("IsNot: " + preceededByNot);
//                System.out.println("HasExMark: " + followedByExMark);
//                System.out.println();
//            }
//
//        }
    }

    private boolean isTransitive(String verb) {
        return transitiveVerbs.contains(verb.toLowerCase());
    }

    private List<String> getCompatibleMorpho(String morpho, String lemma) {
        List<String> ret = new ArrayList<>();

        String[] parts = morpho.split("\\s+");
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];

            String[] words = part.split("/");
            if (words.length > 1) {
                part = words[0];
                String[] t = part.split("~");
                if (t.length > 1) {
                    part = t[1];
                }
            }

            String[] properties = part.split("\\+");
            if (properties[0].equalsIgnoreCase(lemma)) {
                ret.add(part);
            }
        }

        return ret;
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
        return Collections.singleton(TenseAnnotations.TenseAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
//                DigiMorphAnnotations.MorphoAnnotation.class,
                CoreAnnotations.LemmaAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));
    }
//    @Override public Set<Requirement> requirementsSatisfied() {
//        return Collections.singleton(TenseAnnotations.DH_TENSE_REQUIREMENT);
//    }
//
//    @Override public Set<Requirement> requires() {
//        return Collections.unmodifiableSet(
//                new ArraySet<Requirement>(DigiMorphAnnotations.DH_MORPHOLOGY_REQUIREMENT, POS_REQUIREMENT));
//    }
}
