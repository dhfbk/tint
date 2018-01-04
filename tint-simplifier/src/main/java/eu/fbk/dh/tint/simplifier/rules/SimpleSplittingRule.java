package eu.fbk.dh.tint.simplifier.rules;

import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 15/02/17.
 */

public abstract class SimpleSplittingRule implements SimplificationRule {

    private List<String> words = new ArrayList<>();
    private List<Pattern> patterns = new ArrayList<>();
    private Map<Integer, String> replacements = new HashMap<>();
    private int head = 1;
    private boolean useRegex = false;

    private static Pattern replacementPattern = Pattern.compile("\\$([0-9]+)");

//    public SimpleSplittingRule(List<String> words, Map<Integer, String> replacements, int head) {
//        this.words = words;
//        this.replacements = replacements;
//        this.head = head;
//    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public void setReplacements(Map<Integer, String> replacements) {
        this.replacements = replacements;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
    }
    //    public static void main(String[] args) {
//        words.add(",");
//        words.add("precisando");
//        words.add("che");
//        replacements.put(0, "");
//        replacements.put(1, "Si precisa");
//
//    }

    @Override public String apply(Annotation annotation, Map<Integer, HashMultimap<Integer, Integer>> children) {

        if (useRegex) {
            for (String word : words) {
                patterns.add(Pattern.compile(word));
            }
        }

        StringBuffer ret = new StringBuffer();

        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (int sentIndex = 0; sentIndex < sentences.size(); sentIndex++) {
            CoreMap sentence = sentences.get(sentIndex);

            List<Matcher> matchers = new ArrayList<>();
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            Integer foundHead = null;
            for (int i = 0; i < tokens.size() - (words.size() - 1); i++) {

                matchers = new ArrayList<>();

                boolean equals = true;
                for (int j = 0; j < words.size(); j++) {
                    CoreLabel token = tokens.get(i + j);
                    if (useRegex) {
                        matchers.add(patterns.get(j).matcher(token.originalText().toLowerCase()));
                    } else if (!token.originalText().toLowerCase().equals(words.get(j))) {
                        equals = false;
                    }
                }

                if (useRegex) {
                    for (Matcher matcher : matchers) {
                        if (!matcher.find()) {
                            equals = false;
                        }
                    }

                }

                if (equals) {
                    foundHead = i + head;
//                    System.out.println("Beccato! " + tokens.get(foundHead));
                    foundHead++; // indexes start from 1
                    break;
                }
            }

            if (foundHead != null) {
                List<String> groups = new ArrayList<>();
                if (useRegex) {
                    for (Matcher matcher : matchers) {
                        for (int i = 0; i < matcher.groupCount(); i++) {
                            groups.add(matcher.group(i + 1));
                        }
                    }
                }

                StringBuffer oldSentence = new StringBuffer();
                StringBuffer newSentence = new StringBuffer();

//                    Set<Integer> tokensToTheOldSentence = new HashSet<>();
                Set<Integer> tokensToTheNewSentence = new HashSet<>();
//                    Set<Integer> partsToTheOldSentence = new HashSet<>();
//                    Set<Integer> partsToTheNewSentence = new HashSet<>();

                tokensToTheNewSentence.add(foundHead);
                tokensToTheNewSentence.addAll(children.get(sentIndex).get(foundHead));

//                System.out.println(foundHead);
//                System.out.println(tokensToTheNewSentence);

//                    for (int i = 0; i < replacements.size(); i++) {
//                        int thisID = foundHead - head + i;
//                        if (tokensToTheNewSentence.contains(thisID)) {
//                            partsToTheNewSentence.add()
//                        }
//                    }

                for (int i = 0; i < tokens.size(); i++) {
                    CoreLabel token = tokens.get(i);

//                    int spaces = 0;
//                    if (i != tokens.size() - 1) {
//                        CoreLabel nextToken = tokens.get(i + 1);
//                        Integer begin = nextToken.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                        Integer end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//                        spaces = begin - end;
//                        System.out.println("N: " + token + " -- " + nextToken + " -- " + (begin - end));
//                    }
//                    if (i != 0) {
//                        CoreLabel prevToken = tokens.get(i - 1);
//                        Integer begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                        Integer end = prevToken.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//                        spaces = Math.max(spaces, begin - end);
//                        System.out.println("P: " + prevToken + " -- " + token + " -- " + (begin - end));
//                    }
                    String toAppend = token.originalText();
                    int relativeID = i - foundHead + head + 1;
                    String replacement = replacements.get(relativeID);
                    if (useRegex && replacement != null) {
                        Matcher matcher = replacementPattern.matcher(replacement);
                        StringBuffer sb = new StringBuffer(replacement.length());
                        while (matcher.find()) {
//                            String text = matcher.group(1);
                            String text = groups.get(Integer.parseInt(matcher.group(1)) - 1);
                            matcher.appendReplacement(sb, Matcher.quoteReplacement(text));
                        }
                        matcher.appendTail(sb);
                        replacement = sb.toString();
                    }
                    if (replacement != null) {
                        toAppend = replacement;
                    }
                    if (toAppend.length() > 0) {
                        if (tokensToTheNewSentence.contains(i + 1)) {
                            newSentence.append(toAppend);
//                            for (int j = 0; j < spaces; j++) {
                            newSentence.append(" ");
//                            }
                        } else {
                            oldSentence.append(toAppend);
//                            for (int j = 0; j < spaces; j++) {
                            oldSentence.append(" ");
//                            }
                        }
                    }
                }

//                System.out.println(oldSentence);
//                System.out.println(newSentence);

                ret.append(oldSentence).append("\n");
                ret.append(newSentence).append("\n");

            } else {
                ret.append(sentence.get(CoreAnnotations.TextAnnotation.class).trim()).append("\n");
            }

        }

        return ret.toString();
    }
}
