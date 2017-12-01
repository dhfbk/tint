package eu.fbk.dh.tint.digimorph.annotator;

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
 * Created by giovannimoretti on 19/05/16.
 *
 * @version 0.42a
 */
public class DigiLemmaAnnotator implements Annotator {

    //private Multimap<String,String> pos_morpho_mapping = ArrayListMultimap.create();
    private static Map<String, String> pos_morpho_mapping = new HashMap<>();
    private static Map<String, String> guessMap = new HashMap<>();
    private static boolean DEFAULT_USE_GUESSER = true;
    private static boolean DEFAULT_FEATURES = true;

    private boolean useGuesser, extractFeatures;
    private GuessModel guesser;

    static private final String auxiliary = "VA";
    static private final String verb = "V";
    static private final String pNoun = "SP";
    static private final Set<String> betweenAuxAndVerb = new HashSet<>(Arrays.asList("B", "BN"));

    static {
        pos_morpho_mapping.put("A", "+adj");
        pos_morpho_mapping.put("AP", "+adj");
        pos_morpho_mapping.put("B", "+adv");
        pos_morpho_mapping.put("BN", "+adv");
        pos_morpho_mapping.put("C", "+conj");
        pos_morpho_mapping.put("CC", "+conj");
        pos_morpho_mapping.put("CS", "+conj");
        pos_morpho_mapping.put("DD", "+adj");
        pos_morpho_mapping.put("DE", "+adj");
        pos_morpho_mapping.put("DI", "+adj");
        pos_morpho_mapping.put("DQ", "+adj");
        pos_morpho_mapping.put("DR", "+adj");
        pos_morpho_mapping.put("DT", "+adj");
        pos_morpho_mapping.put("E", "+prep");
        pos_morpho_mapping.put("E+RD", "+prep");
        pos_morpho_mapping.put("I", "+inter");
        pos_morpho_mapping.put("N", "+adj");
        pos_morpho_mapping.put("NO", "+adj");
        pos_morpho_mapping.put("PC", "+pron");
        pos_morpho_mapping.put("PD", "+pron");
        pos_morpho_mapping.put("PE", "+pron");
        pos_morpho_mapping.put("PI", "+pron");
        pos_morpho_mapping.put("PP", "+pron");
        pos_morpho_mapping.put("PQ", "+pron");
        pos_morpho_mapping.put("PR", "+pron");
        pos_morpho_mapping.put("RD", "+art");
        pos_morpho_mapping.put("RI", "+art");
        pos_morpho_mapping.put("S", "+n+");
        pos_morpho_mapping.put("SP", "+n+");
        pos_morpho_mapping.put("T", "+adj");
        pos_morpho_mapping.put("V", "+v+");
        pos_morpho_mapping.put("VA", "+v+");
        pos_morpho_mapping.put("VA", "+v+");
        pos_morpho_mapping.put("VM", "+v+");
        pos_morpho_mapping.put("VM", "+v+");
        pos_morpho_mapping.put("V+PC", "+v+");
        guessMap.put("A", "ADJ");
        guessMap.put("S", "NOUN");
        guessMap.put("V", "VERB");
        guessMap.put("B", "ADV");
    }

    public DigiLemmaAnnotator(String annotatorName, Properties prop) {
        useGuesser = PropertiesUtils.getBoolean(prop.getProperty(annotatorName + ".use_guesser"), DEFAULT_USE_GUESSER);
        extractFeatures = PropertiesUtils.getBoolean(prop.getProperty(annotatorName + ".extract_features"), DEFAULT_FEATURES);

        if (useGuesser) {
            guesser = GuessModelInstance.getInstance().getModel();
        }
    }

    public void annotate(Annotation annotation) {
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

                String last_valuable_genre = "";
                Boolean valid_aux = false;

                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel token : tokens) {

                    String[] morph_fatures = token.get(DigiMorphAnnotations.MorphoAnnotation.class).split("\\s+");
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    boolean isGuessable = guessMap.containsKey(pos);

                    token.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, true);
                    token.set(CoreAnnotations.LemmaAnnotation.class, morph_fatures[0]);
                    token.set(DigiMorphAnnotations.SelectedMorphoAnnotation.class, "");

                    if (!pos.equals(verb)) {
                        if (pos.equals(auxiliary) || (valid_aux && betweenAuxAndVerb.contains(pos))) {
                            valid_aux = true;
                        } else {
                            valid_aux = false;
                        }
                    }

