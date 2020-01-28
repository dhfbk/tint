package eu.fbk.dh.tint.readability;

import com.itextpdf.layout.hyphenation.Hyphenation;
import com.itextpdf.layout.hyphenation.Hyphenator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.es.SpanishReadabilityModel;
import eu.fbk.dh.tint.verb.VerbAnnotations;
import eu.fbk.dh.tint.verb.VerbMultiToken;
import eu.fbk.utils.core.FrequencyHashSet;
import eu.fbk.utils.core.PropertiesUtils;
import eu.fbk.utils.gson.JSONExclude;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

/**
 * Created by alessio on 21/09/16.
 */
public abstract class Readability {

    public static Integer DEFAULT_TTR_LIMIT = 100;
    @JSONExclude
    private int ttrLimit;

    private String language = null;
    private int contentWordSize = 0, contentEasyWordSize = 0, wordCount = 0;
    private int docLenWithSpaces = 0, docLenWithoutSpaces = 0, docLenLettersOnly = 0;
    private int goodSentenceCount = 0, sentenceCount = 0, tokenCount = 0;
    private int hyphenCount = 0;
    private int hyphenWordCount = 0;

    private Double ttrValue;
    private Double density;
    private Double deepAvg;
    private Double deepMax;
    private Double propositionsAvg;
    private Double wordsAvg;
    //    private Double coordinateRatio;
    private Double subordinateRatio;

    Map<Integer, Integer> deeps = new HashMap<>();

    protected TreeMap<Integer, DescriptionForm> forms = new TreeMap<>();

    protected Map<String, Double> measures = new HashMap<>();
    protected Map<String, String> labels = new HashMap<>();

    protected Map<String, Double> minYellowValues = new HashMap<>();
    protected Map<String, Double> maxYellowValues = new HashMap<>();
    protected Map<String, Double> minValues = new HashMap<>();
    protected Map<String, Double> maxValues = new HashMap<>();

    public int getHyphenWordCount() {
        return hyphenWordCount;
    }

    @JSONExclude
    protected HashSet<String> contentPosList = new HashSet<>();
    @JSONExclude
    protected HashSet<String> simplePosList = new HashSet<>();
    @JSONExclude
    protected HashSet<String> nonWordPosList = new HashSet<>();

    protected HashMap<String, String> genericPosDescription = new HashMap<>();
    protected HashMap<String, String> posDescription = new HashMap<>();

    @JSONExclude
    boolean useGenericForContent = true;
    @JSONExclude
    boolean useGenericForSimple = true;
    @JSONExclude
    boolean useGenericForWord = true;

    Set<Integer> tooLongSentences = new HashSet<>();
    FrequencyHashSet<String> posStats = new FrequencyHashSet<>();
    FrequencyHashSet<String> genericPosStats = new FrequencyHashSet<>();

    @JSONExclude
    protected Hyphenator hyphenator;
    @JSONExclude
    protected Annotation annotation;

    public Readability(String language, Annotation annotation, Properties localProperties) {
        this.language = language;
        this.annotation = annotation;

        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        docLenWithSpaces = text.length();
        docLenWithoutSpaces = text.replaceAll("\\s+", "").length();
        ttrLimit = PropertiesUtils.getInteger(localProperties.getProperty("ttrLimit"), DEFAULT_TTR_LIMIT);
    }

    public void finalizeReadability() {
        Set<String> ttr = new HashSet<>();

        int i = 0;
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            Boolean isWord = token.get(ReadabilityAnnotations.LiteralWord.class);
            if (isWord == null || !isWord) {
                continue;
            }

            if (ttrLimit > 0 && i >= ttrLimit) {
                break;
            }
            String tokenText = token.originalText().toLowerCase();
            ttr.add(tokenText);
            i++;
        }

        List<Integer> propositions = new ArrayList<>();

        Integer coordinates = 0;
        Integer subordinates = 0;

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        if (language.equals("en")) {
            for (int sentIndex = 0; sentIndex < sentences.size(); sentIndex++) {

                int sentSubordinates = 0;
                int sentCoordinates = 0;
                int auxAnnotations = 0;
                int verbAnnotations = 0;

                CoreMap sentence = sentences.get(sentIndex);
                SemanticGraph semanticGraph = sentence
                        .get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
                try {
                    for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
                        switch (edge.getRelation().getShortName()) {
                            case "aux":
                            case "auxpass":
                                auxAnnotations++;
                                break;
                            case "mark":
                            case "acl:relcl":
//                        case "xcomp":
                                sentSubordinates++;
                                break;
                        }

                    }
                } catch (Exception e) {
                    // continue
                }

                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if (pos.startsWith("V") || pos.startsWith("M")) {
                        verbAnnotations++;
                    }
                }

