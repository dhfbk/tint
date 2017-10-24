/*
 * Copyright (2013) Fondazione Bruno Kessler (http://www.fbk.eu/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.fbk.dh.tint.tokenizer;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import eu.fbk.dh.tint.tokenizer.token.CharacterTable;
import eu.fbk.dh.tint.tokenizer.token.Token;
import eu.fbk.dh.tint.tokenizer.token.TokenGroup;
import eu.fbk.utils.core.PropertiesUtils;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Written by Alessio Palmero Aprosio
 * <p>
 * partially based on HardTokenizer, part of the twm-lib package,
 * written by Claudio Giuliano and Alessio Palmero Aprosio.
 */
public class ItalianTokenizer {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>HardTokenizer</code>.
     */
    static Logger logger = LoggerFactory.getLogger(ItalianTokenizer.class);
    static Pattern spaceTokenizer = Pattern.compile("\\s+");

    private Trie trie;
    private Set<Integer> splittingChars = new HashSet<>();
    private Set<Integer> sentenceChars = new HashSet<>();
    private Map<Integer, String> normalizedChars = new HashMap<>();
    private Map<String, String> normalizedStrings = new HashMap<>();
    private Map<Pattern, Integer> expressions = new HashMap<>();

    CoreLabelTokenFactory factory = new CoreLabelTokenFactory();

    public ItalianTokenizer() {
        this(null);
    }

    public ItalianTokenizer(@Nullable File settingFile) {
        Trie.TrieBuilder builder = Trie.builder().removeOverlaps();

        InputStream stream = null;
        if (settingFile != null) {
            try {
                stream = new FileInputStream(settingFile);
            } catch (FileNotFoundException e) {
                // continue
            }
        }
        if (stream == null) {
            stream = this.getClass().getResourceAsStream("/token-settings.xml");
        }

        logger.trace("Loading model");
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr;
            NodeList nl;
            int count;

            Document doc = dBuilder.parse(stream);
            doc.getDocumentElement().normalize();

            // Normalization rules
            expr = xpath.compile("/settings/normalization/char");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                Element element = (Element) item;
                String hexCode = element.getAttribute("hexcode");
                String content = element.getTextContent();

                // Bad: need fix
                if (content.equals("`")) {
                    content = "'";
                }

                int num = Integer.parseInt(hexCode, 16);
                if (content.length() == 0) {
                    continue;
                }
                normalizedChars.put(num, content);
            }
            logger.info("Loaded {} normalization rules", normalizedChars.size());

