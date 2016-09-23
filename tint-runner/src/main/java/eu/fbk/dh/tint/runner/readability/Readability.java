package eu.fbk.dh.tint.runner.readability;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itextpdf.layout.hyphenation.Hyphenation;
import com.itextpdf.layout.hyphenation.Hyphenator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import eu.fbk.dh.tint.runner.JSONable;
import eu.fbk.utils.core.FrequencyHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alessio on 21/09/16.
 */
abstract class Readability implements JSONable {

    private String language = null;
    private int contentWordSize = 0, contentEasyWordSize = 0, wordCount = 0;
    private int docLenWithSpaces = 0, docLenWithoutSpaces = 0, docLenLettersOnly = 0;
    private int sentenceCount = 0, tokenCount = 0;
    private int hyphenCount = 0;
    private int hyphenWordCount = 0;

    protected HashSet<String> contentPosList = new HashSet<>();
    protected HashSet<String> simplePosList = new HashSet<>();
    protected HashSet<String> nonWordPosList = new HashSet<>();

    protected HashMap<String, String> genericPosDescription = new HashMap<>();
    protected HashMap<String, String> posDescription = new HashMap<>();

    protected boolean useGenericForContent = true;
    protected boolean useGenericForSimple = true;
    protected boolean useGenericForWord = true;

    protected Set<Integer> tooLongSentences = new HashSet<>();
    protected FrequencyHashSet<String> posStats = new FrequencyHashSet<>();
    protected FrequencyHashSet<String> genericPosStats = new FrequencyHashSet<>();

    protected Hyphenator hyphenator;

    public Readability(String language) {
        this.language = language;
    }

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
        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
        String word = token.word();

        if (isWordPos(pos)) {
            wordCount++;
            docLenLettersOnly += token.endPosition() - token.beginPosition();

            if (isContentPos(pos)) {
                contentWordSize++;
            }
            if (isEasyPos(pos)) {
                contentEasyWordSize++;
            }

            Hyphenation hyphenation = hyphenator.hyphenate(word);

            if (hyphenation != null) {
                incrementHyphenCount(hyphenation.length() + 1);
                hyphenWordCount++;
            } else if (word.length() < 5) {
                incrementHyphenCount(1);
                hyphenWordCount++;
            }
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
                ", hyphenCount=" + hyphenCount +
                ", hyphenWordCount=" + hyphenWordCount +
                ", sentenceCount=" + sentenceCount +
                ", tokenCount=" + tokenCount +
                ", contentPosList=" + contentPosList +
                ", simplePosList=" + simplePosList +
                ", nonWordPosList=" + nonWordPosList +
                ", genericPosDescription=" + genericPosDescription +
                ", posDescription=" + posDescription +
                ", useGenericForContent=" + useGenericForContent +
                ", useGenericForSimple=" + useGenericForSimple +
                ", useGenericForWord=" + useGenericForWord +
                ", tooLongSentences=" + tooLongSentences +
                ", posStats=" + posStats +
                ", genericPosStats=" + genericPosStats +
                '}';
    }

    public static void main(String[] args) {
//        Readability readability = new ItalianStandardReadability();
        Hyphenator h = new Hyphenator("it", "IT", 2, 2);
        Hyphenation hyphenation = h.hyphenate("lasciavamo");
        System.out.println(hyphenation);
    }

    @Override public String getName() {
        return "readability";
    }
}