                verbAnnotations -= auxAnnotations;
                propositions.add(verbAnnotations);
                sentCoordinates = Math.max(0, verbAnnotations - sentSubordinates);

//                System.out.println(verbAnnotations);
//                System.out.println(sentCoordinates);
//                System.out.println(sentSubordinates);

                Integer sentTotal = sentCoordinates + sentSubordinates;

                coordinates += sentCoordinates;
                subordinates += sentSubordinates;

                Double sentSubordinateRatio = 0.0;

                if (sentTotal > 0) {
                    sentSubordinateRatio = (1.0 * sentSubordinates) / sentTotal;
                }

                sentence.set(ReadabilityAnnotations.SubordinateRatioAnnotation.class, sentSubordinateRatio);
            }
        }

        for (int sentIndex = 0; sentIndex < sentences.size(); sentIndex++) {
            CoreMap sentence = sentences.get(sentIndex);

            SemanticGraph semanticGraph = sentence
                    .get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
            int depth = 0;
            if (semanticGraph == null) {
                continue;
            }

            for (IndexedWord indexedWord : semanticGraph.getLeafVertices()) {
                try {
                    depth = Math.max(depth, semanticGraph.getPathToRoot(indexedWord).size());
                } catch (NullPointerException e) {
                    // ignored
                }
            }
            deeps.put(sentIndex, depth);
            sentence.set(ReadabilityAnnotations.SentenceDepthAnnotation.class, depth);

            Integer contentWords = 0;
            Integer literalWords = 0;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                if (token.get(ReadabilityAnnotations.ContentWord.class) != null && token.get(ReadabilityAnnotations.ContentWord.class)) {
                    contentWords++;
                }
                if (token.get(ReadabilityAnnotations.LiteralWord.class) != null && token.get(ReadabilityAnnotations.LiteralWord.class)) {
                    literalWords++;
                }
            }
            Double sentDensity = (1.0 * contentWords) / literalWords;
            sentence.set(ReadabilityAnnotations.DensityAnnotation.class, sentDensity);
            sentence.set(ReadabilityAnnotations.ContentWordsAnnotation.class, contentWords);
            sentence.set(ReadabilityAnnotations.LiteralWordsAnnotation.class, literalWords);

            if (!sentence.containsKey(VerbAnnotations.VerbsAnnotation.class)) {
                continue;
            }
            if (!language.equals("it")) {
                continue;
            }

            Integer sentCoordinates = 0;
            Integer sentSubordinates = 0;

            List<VerbMultiToken> verbs = sentence.get(VerbAnnotations.VerbsAnnotation.class);
            propositions.add(verbs.size());

            Set<Integer> heads = new HashSet<>();
            for (VerbMultiToken verb : verbs) {
                Map<Integer, String> parentIDs = SemanticGraphUtils.getParent(verb, semanticGraph);
                Integer head = SemanticGraphUtils.getHead(verb, semanticGraph);
                heads.add(head);
//                indexedVerbs.put(head, verb);

                if (parentIDs.size() == 0) {
                    continue;
                }

                if (parentIDs.values().contains("conj")) {
                    sentCoordinates++;
                    continue;
                }

                sentSubordinates++;
            }

            Integer sentTotal = sentCoordinates + sentSubordinates;

            coordinates += sentCoordinates;
            subordinates += sentSubordinates;

            Double sentSubordinateRatio = 0.0;

            if (sentTotal > 0) {
                sentSubordinateRatio = (1.0 * sentSubordinates) / sentTotal;
            }

            sentence.set(ReadabilityAnnotations.SubordinateRatioAnnotation.class, sentSubordinateRatio);

        }

        ttrValue = 1.0 * ttr.size() / (1.0 * i);
        if (deeps.size() > 0) {
            deepAvg = deeps.values().stream().mapToInt(val -> val).average().getAsDouble();
            deepMax = deeps.values().stream().mapToInt(val -> val).max().getAsInt() * 1.0;
        }
        if (propositions.size() > 0) {
            propositionsAvg = propositions.stream().mapToInt(val -> val).average().getAsDouble();
            wordsAvg = (1.0 * getWordCount()) / propositions.stream().mapToInt(val -> val).sum();
            if (wordsAvg == Double.POSITIVE_INFINITY) {
                wordsAvg = 0.0;
            }
        }

        int total = coordinates + subordinates;
        if (total == 0) {
//            coordinateRatio = 0.0;
            subordinateRatio = 0.0;
        } else {
//            coordinateRatio = (1.0 * coordinates) / (coordinates + subordinates);
            subordinateRatio = (1.0 * subordinates) / (coordinates + subordinates);
        }
        density = (1.0 * getContentWordSize()) / getWordCount();

