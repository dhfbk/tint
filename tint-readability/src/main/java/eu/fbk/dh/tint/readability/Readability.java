package eu.fbk.dh.tint.readability;

import com.itextpdf.layout.hyphenation.Hyphenation;
import com.itextpdf.layout.hyphenation.Hyphenator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.json.JSONExclude;
import eu.fbk.dh.tint.json.JSONable;
import eu.fbk.dh.tint.json.JSONableString;
import eu.fbk.dh.tint.readability.es.SpanishReadabilityModel;
import eu.fbk.utils.core.FrequencyHashSet;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by alessio on 21/09/16.
 */
public abstract class Readability implements JSONable {

    private String language = null;
    private int contentWordSize = 0, contentEasyWordSize = 0, wordCount = 0;
    private int docLenWithSpaces = 0, docLenWithoutSpaces = 0, docLenLettersOnly = 0;
    private int sentenceCount = 0, tokenCount = 0;
    private int hyphenCount = 0;
    private int hyphenWordCount = 0;

    protected Map<String, Double> measures = new HashMap<>();

    public int getHyphenWordCount() {
        return hyphenWordCount;
    }

    @JSONExclude protected HashSet<String> contentPosList = new HashSet<>();
    @JSONExclude protected HashSet<String> simplePosList = new HashSet<>();
    @JSONExclude protected HashSet<String> nonWordPosList = new HashSet<>();

    protected HashMap<String, String> genericPosDescription = new HashMap<>();
    protected HashMap<String, String> posDescription = new HashMap<>();

    @JSONExclude boolean useGenericForContent = true;
    @JSONExclude boolean useGenericForSimple = true;
    @JSONExclude boolean useGenericForWord = true;

    Set<Integer> tooLongSentences = new HashSet<>();
    FrequencyHashSet<String> posStats = new FrequencyHashSet<>();
    FrequencyHashSet<String> genericPosStats = new FrequencyHashSet<>();

    @JSONExclude protected Hyphenator hyphenator;
    @JSONExclude protected Annotation annotation;

    public Readability(String language, Annotation annotation) {
        this.language = language;
        this.annotation = annotation;

        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        docLenWithSpaces = text.length();
        docLenWithoutSpaces = text.replaceAll("\\s+", "").length();
    }

    public abstract void finalizeReadability();

    public abstract void addingContentWord(CoreLabel token);

    public abstract void addingEasyWord(CoreLabel token);

    public abstract void addingWord(CoreLabel token);

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

    public void addWord(CoreLabel token) {
        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
        String word = token.word();

        addingToken(token);

        if (isWordPos(pos)) {
            addingWord(token);
            wordCount++;
            docLenLettersOnly += token.endPosition() - token.beginPosition();

            Hyphenation hyphenation = hyphenator.hyphenate(word);

            if (hyphenation != null) {
                incrementHyphenCount(hyphenation.length() + 1);
                token.set(ReadabilityAnnotations.HyphenationAnnotation.class,
                        new JSONableString(hyphenation.toString()));
                hyphenWordCount++;
            } else if (word.length() < 5) {
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
            token.set(ReadabilityAnnotations.HyphenationAnnotation.class, new JSONableString(token.originalText()));
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

    @Override public String toString() {
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
                ", measures=" + measures +
                ", contentPosList=" + contentPosList +
                ", simplePosList=" + simplePosList +
                ", nonWordPosList=" + nonWordPosList +
                ", tooLongSentences=" + tooLongSentences +
                ", posStats=" + posStats +
                ", genericPosStats=" + genericPosStats +
                '}';
    }

    @Override public String getName() {
        return "readability";
    }
}
