package eu.fbk.dh.tint.simplifier;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static eu.fbk.dh.tint.simplifier.Simplifier.*;

public class Test {

    static String PREPOSITION_PREFIX = "E";
    static String ADVERB_PREFIX = "B";
    static String SUBJ_RELATION = "nsubj";
    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

    static Set<String> prepositions = new HashSet<>();
    static Set<String> agnosticBegins = new HashSet<>();

    static {
        prepositions.add("dopo");
        agnosticBegins.add("sulla base");
        agnosticBegins.add("solo in caso di");
    }

    abstract static class Action {

        Annotation annotation;

        abstract String apply(String text, int[] conversionTable);

//        protected Integer getValue(Integer id, Map<Integer, Integer> conversionTable) {
//            return conversionTable.getOrDefault(id, id);
//        }

        public Action(Annotation annotation) {
            this.annotation = annotation;
        }
    }

    static class Remove extends Action {

        Integer originalStart;
        Integer originalEnd;
        boolean checkSpaceInside = true;

        public Remove(Annotation annotation, Integer originalStart, Integer originalEnd) {
            super(annotation);
            this.originalStart = originalStart;
            this.originalEnd = originalEnd;
        }

        @Override public String toString() {
            return "Remove{" +
                    "originalStart=" + originalStart +
                    ", originalEnd=" + originalEnd +
                    '}';
        }

        @Override String apply(String text, int[] conversionTable) {
//            System.out.println("DELETE");
//            System.out.println(text);
//            System.out.println(conversionTable);
//            System.out.println(originalStart);
//            System.out.println(originalEnd);
            Integer start = conversionTable[originalStart];
            Integer end = conversionTable[originalEnd];
            for (int i = originalStart; i < Math.min(originalEnd, conversionTable.length); i++) {
                conversionTable[i] = start;
            }
            for (int i = originalEnd; i < conversionTable.length; i++) {
                conversionTable[i] -= end - start;
            }
            StringBuffer buffer = new StringBuffer();
            buffer.append(text.substring(0, start));
            buffer.append(text.substring(end));
            return buffer.toString();
        }
    }

    static class Insert extends Action {

        Integer originalStart;
        String textToInsert;
        boolean checkSpaceBefore = false;
        boolean checkSpaceAfter = true;

        public Insert(Annotation annotation, Integer originalStart, String textToInsert) {
            super(annotation);
            this.originalStart = originalStart;
            this.textToInsert = textToInsert;
        }

        @Override public String toString() {
            return "Insert{" +
                    "originalStart=" + originalStart +
                    ", textToInsert='" + textToInsert + '\'' +
                    '}';
        }

        @Override String apply(String text, int[] conversionTable) {
//            System.out.println("INSERT");
//            System.out.println(text);
//            System.out.println(conversionTable);
//            System.out.println(originalStart);
//            System.out.println(textToInsert);
            StringBuffer buffer = new StringBuffer();
            Integer start = conversionTable[originalStart];
            for (int i = start; i < conversionTable.length; i++) {
                conversionTable[i] += textToInsert.length();
            }
            buffer.append(text.substring(0, start));
            buffer.append(textToInsert);
            buffer.append(text.substring(start));
            return buffer.toString();
        }
    }

    public static String complexString(Annotation annotation) throws Exception {
        String originalText = annotation.get(CoreAnnotations.TextAnnotation.class);
        int offset = 0;
//        StringBuffer stringBuffer = new StringBuffer();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

            // Looking for commas
            List<Integer> commas = new ArrayList<>();
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                if (token.originalText().equals(",")) {
                    commas.add(token.index());
                }
            }

            SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//            System.out.println(semanticGraph);

            // Collecting nsubj relations
            // Warning: the key MUST be lower than the value
            Map<Integer, Integer> subjects = new HashMap<>();
            for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
                String relation = edge.getRelation().getShortName();
                if (!relation.equals(SUBJ_RELATION)) {
                    continue;
                }

                int index1 = edge.getDependent().index();
                int index2 = edge.getGovernor().index();
                subjects.put(Math.min(index1, index2), Math.max(index1, index2));
            }