            // end sentence chars
            expr = xpath.compile("/settings/sentenceSplitting/char");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                Element element = (Element) item;
                String charID = element.getAttribute("id");
                sentenceChars.add(Integer.parseInt(charID));
            }
            logger.info("Loaded {} sentence splitting rules", sentenceChars.size());

            // splitting rules
            expr = xpath.compile("/settings/tokenSplitting/char");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                Element element = (Element) item;
                String charID = element.getAttribute("id");
                splittingChars.add(Integer.parseInt(charID));
            }
            logger.info("Loaded {} token splitting rules", splittingChars.size());

            // expressions
            expr = xpath.compile("/settings/expressions/expression");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            StringBuilder b = new StringBuilder();
            b.append("(");
            boolean first = true;
            count = 0;
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                Element element = (Element) item;
                String regExp = element.getAttribute("find");
                boolean merge = PropertiesUtils.getBoolean(element.getAttribute("merge"), true);
                Integer group = PropertiesUtils.getInteger(element.getAttribute("get"), 1);
                if (merge) {
                    if (!first) {
                        b.append("|");
                    }
                    b.append(regExp);
                    count++;
                    first = false;
                } else {
                    expressions.put(Pattern.compile(regExp), group);
                    count++;
                }
            }
            b.append(")");
            expressions.put(Pattern.compile(b.toString()), 1);
            logger.info("Loaded {} regular expressions", count);

            // abbreviations
            expr = xpath.compile("/settings/abbreviations/abbreviation");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            count = 0;
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                String abbr = item.getTextContent();
                abbr = getString(tokenArray(abbr));
                builder.addKeyword(" " + abbr + " ");
                count++;
            }
            logger.info("Loaded {} abbreviations", count);

        } catch (Exception e) {
            e.printStackTrace();
        }

        trie = builder.build();
    }

    public TokenGroup tokenArray(String text) {

        if (text.length() == 0) {
            return new TokenGroup();
        }

//        List<Token> list = new ArrayList<Token>();
        TokenGroup tokenGroup = new TokenGroup();

        Character currentChar;
        Character previousChar = null;
        int start = 0;
        Boolean isCurrentCharLetterOrDigit;
        Boolean isPreviousCharLetterOrDigit;

        MutableBoolean isNewLine = new MutableBoolean(false);
        Token lastToken = new Token(0, 0, "");

        //logger.debug("0\t" + (int) previousChar + "\t<" + previousChar + ">");
        for (int i = 0; i < text.length(); i++) {

            currentChar = text.charAt(i);
            isCurrentCharLetterOrDigit = Character.isLetterOrDigit(currentChar);
            isPreviousCharLetterOrDigit = previousChar != null && Character.isLetterOrDigit(previousChar);

            if (isCurrentCharLetterOrDigit) {
                if (!isPreviousCharLetterOrDigit) {
                    start = i;
                }
            } else {
                if (isPreviousCharLetterOrDigit) {
                    String substring = text.substring(start, i);
                    addToken(tokenGroup, start, i, substring, isNewLine, lastToken);

                    if (!splittingChars.contains(currentChar.hashCode())) {
                        String charString = new String(new char[]{currentChar});
                        addToken(tokenGroup, i, i + 1, charString, isNewLine, lastToken);
                    }
                } else {
                    if (!splittingChars.contains(currentChar.hashCode())) {
                        String charString = new String(new char[]{currentChar});
                        addToken(tokenGroup, i, i + 1, charString, isNewLine, lastToken);
                    }
                }
            }

            if (sentenceChars.contains(currentChar.hashCode())) {
                isNewLine.setValue(true);
            }

            previousChar = currentChar;
        }
        if (Character.isLetterOrDigit(previousChar)) {
            String substring = text.substring(start, text.length());
            addToken(tokenGroup, start, text.length(), substring, isNewLine, lastToken);
        }

        return tokenGroup;
    }

    private void addToken(TokenGroup group, int start, int end, String charString, MutableBoolean isNewLine,
                          Token lastToken) {
        Token token = new Token(start, end, charString);
        if (isNewLine.booleanValue()) {
            group.addNewLine(start);
            isNewLine.setValue(false);
        }
        token.setPreceedBySpace(start - lastToken.getEnd() > 0);

        int spaces = 0;
        if (lastToken != null && lastToken.getEnd() != 0) {
            int endLast = lastToken.getEnd();
            spaces = lastToken.getSpaceOffset();
            if (start == endLast) {
                spaces++;
            } else {
                spaces -= Math.max(0, start - endLast - 1);
            }
        }
        token.setSpaceOffset(spaces);

        // Normalization
        String n;
        if (charString.length() == 1) {
            int c = charString.charAt(0);
            n = normalizedChars.get(c);
        } else {
            n = normalizedStrings.get(charString);
        }
        if (n != null) {
            token.setNormForm(n);
        }

        lastToken.updateByToken(token);
        group.addToken(token);
    }

    public static String getString(TokenGroup tokenGroup) {
        StringBuilder buffer = new StringBuilder();
        ArrayList<Token> tokens = tokenGroup.getSupport();

        // todo: check this
        if (tokens.size() > 0) {
            for (int i = 0; i < tokens.size() - 1; i++) {
                Token token = tokens.get(i);
                buffer.append(token.getForm()).append(CharacterTable.SPACE);
            }
            buffer.append(tokens.get(tokens.size() - 1).getForm());
        }
        return buffer.toString();
    }

