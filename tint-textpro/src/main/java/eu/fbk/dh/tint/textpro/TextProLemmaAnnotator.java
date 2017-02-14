package eu.fbk.dh.tint.textpro;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Created by alessio on 06/05/15.
 */

public class TextProLemmaAnnotator implements Annotator {

    static class LemmaProperty {

        private String pos;
        private boolean toLower;
        private String lemma;

        public LemmaProperty(String lemma, String pos, boolean toLower) {
            this.pos = pos;
            this.toLower = toLower;
            this.lemma = lemma;
        }

        public String getLemma() {
            return lemma;
        }

        public void setLemma(String lemma) {
            this.lemma = lemma;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public boolean isToLower() {
            return toLower;
        }

        public void setToLower(boolean toLower) {
            this.toLower = toLower;
        }

        @Override public String toString() {
            return "LemmaProperty{" +
                    "pos='" + pos + '\'' +
                    ", toLower=" + toLower +
                    ", lemma='" + lemma + '\'' +
                    '}';
        }
    }

    FstanRunner runner;

    public TextProLemmaAnnotator(String annotatorName, Properties props) {
        String command = props.getProperty(annotatorName + ".fstan_command");
        String model = props.getProperty(annotatorName + ".fstan_model");
        runner = new FstanRunner(command, model);
    }

    public static LemmaProperty getFstanPos(String lemma, String pos) {
        String type = null;
        boolean toLower = false;

        switch (pos) {
        case "FF":
            type = "punc";
            break;
        case "DD":
            type = "adj";
            break;
        case "PP":
            type = "adj";
            break;
        case "A":
            type = "adj";
            break;
        case "NO":
            type = "adj";
            break;
        case "DE":
            type = "adj";
            break;
        case "PQ":
            type = "pron";
            break;
        case "PR":
            type = "pron";
            break;
        case "B":
            type = "adv";
            break;
        case "B+PC":
            type = "adv";
            break;
        case "E":
            type = "prep";
            break;
        case "DI":
            type = "adj";
            break;
        case "I":
            toLower = true;
            break;
        case "VA+PC":
            type = "v";
            break;
        case "BN":
            type = "adv";
            break;
        case "FS":
            type = "punc";
            break;
        case "DQ":
            type = "adj";
            break;
        case "PC+PC":
            type = "pron";
            break;
        case "N":
            type = "adj";
            break;
        case "DR":
            type = "pron";
            break;
        case "S":
            type = "n";
            break;
        case "T":
            type = "adj";
            break;
        case ".$$.":
            break;
        case "V":
            type = "v";
            break;
        case "E+RD":
            type = "prep";
            break;
        case "VM+PC":
            type = "v";
            break;
        case "X":
            toLower = true;
            break;
        case "SP":
            break;
        case "CC":
            type = "conj";
            break;
        case "SW":
            type = "n";
            break;
        case "V+PC+PC":
            type = "v";
            break;
        case "VA":
            type = "v";
            break;
        case "AP":
            type = "adj";
            break;
        case "V+PC":
            type = "v";
            break;
        case "CS":
            type = "conj";
            break;
        case "RD":
            type = "art";
            break;
        case "PC":
            type = "pron";
            break;
        case "PD":
            type = "pron";
            break;
        case "PE":
            type = "pron";
            break;
        case "RI":
            type = "art";
            break;
        case "VM":
            type = "v";
            break;
        case "PI":
            type = "pron";
            break;
        case "FB":
            type = "punc";
            break;
        case "VM+PC+PC":
            type = "v";
            break;
        case "FC":
            type = "punc";
            break;
        }

        return new LemmaProperty(lemma, type, toLower);
    }

    @Override
    public void annotate(Annotation annotation) {
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {

            ArrayList<LemmaProperty> list = new ArrayList<>();
            ArrayList<String> tokenList = new ArrayList<>();

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel token : tokens) {
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    String originalToken = token.get(CoreAnnotations.TextAnnotation.class);

                    LemmaProperty lemmaProperty = getFstanPos(originalToken, pos);
//                    System.out.println(originalToken);
//                    System.out.println(lemmaProperty);
//                    System.out.println(lemmaProperty.getLemma());

                    list.add(lemmaProperty);
                    tokenList.add(lemmaProperty.getLemma());
                }
            }

            ArrayList<String[]> results = runner.run(tokenList);

            int i = 0;
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel token : tokens) {
                    ArrayList<String[]> res = new ArrayList<>();

                    res.add(results.get(i));
                    LemmaProperty lemmaProperty = list.get(i);

                    ArrayList<String> strings = null;
                    if (lemmaProperty.getPos() != null) {
                        strings = runner.get(lemmaProperty.getLemma(), lemmaProperty.getPos(), res);
                    }
                    String lemma = lemmaProperty.getLemma();
                    if (strings != null) {
                        lemma = strings.get(0);
                    }
                    if (lemmaProperty.isToLower()) {
                        lemma = lemma.toLowerCase();
                    }

                    token.set(CoreAnnotations.LemmaAnnotation.class, lemma);

                    i++;
                }
            }
        } else {
            throw new RuntimeException("unable to find words/tokens in: " + annotation);
        }

    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(CoreAnnotations.LemmaAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));
    }
}
