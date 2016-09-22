package eu.fbk.dh.tint.runner.readability;

import eu.fbk.utils.core.FrequencyHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by alessio on 21/09/16.
 */
abstract class Readability {

    String language = null;
    int contentWordSize, contentEasyWordSize, wordCount;
    int docLenWithSpaces, docLenWithoutSpaces, docLenLettersOnly;
    int sentenceCount, tokenCount;

    protected HashSet<String> contentPosList = new HashSet<>();
    protected HashSet<String> simplePosList = new HashSet<>();
    protected HashSet<String> nonWordPosList = new HashSet<>();

    protected HashMap<String, String> genericPosDescription = new HashMap<>();
    protected HashMap<String, String> posDescription = new HashMap<>();

    protected boolean useGenericForContent = true;
    protected boolean useGenericForSimple = true;
    protected boolean useGenericForWord = true;

    protected Set<Integer> tooLongSentences;
    protected FrequencyHashSet<String> posStats = new FrequencyHashSet<>();
    protected FrequencyHashSet<String> genericPosStats = new FrequencyHashSet<>();

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

    public void addWord(String lemma, String pos) {
        if (isWordPos(pos)) {
            wordCount++;

            if (isContentPos(pos)) {
                contentWordSize++;
            }
            if (isEasyPos(pos)) {
                contentEasyWordSize++;
            }
        }

        String genericPos = getGenericPos(pos);
        posStats.add(pos);
        genericPosStats.add(genericPos);
    }

    protected String getGenericPos(String pos) {
        return pos.substring(0, 1);
    }

    abstract Map<String, Object> json();

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
}
