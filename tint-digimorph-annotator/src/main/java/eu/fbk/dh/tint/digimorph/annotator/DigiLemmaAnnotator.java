package eu.fbk.dh.tint.digimorph.annotator;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.fcw.utils.ConllToken;
import eu.fbk.utils.core.PropertiesUtils;
import eu.fbk.utils.corenlp.CustomAnnotations;

import java.util.*;

/**
 * Created by giovannimoretti on 19/05/16.
 *
 * @version 0.42a
 */
public class DigiLemmaAnnotator implements Annotator {

    private static Map<String, String> pos_morpho_mapping = new HashMap<>();
    private static Map<String, String> guessMap = new HashMap<>();
    private static boolean DEFAULT_USE_GUESSER = true;
    private static boolean DEFAULT_FEATURES = true;

    private boolean useGuesser, extractFeatures;
    private GuessModel guesser;
    private String guessModel;

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
        guessModel = prop.getProperty(annotatorName + ".guess_model");

        //todo: the model is unique
        if (useGuesser || extractFeatures) {
            guesser = GuessModelInstance.getInstance(guessModel).getModel();
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

                    boolean chosenGuess = true;
                    String chosenLemma;
                    if (morph_fatures.length > 0) {
                        chosenLemma = morph_fatures[0];
                    } else {
                        chosenLemma = token.originalText();
                    }
                    String chosenMorpho = "";
                    String chosenFeaturesString = "";
                    SortedSetMultimap<String, String> chosenFeatures = TreeMultimap.create();

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
                                    chosenLemma = guess.lemma;
                                } else {
                                    chosenLemma = finalMorpho.split("\\+")[0].split("~")[0];
                                    chosenMorpho = finalMorpho;
                                    if (!shouldBeGuessed) {
                                        chosenGuess = false;
                                    }
                                }
                            }

                            // More candidates
                            else {
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

                                    chosenGuess = false;

                                    if (possibleCandidate.length() > 0) {
                                        chosenMorpho = possibleCandidate;
                                        chosenLemma = possibleCandidate.split("\\+")[0].split("~")[0];
                                    } else {
                                        if (firstCandidate.length() > 0) {
                                            chosenMorpho = firstCandidate;
                                            chosenLemma = firstCandidate.split("\\+")[0].split("~")[0];
                                        } else {
                                            chosenGuess = true;
                                            chosenLemma = token.word();
                                            chosenMorpho = "";
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isGuessable && chosenGuess && useGuesser) {
                        GuessModel.Token guess = guesser.guess(token.word(), guessMap.get(pos));
                        chosenFeaturesString = guess.feats;
                        chosenFeatures = ConllToken.featureStringToAnnotation(guess.feats);
                        chosenLemma = guess.lemma;
                    }

                    if (!chosenGuess) {

                        String useMorpho = chosenMorpho;
                        String usePos = pos;

                        // todo: we can do it better
                        if (pos.startsWith("V+")) {
                            usePos = "V";
                            try {
                                useMorpho = chosenMorpho.split("/")[0];
                                useMorpho = useMorpho.split("~")[1];
                            } catch (Exception e) {
                                // ignored
                            }
                        }

                        chosenFeaturesString = guesser.getMorphoFeats(useMorpho, usePos);
                        if (chosenFeaturesString != null) {
                            chosenFeatures = ConllToken.featureStringToAnnotation(chosenFeaturesString);
                        }
                    }

                    if (chosenLemma.equals("[PUNCT]")) {
                        chosenLemma = token.originalText();
                    }

                    token.set(CoreAnnotations.LemmaAnnotation.class, chosenLemma);
                    token.set(DigiMorphAnnotations.SelectedMorphoAnnotation.class, chosenMorpho);
                    token.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, chosenGuess);
                    if (extractFeatures) {
                        token.set(CoreAnnotations.FeaturesAnnotation.class, chosenFeaturesString);
                        Map<String, Collection<String>> chosenFeaturesMap = new HashMap<>(chosenFeatures.asMap());
                        token.set(CustomAnnotations.FeaturesAnnotation.class, chosenFeaturesMap);
                        HashMap<String, String> conlluFeats = new HashMap<>();
                        for (String key : chosenFeatures.keySet()) {
                            conlluFeats.put(key, String.join(",", chosenFeatures.get(key)));
                        }
                        token.set(CoreAnnotations.CoNLLUFeats.class, conlluFeats);
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