//        System.out.println("Average deep: " + deepAvg);
//        System.out.println("Average propositions: " + propositionsAvg);
//        System.out.println("Average words per proposition: " + wordsAvg);
//        System.out.println(String.format("Coordinates: %d (%.2f%%)", coordinates, coordinateRatio));
//        System.out.println("Coordinates: " + coordinates);
//        System.out.println("Subordinates: " + subordinates);
//        System.out.println(String.format("Subordinates: %d (%.2f%%)", subordinates, subordinateRatio));
//        System.out.println("TTR: " + ttrValue);
    }

    public Map<String, Double> getMeasures() {
        return measures;
    }

    public void addingContentWord(CoreLabel token) {
        token.set(ReadabilityAnnotations.ContentWord.class, true);
    }

    public abstract void addingEasyWord(CoreLabel token);

    public void addingWord(CoreLabel token) {
        token.set(ReadabilityAnnotations.LiteralWord.class, true);
    }

    public abstract void addingToken(CoreLabel token);

    public abstract void addingSentence(CoreMap sentence);

    public void addTooLongSentence(Integer sentenceID) {
        tooLongSentences.add(sentenceID);
    }

    public Set<Integer> getTooLongSentences() {
        return tooLongSentences;
    }

    public String getLanguage() {
        return language;
    }

    public int getContentWordSize() {
        return contentWordSize;
    }

    public void setContentWordSize(int contentWordSize) {
        this.contentWordSize = contentWordSize;
    }

    public int getContentEasyWordSize() {
        return contentEasyWordSize;
    }

    public void setContentEasyWordSize(int contentEasyWordSize) {
        this.contentEasyWordSize = contentEasyWordSize;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getDocLenWithSpaces() {
        return docLenWithSpaces;
    }

    public void setDocLenWithSpaces(int docLenWithSpaces) {
        this.docLenWithSpaces = docLenWithSpaces;
    }

    public int getDocLenWithoutSpaces() {
        return docLenWithoutSpaces;
    }

    public void setDocLenWithoutSpaces(int docLenWithoutSpaces) {
        this.docLenWithoutSpaces = docLenWithoutSpaces;
    }

    public int getDocLenLettersOnly() {
        return docLenLettersOnly;
    }

    public void setDocLenLettersOnly(int docLenLettersOnly) {
        this.docLenLettersOnly = docLenLettersOnly;
    }

    public int getGoodSentenceCount() {
        return goodSentenceCount;
    }

    public void setGoodSentenceCount(int goodSentenceCount) {
        this.goodSentenceCount = goodSentenceCount;
    }

    public int getSentenceCount() {
        return sentenceCount;
    }

    public void setSentenceCount(int sentenceCount) {
        this.sentenceCount = sentenceCount;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public FrequencyHashSet<String> getPosStats() {
        return posStats;
    }

    public FrequencyHashSet<String> getGenericPosStats() {
        return genericPosStats;
    }

    public String getTransformedPos(String pos) {
        return pos;
    }

    public int getHyphenCount() {
        return hyphenCount;
    }

    public void setHyphenCount(int hyphenCount) {
        this.hyphenCount = hyphenCount;
    }

    public void incrementHyphenCount(int increment) {
        this.hyphenCount += increment;
    }

    // thanks! http://stackoverflow.com/questions/3322152/is-there-a-way-to-get-rid-of-accents-and-convert-a-whole-string-to-regular-lette
    public static String flattenToAscii(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        for (char c : string.toCharArray()) {
            if (c <= '\u007F') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public void addWord(CoreLabel token) {
        token.set(ReadabilityAnnotations.ContentWord.class, false);
        token.set(ReadabilityAnnotations.LiteralWord.class, false);

        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
        String word = token.word();

        addingToken(token);

        if (isWordPos(pos)) {
            addingWord(token);
            wordCount++;
            docLenLettersOnly += token.endPosition() - token.beginPosition();

            word = flattenToAscii(word);
            Hyphenation hyphenation = hyphenator.hyphenate(word);

            boolean done = false;
            if (hyphenation != null) {
                try {
                    String h = hyphenation.toString();
                    incrementHyphenCount(hyphenation.length() + 1);
                    token.set(ReadabilityAnnotations.HyphenationAnnotation.class, h);
                    done = true;
                    hyphenWordCount++;
                } catch (Exception e) {
                    // ignored
                }
            }

            if (!done && word.length() < 5) {
                incrementHyphenCount(1);
                hyphenWordCount++;
            }

            if (isContentPos(pos)) {
                contentWordSize++;
                addingContentWord(token);
            }
            if (isEasyPos(pos)) {
                contentEasyWordSize++;
                addingEasyWord(token);
            }
        }
        if (token.get(ReadabilityAnnotations.HyphenationAnnotation.class) == null) {
            token.set(ReadabilityAnnotations.HyphenationAnnotation.class, token.originalText());
        }

        String genericPos = getGenericPos(pos);
        posStats.add(pos);
        genericPosStats.add(genericPos);
    }

    protected String getGenericPos(String pos) {
        return pos.substring(0, 1);
    }

    protected boolean getGenericPosInfo(boolean constraint, Set<String> setToCheck, String pos, boolean reverse) {
        if (constraint) {
            pos = getGenericPos(pos);
        }
        boolean ret = setToCheck.contains(pos);
        if (reverse) {
            return !ret;
        } else {
            return ret;
        }
    }

    public static InputStream getStream(String fileName, @Nullable String defaultFileName)
            throws FileNotFoundException {
        if (fileName != null) {
            File streamFile = new File(fileName);
            if (streamFile.exists()) {
                return new FileInputStream(streamFile);
            }
        }
        InputStream input = SpanishReadabilityModel.class.getResourceAsStream(defaultFileName);
        if (input != null) {
            return input;
        }

        if (defaultFileName != null) {
            return getStream(defaultFileName, null);
        }
        return null;
    }

    protected boolean isWordPos(String pos) {
        return getGenericPosInfo(useGenericForWord, nonWordPosList, pos, true);
    }

    protected boolean isContentPos(String pos) {
        return getGenericPosInfo(useGenericForContent, contentPosList, pos, false);
    }

    protected boolean isEasyPos(String pos) {
        return getGenericPosInfo(useGenericForSimple, simplePosList, pos, false);
    }

    public Double getTtrValue() {
        return ttrValue;
    }

    public Double getDeepAvg() {
        return deepAvg;
    }

    public Double getDeepMax() {
        return deepMax;
    }

    public Double getPropositionsAvg() {
        return propositionsAvg;
    }

    public Double getWordsAvg() {
        return wordsAvg;
    }

//    public Double getCoordinateRatio() {
//        return coordinateRatio;
//    }

    public Double getSubordinateRatio() {
        return subordinateRatio;
    }

    public Double getDensity() {
        return density;
    }

    @Override
    public String toString() {
        return "Readability{" +
                "language='" + language + '\'' +
                ", contentWordSize=" + contentWordSize +
                ", contentEasyWordSize=" + contentEasyWordSize +
                ", wordCount=" + wordCount +
                ", docLenWithSpaces=" + docLenWithSpaces +
                ", docLenWithoutSpaces=" + docLenWithoutSpaces +
                ", docLenLettersOnly=" + docLenLettersOnly +
                ", sentenceCount=" + sentenceCount +
                ", tokenCount=" + tokenCount +
                ", hyphenCount=" + hyphenCount +
                ", hyphenWordCount=" + hyphenWordCount +
                ", ttrValue=" + ttrValue +
                ", deepAvg=" + deepAvg +
                ", deepMax=" + deepMax +
                ", propositionsAvg=" + propositionsAvg +
                ", wordsAvg=" + wordsAvg +
                ", subordinateRatio=" + subordinateRatio +
                ", measures=" + measures +
                ", contentPosList=" + contentPosList +
                ", simplePosList=" + simplePosList +
                ", nonWordPosList=" + nonWordPosList +
                ", tooLongSentences=" + tooLongSentences +
                ", posStats=" + posStats +
                ", genericPosStats=" + genericPosStats +
                '}';
    }
}
