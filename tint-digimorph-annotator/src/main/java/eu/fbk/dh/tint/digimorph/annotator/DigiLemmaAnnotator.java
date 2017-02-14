package eu.fbk.dh.tint.digimorph.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/**
 * Created by giovannimoretti on 19/05/16.
 *
 * @version 0.42a
 */
public class DigiLemmaAnnotator implements Annotator {

    //private Multimap<String,String> pos_morpho_mapping = ArrayListMultimap.create();
    private Map<String, String> pos_morpho_mapping = new HashMap<String, String>();

    private final String auxiliary = "VA";

    private final Set<String> betweenAuxAndVerb = new HashSet<>(Arrays.asList("B", "BN"));

    public DigiLemmaAnnotator(String annotatorName, Properties prop) {







        /*pos_morpho_mapping.put("A", "+ADJ");
        pos_morpho_mapping.put("AP", "+DET");
        pos_morpho_mapping.put("B", "+ADV");
        pos_morpho_mapping.put("BN", "+ADV");
        pos_morpho_mapping.put("C", "+CON");
        pos_morpho_mapping.put("CC", "+CON");
        pos_morpho_mapping.put("CS", "+CON");
        pos_morpho_mapping.put("DD", "+DET");
        pos_morpho_mapping.put("DE", "+DET");
        pos_morpho_mapping.put("DI", "+DET");
        pos_morpho_mapping.put("DQ", "+DET");
        pos_morpho_mapping.put("DR", "+DET");
        pos_morpho_mapping.put("DT", "+DET");
        pos_morpho_mapping.put("E", "+PRE");
        pos_morpho_mapping.put("E+RD", "+ART");
        pos_morpho_mapping.put("I", "+INT");
        pos_morpho_mapping.put("N", "+PRO");
        pos_morpho_mapping.put("NO", "+ADJ");
        pos_morpho_mapping.put("PC", "+PRO");
        pos_morpho_mapping.put("PD", "+PRO");
        pos_morpho_mapping.put("PE", "+PRO");
        pos_morpho_mapping.put("PI", "+PRO");
        pos_morpho_mapping.put("PP", "+PRO");
        pos_morpho_mapping.put("PQ", "+PRO");
        pos_morpho_mapping.put("PR", "+PRO");
        pos_morpho_mapping.put("RD", "+ART");
        pos_morpho_mapping.put("RI", "+ART");
        pos_morpho_mapping.put("S", "+NOUN");
        pos_morpho_mapping.put("SP", "+NPR");
        pos_morpho_mapping.put("T", "+DET");
        pos_morpho_mapping.put("V", "+VER");
        pos_morpho_mapping.put("VA", "+VER");
        pos_morpho_mapping.put("VA", "+AUX");
        pos_morpho_mapping.put("VM", "+MOD");
        pos_morpho_mapping.put("VM", "+VER");
        pos_morpho_mapping.put("V+PC", "+VER");
        */

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
    }

    public void annotate(Annotation annotation) {
        if (annotation.containsKey(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                String last_valuable_genre = "";
                Boolean valid_aux = false;
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    String[] morph_fatures = c.get(DigiMorphAnnotations.MorphoAnnotation.class).split(" ");
                    String pos = c.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    c.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, true);
                    c.set(CoreAnnotations.LemmaAnnotation.class, morph_fatures[0]);
                    if (!pos.equals("V")) {
                        if (pos.equals(auxiliary) || (valid_aux && betweenAuxAndVerb.contains(pos))) {
                            valid_aux = true;
                        } else {
                            valid_aux = false;
                        }
                    }

                    if (!pos.equals("SP")) {

                        if (morph_fatures.length > 1) {
                            if (morph_fatures.length == 2) {

                                if (morph_fatures[1].contains("+art") && morph_fatures[1].contains("+m+")) {
                                    last_valuable_genre = "m";
                                } else {
                                    last_valuable_genre = "f";
                                }

                                c.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, false);
                                c.set(CoreAnnotations.LemmaAnnotation.class,
                                        morph_fatures[1].split("\\+")[0].split("~")[0]);
                            } else {

                                Set<String> lemmas = new HashSet<String>();
                                for (int i = 1; i < morph_fatures.length; i++) {
                                    lemmas.add(morph_fatures[i].split("\\+")[0].split("~")[0]);
                                }
                                if (lemmas.size() > 1) {
                                    //woking with multiple features element
                                    String featMapped = pos_morpho_mapping.get(pos);

                                    String possible_candidate = "";
                                    String firstCandidate = "";
                                    if (featMapped != null) {
                                        for (String feature : morph_fatures) {
                                            if (feature.contains(featMapped)) {
                                                if (firstCandidate.length() == 0) {
                                                    firstCandidate = feature.split("\\+")[0].split("~")[0];
                                                }

                                                if (featMapped.equals("+art") && feature.contains("+m+")) {
                                                    last_valuable_genre = "m";
                                                } else if (featMapped.equals("+art") && feature.contains("+f+")) {
                                                    last_valuable_genre = "f";
                                                }

                                                if (last_valuable_genre.equals("m") && feature.contains("+m+")) {
                                                    possible_candidate = feature.split("\\+")[0].split("~")[0];
                                                } else if (last_valuable_genre.equals("m") && feature.contains("+f+")) {
                                                    possible_candidate = feature.split("\\+")[0].split("~")[0];
                                                }

                                                if (valid_aux && feature.contains("+part+")) {
                                                    possible_candidate = feature.split("\\+")[0].split("~")[0];
                                                    valid_aux = false;
                                                }

                                            }
                                        }
                                        boolean guessed = false;
                                        if (firstCandidate.length() == 0) {
                                            firstCandidate = c.word();
                                            guessed = true;
                                        }
                                        c.set(CoreAnnotations.LemmaAnnotation.class,
                                                possible_candidate.length() > 0 ? possible_candidate : firstCandidate);

//                                        if (possible_candidate.length() > 0){
                                        c.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, guessed);
//                                        }
                                    }

                                } else {
                                    c.set(CoreAnnotations.LemmaAnnotation.class,
                                            morph_fatures[1].split("\\+")[0].split("~")[0]);
                                    c.set(DigiMorphAnnotations.GuessedLemmaAnnotation.class, false);
                                }
                            }
                        }
                    }
                }
            }
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
                DigiMorphAnnotations.MorphoAnnotation.class,
                CoreAnnotations.LemmaAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));
    }
}