//                Map<Integer, Integer> removes = new HashMap<>();
//                Map<Integer, String> inserts = new HashMap<>();

            for (int i1 = 0, commasSize1 = commas.size(); i1 < commasSize1 - 1; i1++) {
                List<Action> actions = new ArrayList<>();

                Integer comma1 = commas.get(i1);
                Integer comma2 = commas.get(i1 + 1);

                StringBuffer buffer = new StringBuffer();
                Integer okStart = 0; // for adverbs

                Set<IndexedWord> parents = new HashSet<>();
                Set<IndexedWord> children = new HashSet<>();
                for (int i = comma1 + 1; i < comma2; i++) {
                    IndexedWord node = semanticGraph.getNodeByIndex(i);
                    parents.addAll(getParents(semanticGraph, node));
                    children.addAll(getChildren(semanticGraph, node));
                    CoreLabel token = sentence.get(CoreAnnotations.TokensAnnotation.class).get(i - 1);
                    buffer.append(token.originalText()).append(" ");
                }
                String sentenceText = buffer.toString().trim();

                for (int i = comma1; i <= comma2; i++) {
                    IndexedWord node = semanticGraph.getNodeByIndex(i);
                    children.remove(node);
                    parents.remove(node);
                }

                // Rule: children inside the set, only one parent outside the set
                // Warning: if this rule changes, check the code below
                if (parents.size() != 1 || children.size() != 0) {
                    continue;
                }

                // Rule: the whole set is included in a subject-verb pair
                Integer included = null;
                for (Integer key : subjects.keySet()) {
                    Integer value = subjects.get(key);
                    if (comma1 > key && comma2 < value) {
                        included = key;
                        break;
                    }
                }
                if (included == null) {
                    continue;
                }

                // Rule: check how the part begins
                boolean keepForWord = false;
                String okText = sentenceText.substring(okStart).trim();
                for (String preposition : prepositions) {
                    // todo: check end of token
                    if (okText.toLowerCase().startsWith(preposition.toLowerCase() + " ")) {
                        keepForWord = true;
                    }
                }
                for (String agnosticBegin : agnosticBegins) {
                    // todo: check end of token
                    if (okText.toLowerCase().startsWith(agnosticBegin.toLowerCase() + " ")) {
                        keepForWord = true;
                    }
                }

                if (!keepForWord) {
                    continue;
                }

                // There should be only one word in parents
                IndexedWord parent = parents.iterator().next();

                List<IndexedWord> allChildren = getChildrenRecursive(semanticGraph, parent);
                if (allChildren.size() == 0) {
                    continue;
                }

                Integer firstIndex = -1;
                for (IndexedWord child : allChildren) {
                    int index = child.index();
                    if (firstIndex == -1 || firstIndex > index) {
                        firstIndex = index;
                    }
                }

                // Need to remove this part
                int removeBegin = sentence.get(CoreAnnotations.TokensAnnotation.class).get(comma1 - 1).beginPosition();
                int removeEnd = sentence.get(CoreAnnotations.TokensAnnotation.class).get(comma2 - 1).endPosition();
                actions.add(new Remove(annotation, removeBegin, removeEnd));

                // Need to add this part
                int insertBegin = sentence.get(CoreAnnotations.TokensAnnotation.class).get(firstIndex - 1).beginPosition();
                actions.add(new Insert(annotation, insertBegin, sentenceText + ", "));

//                    System.out.println(buffer.toString());
//                    System.out.println(firstIndex);
//
//                    System.out.println(allChildren);
//                    System.out.println(comma1);
//                    System.out.println(comma2);
//                    System.out.println(parents);
//                    System.out.println(children);
//                    System.out.println(included);

                String text = sentence.get(CoreAnnotations.TextAnnotation.class);
                int[] conversionTable = new int[text.length()];
                for (int i = 0; i < text.length(); i++) {
                    conversionTable[i] = i;
                }
                for (Action action : actions) {
                    text = action.apply(text, conversionTable);
                }

                Integer begin = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                Integer end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(originalText.substring(0, begin + offset));
                stringBuffer.append(text);
                stringBuffer.append(originalText.substring(end + offset));
                originalText = stringBuffer.toString().trim();

                offset += text.length() - (end - begin);

//                System.out.println(sentenceText);
//                System.out.println(text);
//                System.out.println(Arrays.toString(conversionTable));

            }

        }

        return originalText;
    }

    public static void main(String[] args) {

//        String inputFile = "/Users/alessio/Documents/SIMPATICO/sintattico/simpitiki-syntax.xml";
//        String outFile = "/Users/alessio/Documents/SIMPATICO/sintattico/simpitiki-sentences.txt";

        String inputFile = args[0];
        String outFile = args[1];

        /*

        Before running:
        - create XML adding <simplification> and <before> tags
        - sed -ie 's~&~\&amp;~g' [file]
        - perl -CSDA -pe's/[^\x9\xA\xD\x20-\x{D7FF}\x{E000}-\x{FFFD}\x{10000}-\x{10FFFF}]+//g;' [source] > [dest]

        Next commands:
        - transform for Lex -> cut -f4 [origin] > [destination]
        - run the Python stuff
        - paste the files -> paste [destination] [destination-parsed] > [final-file]
         */

        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr;
            NodeList nl;
            Document doc = dBuilder.parse(new File(inputFile));
            int totalSimplified = 0;
            int totalSentences = 0;

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, depparse, fake_dep");

            pipeline.load();

            expr = xpath.compile("/root/simplification");
            nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                totalSentences++;
                Node item = nl.item(i);
                Element element = (Element) item;

                String tbsTemp = element.getAttribute("toBeSimplified");
                if (tbsTemp == null || tbsTemp.length() == 0) {
                    tbsTemp = "1";
                }
                boolean toBeSimplified = PropertiesUtils.getBoolean(tbsTemp, true);

                expr = xpath.compile("before");
                NodeList beforeList = (NodeList) expr.evaluate(item, XPathConstants.NODESET);
//                expr = xpath.compile("after");
//                NodeList afterList = (NodeList) expr.evaluate(item, XPathConstants.NODESET);

                Node before = beforeList.item(0);
//                Node after = afterList.item(0);

                String text1 = before.getTextContent();
//                String text2 = after.getTextContent();

                Annotation annotation = pipeline.runRaw(text1);

//                Map<Integer, HashMultimap<Integer, Integer>> children = new HashMap<>();
//                List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//                for (int sentIndex = 0; sentIndex < sentences.size(); sentIndex++) {
//                    CoreMap sentence = sentences.get(sentIndex);
//
//                    children.put(sentIndex, HashMultimap.create());
//
//                    SemanticGraph semanticGraph = sentence
//                            .get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
//                    Collection<IndexedWord> rootNodes = semanticGraph.getRoots();
//                    if (rootNodes.isEmpty()) {
//                        continue;
//                    }
//
//                    for (IndexedWord root : rootNodes) {
//                        Set<Integer> stack = new HashSet<>();
//                        Set<IndexedWord> used = new HashSet<>();
//                        addChildren(children.get(sentIndex), stack, root, semanticGraph, used);
//                    }
//                }

                String originalText = annotation.get(CoreAnnotations.TextAnnotation.class);
                writer.append(toBeSimplified ? "1" : "0");
                writer.append("\t");
                writer.append(originalText.trim());
                writer.append("\t");

//                SimplificationRule rule;
//                String output;

                String simplifiedText = originalText;

                simplifiedText = complexString(annotation);
                simplifiedText = simplifiedText.replaceAll(", eppure", ". Eppure");
                simplifiedText = simplifiedText.replaceAll(", tuttavia", ". Tuttavia");

                int offset = 0;
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    Integer sentenceOffset = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
                    Collection<IndexedWord> rootNodes = semanticGraph.getRoots();
                    if (rootNodes.size() != 1) {
                        continue;
                    }

                    List<Action> actions = new ArrayList<>();

                    IndexedWord rootNode = rootNodes.iterator().next();
                    List<SemanticGraphEdge> outEdgesSorted = semanticGraph.getOutEdgesSorted(rootNode);
                    List<IndexedWord> underRoot = new ArrayList<>();
                    List<Action> tmpActions = null;
                    for (SemanticGraphEdge semanticGraphEdge : outEdgesSorted) {

                        IndexedWord dependent = semanticGraphEdge.getDependent();
                        int depIndex = dependent.index();
                        String depText = dependent.originalText().toLowerCase();

                        Integer begin = dependent.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentenceOffset;
                        Integer end = dependent.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) - sentenceOffset;
                        if (semanticGraphEdge.getRelation().getShortName().equals("cc")) {
                            if (depText.equals("e") || depText.equals("ed")) {
                                tmpActions = new ArrayList<>();
                                tmpActions.add(new Remove(annotation, begin, end));
                                tmpActions.add(new Insert(annotation, begin, ". "));
                                if (depIndex > 1) {
                                    CoreLabel previousToken = sentence.get(CoreAnnotations.TokensAnnotation.class).get(depIndex - 2);
                                    if (previousToken.originalText().equals(",")) {
                                        Integer pbegin = previousToken.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentenceOffset;
                                        Integer pend = previousToken.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) - sentenceOffset;
                                        tmpActions.add(new Remove(annotation, pbegin, pend));
                                    }
                                }
                            }
                            if (depText.equals("ma")) {
                                tmpActions = new ArrayList<>();
                                tmpActions.add(new Remove(annotation, begin, end));
                                tmpActions.add(new Insert(annotation, begin, ". Però"));
                                if (depIndex > 1) {
                                    CoreLabel previousToken = sentence.get(CoreAnnotations.TokensAnnotation.class).get(depIndex - 2);
                                    if (previousToken.originalText().equals(",")) {
                                        Integer pbegin = previousToken.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentenceOffset;
                                        Integer pend = previousToken.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) - sentenceOffset;
                                        tmpActions.add(new Remove(annotation, pbegin, pend));
                                    }
                                }
                            }
                        } else {
                            if (tmpActions != null && dependent.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("V")) {
                                actions.addAll(tmpActions);
                            }
                            tmpActions = null;
                        }