                    if (!pos.equals(pNoun)) {

                        if (morph_fatures.length > 1) {

                            // One possible candidate
                            if (morph_fatures.length == 2) {

                                String finalMorpho = morph_fatures[1];

                                if (finalMorpho.contains("+art") || finalMorpho.equals("+adj")) {
                                    if (finalMorpho.contains("+m+")) {
                                        last_valuable_genre = "m";
                                    } else {
                                        last_valuable_genre = "f";
                                    }
                                }

                                String featMapped = pos_morpho_mapping.get(pos);
                                boolean shouldBeGuessed = featMapped == null || !finalMorpho.contains(featMapped);

                                if (isGuessable && useGuesser && shouldBeGuessed) {
                                    GuessModel.Token guess = guesser.guess(token.word(), guessMap.get(pos));
                                    token.set(CoreAnnotations.LemmaAnnotation.class, guess.lemma);
                                } else {
                                    token.set(CoreAnnotations.LemmaAnnotation.class, finalMorpho.split("\\+")[0].split("~")[0]);
                                    token.set(DigiMorphAnnotations.SelectedMorphoAnnotation.class, finalMorpho);
                                    if (!shouldBeGuessed) {
                                        token.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, false);
                                    }
                                }
                            }

                            // More candidates
                            else {
//                                Set<String> lemmas = new HashSet<String>();
//                                for (int i = 1; i < morph_fatures.length; i++) {
//                                    lemmas.add(morph_fatures[i].split("\\+")[0].split("~")[0]);
//                                }
//                                if (lemmas.size() > 1) {
                                    // woking with multiple features element

                                    String featMapped = pos_morpho_mapping.get(pos);

                                    String possibleCandidate = "";
                                    String firstCandidate = "";

                                    if (featMapped != null) {
                                        for (String feature : morph_fatures) {
                                            if (feature.contains(featMapped)) {
                                                if (firstCandidate.length() == 0) {
                                                    firstCandidate = feature;
                                                }

                                                if (featMapped.equals("+art") || featMapped.equals("+adj")) {
                                                    if (feature.contains("+m+")) {
                                                        last_valuable_genre = "m";
                                                    } else if (feature.contains("+f+")) {
                                                        last_valuable_genre = "f";
                                                    }
                                                }

                                                if (last_valuable_genre.equals("m") && feature.contains("+m+")) {
                                                    possibleCandidate = feature;
                                                } else if (last_valuable_genre.equals("f") && feature.contains("+f+")) {
                                                    possibleCandidate = feature;
                                                }

                                                if (valid_aux && feature.contains("+part+")) {
                                                    possibleCandidate = feature;
                                                    valid_aux = false;
                                                }

                                            }
                                        }

                                        boolean guessed = false;

                                        String chosenLemma;
                                        String chosenMorpho;

                                        if (possibleCandidate.length() > 0) {
                                            chosenMorpho = possibleCandidate;
                                            chosenLemma = possibleCandidate.split("\\+")[0].split("~")[0];
                                        }
                                        else {
                                            if (firstCandidate.length() > 0) {
                                                chosenMorpho = firstCandidate;
                                                chosenLemma = firstCandidate.split("\\+")[0].split("~")[0];
                                            }
                                            else {
                                                guessed = true;
                                                chosenLemma = token.word();
                                                chosenMorpho = "";
                                            }
                                        }

                                        token.set(CoreAnnotations.LemmaAnnotation.class, chosenLemma);
                                        token.set(DigiMorphAnnotations.SelectedMorphoAnnotation.class, chosenMorpho);
                                        token.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, guessed);
                                    }

//                                } else {
//                                    token.set(CoreAnnotations.LemmaAnnotation.class,
//                                            morph_fatures[1].split("\\+")[0].split("~")[0]);
//                                    token.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, false);
//                                }
                            }
                        }
                    }

                    Boolean stillGuessed = token.get(DigiMorphAnnotations.GuessedLemmaAnnotation.class);
                    if (isGuessable && stillGuessed && useGuesser) {
                        GuessModel.Token guess = guesser.guess(token.word(), guessMap.get(pos));
                        token.set(CoreAnnotations.LemmaAnnotation.class, guess.lemma);
                    }
                }
            }
        }

    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(CoreAnnotations.LemmaAnnotation.class);
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                DigiMorphAnnotations.MorphoAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));
    }
}
