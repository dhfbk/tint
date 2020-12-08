package eu.fbk.dh.tint.splitter;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.IntPair;
import edu.stanford.nlp.util.Pair;
import eu.fbk.utils.corenlp.CustomAnnotations;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class SplitterAnnotator implements Annotator {

    public Map<String, Pair<String, String>> preps = new HashMap<>();
    public static Set<String> clitics = new HashSet<>();

    static {
        clitics.add("li");
        clitics.add("lo");
        clitics.add("la");
        clitics.add("le");
        clitics.add("ci");
        clitics.add("vi");
        clitics.add("ti");
        clitics.add("mi");
        clitics.add("si");
        clitics.add("ne");
        clitics.add("gli");
        clitics.add("ce");
        clitics.add("me");
        clitics.add("te");
        clitics.add("ve");
        clitics.add("se");
        clitics.add("glie");
    }

    public SplitterAnnotator(String annotatorName, Properties prop) {
        try {
            InputStream stream = this.getClass().getResourceAsStream("/preps.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }
                preps.put(parts[0], new Pair<>(parts[1], parts[2]));
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void annotate(Annotation annotation) {
        List<CoreLabel> finalDocumentTokens = new ArrayList<CoreLabel>();
        int sentNum = 0;

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

            List<CoreLabel> newSentenceTokens = new ArrayList<>();
            int sentenceIndex = 1;

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String[] parts = pos.split("\\+");
                if (parts.length > 1) {

                    boolean isCoarse = false;
                    String[] uparts = new String[0];
                    if (token.containsKey(CoreAnnotations.CoarseTagAnnotation.class)) {
                        uparts = token.get(CoreAnnotations.CoarseTagAnnotation.class).split("\\+");
                        isCoarse = true;
                    }

                    // Preposizione articolata
                    if (pos.equals("E+RD")) {
                        Pair<String, String> prepsPair = preps.get(token.originalText().toLowerCase());
                        boolean isFirst = true;
                        List<Object> asList = prepsPair.asList();
                        for (int i = 0; i < asList.size(); i++) {
                            Object prepsPairObj = asList.get(i);
                            String word = (String) prepsPairObj;

                            CoreLabel newToken = new CoreLabel(token);

                            newToken.setWord(word);
                            newToken.setValue(word);
                            newToken.setIndex(sentenceIndex);
                            newToken.setSentIndex(sentNum);

                            newToken.set(CoreAnnotations.PartOfSpeechAnnotation.class, parts[i]);
                            if (isCoarse) {
                                newToken.set(CoreAnnotations.CoarseTagAnnotation.class, uparts[i]);
                                newToken.set(CustomAnnotations.UPosAnnotation.class, uparts[i]);
                            }

                            newToken.set(CoreAnnotations.MWTTokenTextAnnotation.class, token.word());

                            // set that this is a multi-word-token
                            newToken.setIsMWT(true);

                            // set that this is the first word derived from a multi-word-token
                            // e.g. when "des" is split into "de" and "les", "de" would be true
                            if (isFirst) {
                                newToken.setIsMWTFirst(true);
                                newToken.set(CoreAnnotations.CoNLLUTokenSpanAnnotation.class,
                                        new IntPair(token.index(), token.index() + parts.length - 1));
                                isFirst = false;
                            } else {
                                newToken.setIsMWTFirst(false);
                            }

                            // add finalized token
                            newSentenceTokens.add(newToken);
                            finalDocumentTokens.add(newToken);
                            sentenceIndex++;
                        }
                    }

                    // Verb + clitic
                    else {

                        String text = token.originalText().toLowerCase();
                        String textparts[] = new String[parts.length];

                        // get all POS parts but first, in reverse order
                        for (int i = parts.length - 1; i > 0; i--) {
                            for (String clitic : clitics) {
                                if (text.endsWith(clitic)) {
                                    textparts[i] = clitic;
                                    text = text.substring(0, text.length() - clitic.length());
                                    break;
                                }
                            }
                        }
                        textparts[0] = text;

                        boolean isFirst = true;
                        for (int i = 0, textpartsLength = textparts.length; i < textpartsLength; i++) {
                            String word = textparts[i];
                            CoreLabel newToken = new CoreLabel(token);

                            newToken.setWord(word);
                            newToken.setValue(word);
                            newToken.setIndex(sentenceIndex);
                            newToken.setSentIndex(sentNum);

                            newToken.set(CoreAnnotations.PartOfSpeechAnnotation.class, parts[i]);
                            if (isCoarse) {
                                newToken.set(CoreAnnotations.CoarseTagAnnotation.class, uparts[i]);
                                newToken.set(CustomAnnotations.UPosAnnotation.class, uparts[i]);
                            }

                            newToken.set(CoreAnnotations.MWTTokenTextAnnotation.class, token.word());

                            // set that this is a multi-word-token
                            newToken.setIsMWT(true);

                            // set that this is the first word derived from a multi-word-token
                            // e.g. when "des" is split into "de" and "les", "de" would be true
                            if (isFirst) {
                                newToken.setIsMWTFirst(true);
                                newToken.set(CoreAnnotations.CoNLLUTokenSpanAnnotation.class,
                                        new IntPair(token.index(), token.index() + parts.length - 1));
                                isFirst = false;
                            } else {
                                newToken.setIsMWTFirst(false);
                            }

                            // add finalized token
                            newSentenceTokens.add(newToken);
                            finalDocumentTokens.add(newToken);
                            sentenceIndex++;
                        }

                    }
                } else {
                    token.setIndex(sentenceIndex);
                    token.setIsMWT(false);
                    token.setIsMWTFirst(false);
                    newSentenceTokens.add(token);
                    finalDocumentTokens.add(token);
                    sentenceIndex++;
                }
            }

            sentence.set(CoreAnnotations.TokenEndAnnotation.class, finalDocumentTokens.size());
            sentence.set(CoreAnnotations.TokensAnnotation.class, newSentenceTokens);
            sentNum++;
        }
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));
    }
}