//                        System.out.println(semanticGraphEdge);
                        underRoot.add(dependent);
                    }

                    if (actions.size() == 0) {
                        continue;
                    }

                    String text = sentence.get(CoreAnnotations.TextAnnotation.class);
//                    String text = sText;
                    int[] conversionTable = new int[text.length()];
                    for (int j = 0; j < text.length(); j++) {
                        conversionTable[j] = j;
                    }
                    for (Action action : actions) {
                        text = action.apply(text, conversionTable);
                    }

//                    if (!text.equals(sText)) {
//                        System.out.println(text);
//                        System.out.println(sText);
//                    }
//                    System.out.println();

                    Integer begin = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    Integer end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(simplifiedText.substring(0, begin));
                    stringBuffer.append(text);
                    stringBuffer.append(simplifiedText.substring(end));
                    simplifiedText = stringBuffer.toString().trim();

                    offset += text.length() - (end - begin);
                }

                boolean hasBeenSimplified = false;
                if (!simplifiedText.equals(originalText)) {
                    hasBeenSimplified = true;
                    totalSimplified++;
                }

                writer.append(hasBeenSimplified ? "1" : "0");
                writer.append("\t");
                writer.append(simplifiedText.trim());
                writer.append("\n");

//                System.out.println(originalText);
//                System.out.println(simplifiedText);
//                System.out.println();

//                rule = new DenominatiSplittingRule();
//                output = rule.apply(annotation, children);
//
//                System.out.println(output);
//
//                rule = new GarantendoSplittingRule();
//                output = rule.apply(annotation, children);
//
//                System.out.println(output);
//
//                rule = new GarantendoSplittingRule();
//                output = rule.apply(annotation, children);
//
//                System.out.println(text1);
//                System.out.println(simplifiedText);
//                System.out.println(text2);
//                System.out.println();
//
//                System.exit(1);
            }

            System.out.println(totalSimplified);
            System.out.println(totalSentences);
//            pipeline.run(sentence, System.out, TintRunner.OutputFormat.JSON);

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