//    public boolean isSeparatorChar(Character ch) {
//        if (splittingChars.size() > 0) {
//            return splittingChars.contains(ch.hashCode());
//        } else if (ch == CharacterTable.SPACE) {
//            return true;
//        } else if (ch == CharacterTable.CARRIADGE_RETURN) {
//            return true;
//        } else if (ch == CharacterTable.LINE_FEED) {
//            return true;
//        } else if (ch == CharacterTable.HORIZONTAL_TABULATION) {
//            return true;
//        } else if (ch == CharacterTable.FORM_FEED) {
//            return true;
//        }
//
//        return false;
//    }

    public List<List<CoreLabel>> parse(String text) {
        return parse(text, true, false, false);
    }

    public List<List<CoreLabel>> parse(String text, boolean newlineIsSentenceBreak, boolean tokenizeOnlyOnSpace,
                                       boolean ssplitOnlyOnNewLine) {

        List<List<CoreLabel>> ret = new ArrayList<>();
        List<CoreLabel> temp = new ArrayList<>();

        int index = 0;

        if (tokenizeOnlyOnSpace) {

            int nextStart = 0;
            boolean lastIsWhitespace = true;
            int newLineCount = 0;

            for (int i = 0; i < text.length(); i++) {
                char currentChar = text.charAt(i);
                boolean isLast = i == text.length() - 1;
                if (Character.isWhitespace(currentChar) || isLast) {
                    if (!lastIsWhitespace) {
//                        System.out.println("---" + text.substring(nextStart, i) + "---" + newLineCount);

                        int j = i;
                        if (isLast) {
                            j++;
                        }
                        String word = text.substring(nextStart, j);
                        CoreLabel clToken = factory.makeToken(word, word, nextStart, j - nextStart);
                        clToken.setIndex(++index);

                        if (newlineIsSentenceBreak && newLineCount > 0) {
                            if (temp.size() > 0) {
                                ret.add(temp);
                                index = 0; // index must be zeroed to meet Stanford policy
                                temp = new ArrayList<>();
                            }
                        }

                        temp.add(clToken);

                        if (!ssplitOnlyOnNewLine) {
                            if (word.length() == 1 && sentenceChars.contains((int) word.charAt(0))) {
                                ret.add(temp);
                                index = 0; // index must be zeroed to meet Stanford policy
                                temp = new ArrayList<>();
                            }
                        }

                        newLineCount = 0;
                    }
                    if (currentChar == '\n') {
                        newLineCount++;
                    }
                    lastIsWhitespace = true;
                } else {
                    if (lastIsWhitespace) {
                        nextStart = i;
                    }
                    lastIsWhitespace = false;
                }
            }

            if (temp.size() > 0) {
                ret.add(temp);
            }
        } else {
            HashMap<Integer, Integer> mergeList = new HashMap<>();

            for (Pattern expression : expressions.keySet()) {
                int get = expressions.get(expression);
                Matcher matcher = expression.matcher(text);
                while (matcher.find()) {
                    mergeList.put(matcher.start(get), matcher.end(get));
                }
            }

            TokenGroup tokenGroup = tokenArray(text);
            ArrayList<Token> tokens = tokenGroup.getSupport();

            if (tokens.size() == 0) {
                return ret;
            }

            int offset = tokens.get(0).getStart();
            String s = " " + getString(tokenGroup) + " ";

            Collection<Emit> emits = trie.parseText(s);
            for (Emit emit : emits) {
                // Added -1 for compatibility with the "s" string

                Token startToken = tokenGroup.getStartOffIndexes().get(emit.getStart() + 1 - 1 + offset);
//                Token endToken = tokenGroup.getEndOffIndexes().get(emit.getEnd() - 1 - 1 + offset);

                Token endToken = null;
                int endOffset = 0;
                while (endToken == null) {
                    endToken = tokenGroup.getEndOffIndexes().get(emit.getEnd() - endOffset - 1 + offset);
                    endOffset++;
                }

                if (newlineIsSentenceBreak) {
                    String substring = text.substring(startToken.getStart(), endToken.getEnd());
                    if (substring.contains("\n") || substring.contains("\r")) {
                        continue;
                    }
                }
                if (startToken != null && endToken != null) {
                    mergeList.put(startToken.getStart(), endToken.getEnd());
                } else {
                    logger.warn("Something is null! -- " + emit.toString());
                }
            }

            Integer end = null;
            Integer start = null;

            Set<Integer> newLines = tokenGroup.getNewLines();

            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                boolean merging = false;

                if (mergeList.containsKey(token.getStart()) || end != null) {
                    merging = true;
                    if (end == null) {
                        end = mergeList.get(token.getStart());
                    }
                }

                if (merging && (end != null && token.getEnd() >= end)) {
                    end = null;
                    merging = false;
                }

                if (token.getNormForm().equals("'")) {

                    Token prevToken = null,
                            nextToken = null;
                    if (i > 0) {
                        prevToken = tokens.get(i - 1);
                    }
                    if (i < tokens.size() - 1) {
                        nextToken = tokens.get(i + 1);
                    }

                    // Example: l'economia
                    if (prevToken != null &&
                            nextToken != null &&
                            !token.isPreceedBySpace() &&
                            !nextToken.isPreceedBySpace() &&
                            Character.isLetter(prevToken.getForm().charAt(prevToken.getForm().length() - 1)) &&
                            Character.isLetter(nextToken.getForm().charAt(0))) {
                        CoreLabel lastToken = temp.get(temp.size() - 1);
                        start = lastToken.beginPosition();
                        temp.remove(temp.size() - 1);
                        index--;
                    }

                    // Example: sta'
                    else if (prevToken != null &&
                            Character.isLetter(prevToken.getForm().charAt(prevToken.getForm().length() - 1)) &&
                            !token.isPreceedBySpace() &&
                            (nextToken == null || !nextToken.getNormForm().equals("'"))) {
                        CoreLabel lastToken = temp.get(temp.size() - 1);
                        start = lastToken.beginPosition();
                        temp.remove(temp.size() - 1);
                        index--;
                    }

                    // Example: 'ndrangheta
                    else if (nextToken != null &&
                            Character.isLetter(nextToken.getForm().charAt(0)) &&
                            !nextToken.isPreceedBySpace() &&
                            (prevToken == null || !prevToken.getNormForm().equals("'"))) {
                        merging = true;
                    }
                }

                if (merging) {
                    if (start == null) {
                        start = token.getStart();
                    }
                    continue;
                }

                if (start == null) {
                    start = token.getStart();
                } else {
                    newLines.remove(token.getEnd() + 1);
                }

                int finish = token.getEnd();
                String word = text.substring(start, finish);
                String normWord = word;

                // todo: bad
                // solves https://github.com/dhfbk/tint/issues/3
                if (word.charAt(word.length() - 1) == '’' || word.charAt(word.length() - 1) == '`') {
                    normWord = word.substring(0, word.length() - 1) + "'";
                }
                CoreLabel clToken = factory.makeToken(normWord, word, start, finish - start);
                clToken.setIndex(++index);

                if (newlineIsSentenceBreak && newLines.contains(start)) {
                    if (temp.size() > 0) {
                        ret.add(temp);
                        index = 0; // index must be zeroed to meet Stanford policy
                        temp = new ArrayList<>();
                    }
                }

                temp.add(clToken);

                if (!ssplitOnlyOnNewLine) {
                    if (word.length() == 1 && sentenceChars.contains((int) word.charAt(0))) {
                        ret.add(temp);
                        index = 0; // index must be zeroed to meet Stanford policy
                        temp = new ArrayList<>();
                    }
                }

                start = null;
            }

            if (temp.size() > 0) {
                ret.add(temp);
                index = 0; // index must be zeroed to meet Stanford policy
            }
        }

        return ret;
    }

    public static void main(String argv[]) throws IOException {

        ItalianTokenizer tokenizer = new ItalianTokenizer();

//        byte[] file = Files.readAllBytes((new File("/Users/alessio/Desktop/milano.txt")).toPath());
//        String text = new String(file);
        String text = "Clinton in testa nei sondaggi dopo l’«assoluzione» dell’Fbi sull’uso di un server di posta privato quando era Segretario di stato.";
//        text = "``Determinato, pronto a «fare tutto il necessario per mantenere la stabilità dei prezzi».''"
//                + " Ma anche allarmato per come le conseguenze del referendum britannico minacciano l’economia e i mercati europei."
//                + " Sono nato nel 200 S.p.A."
//                + " Il mio indirizzo e-mail è alessio@apnetwork.it."
//                + " Il blog è http://www.ziorufus.it e mi piace molto.";
//        text = "Questo è un test per una sigla qualsiasi tipo a.B.C. che non ha senso.";
//        text = "Milano (/miˈlano/ ascolta[?·info], in milanese: Milan[4], /miˈlãː/[5]) è una città italiana di 1 346 153 abitanti[2], capoluogo dell'omonima città metropolitana e della regione Lombardia, secondo comune italiano per numero di abitanti, tredicesimo comune dell'Unione europea e diciannovesimo del continente e, con l'agglomerato urbano, quarta area metropolitana più popolata d'Europa dopo Londra, Madrid e Parigi[6].\n"
//                + "\n"
//                + "Fondata dagli Insubri all'inizio del VI secolo a.C.[7], fu conquistata dai Romani nel 222 a.C.";

//        System.out.println(text);

        long time = System.currentTimeMillis();
        List<List<CoreLabel>> sentences = tokenizer.parse(text, true, true, true);
        time = System.currentTimeMillis() - time;

        for (int i = 0; i < Math.min(10, sentences.size()); i++) {
            List<CoreLabel> sentence = sentences.get(i);
            for (CoreLabel token : sentence) {
                System.out.println(token.word() + " -- " + token.originalText() + " -- " + token.beginPosition());

            }
            System.out.println();
        }

        int sentenceSize = sentences.size();
        int lastTokenIndex = sentences.get(sentenceSize - 1).get(sentences.get(sentenceSize - 1).size() - 1).index();
        System.out.println("Length: " + text.length());
        System.out.println("Time: " + time);
        System.out.println("Sentences: " + sentenceSize);
        System.out.println("Tokens: " + lastTokenIndex);
    }
}
