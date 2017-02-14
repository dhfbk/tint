import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;

import java.util.*;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    public static void main(String[] args) {
        String sentenceText;
        sentenceText = "Per gli interventi di seguito descritti, la cui autorizzazione può risultare di competenza dei Comuni o delle CTC in relazione alla tipologia ed alla localizzazione dell'intervento, si indicano i seguenti elaborati, precisando che essi sono orientativi e che comunque devono mostrare chiaramente dove si interviene e come si interviene.";
//        sentenceText = "Il mondo, precisando che si tratta della Terra, è molto bello.";

        try {

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, udpipe, ita_morpho, ita_lemma");
            pipeline.setProperty("customAnnotatorClass.udpipe", "eu.fbk.dh.fcw.udpipe.api.UDPipeAnnotator");
            pipeline.setProperty("udpipe.server", "gardner");
            pipeline.setProperty("udpipe.port", "50020");
            pipeline.load();

            Annotation annotation = pipeline.runRaw(sentenceText);

//            System.out.println(JSONOutputter.jsonPrint(annotation));

            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap sentence : sentences) {

//                Set<Integer> noPunct = new HashSet<>();
                HashMultimap<Integer, Integer> children = HashMultimap.create();
//                Map<Integer, Integer> parents = new HashMap<>();

                SemanticGraph semanticGraph = sentence
                        .get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
                Collection<IndexedWord> rootNodes = semanticGraph.getRoots();
                if (rootNodes.isEmpty()) {
                    continue;
                }

                for (IndexedWord root : rootNodes) {
                    Set<Integer> stack = new HashSet<>();
//                    stack.add(root.index());
                    Set<IndexedWord> used = new HashSet<>();
//                    used.add(root);
                    addChildren(children, stack, root, semanticGraph, used);
//                    recToString(root, wordFormat, sb, 1, used);
                }

                List<String> words = new ArrayList<>();
                Map<Integer, String> replacements = new HashMap<>();
                words.add(",");
                words.add("precisando");
                words.add("che");
                int head = 1;
                boolean split = true;
                replacements.put(0, "");
                replacements.put(1, "Si precisa");

                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                Integer foundHead = null;
                for (int i = 0; i < tokens.size() - (words.size() - 1); i++) {

                    boolean equals = true;
                    for (int j = 0; j < words.size(); j++) {
                        CoreLabel token = tokens.get(i + j);
                        if (!token.originalText().toLowerCase().equals(words.get(j))) {
                            equals = false;
                        }
                    }

                    if (equals) {
                        foundHead = i + head;
                        System.out.println("Beccato! " + tokens.get(foundHead));
                        foundHead++; // indexes start from 1
                        break;
                    }
                }

                if (foundHead != null) {
                    StringBuffer oldSentence = new StringBuffer();
                    StringBuffer newSentence = new StringBuffer();

//                    Set<Integer> tokensToTheOldSentence = new HashSet<>();
                    Set<Integer> tokensToTheNewSentence = new HashSet<>();
//                    Set<Integer> partsToTheOldSentence = new HashSet<>();
//                    Set<Integer> partsToTheNewSentence = new HashSet<>();

                    tokensToTheNewSentence.add(foundHead);
                    tokensToTheNewSentence.addAll(children.get(foundHead));

                    System.out.println(foundHead);
                    System.out.println(tokensToTheNewSentence);

//                    for (int i = 0; i < replacements.size(); i++) {
//                        int thisID = foundHead - head + i;
//                        if (tokensToTheNewSentence.contains(thisID)) {
//                            partsToTheNewSentence.add()
//                        }
//                    }

                    for (int i = 0; i < tokens.size(); i++) {
                        CoreLabel token = tokens.get(i);
                        String toAppend = token.originalText();
                        int relativeID = i - foundHead + head + 1;
                        String replacement = replacements.get(relativeID);
                        if (replacement != null) {
                            toAppend = replacement;
                        }
                        if (toAppend.length() > 0) {
                            if (tokensToTheNewSentence.contains(i + 1)) {
                                newSentence.append(toAppend).append(" ");
                            } else {
                                oldSentence.append(toAppend).append(" ");
                            }
                        }
                    }

                    System.out.println(oldSentence);
                    System.out.println(newSentence);

                }

//                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                    System.out.println(token + " -> " + token.index());
//                }
//
//                System.out.println(semanticGraph);
//                System.out.println(children);

//                for (IndexedWord root : rootNodes) {
//                    noPunct.add(root.index());
////                    System.out.println(root.index());
//                }
//
//                System.out.println(semanticGraph);
//
//                for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
//                    String relationName = edge.getRelation().toString();
//                    if (relationName.equals("punct")) {
//                        continue;
//                    }
//
//                    int source = edge.getSource().index();
//                    int target = edge.getTarget().index();
//                    noPunct.add(target);
//                    children.put(source, target);
////                    parents.put(target, source);
//
//                    System.out.println(relationName + ": " + source + " -> " + target);
//                }
//
//                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                    int index = token.index();
//                    Set<Integer> ch = children.get(index);
//                    if (ch != null) {
//
//                    }
//                }
//
//                System.out.println(children.get(36));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void addChildren(HashMultimap<Integer, Integer> children, Set<Integer> stack, IndexedWord current,
            SemanticGraph semanticGraph, Set<IndexedWord> used) {
        List<SemanticGraphEdge> edges = semanticGraph.getOutEdgesSorted(current);
        used.add(current);
        int index = current.index();

        for (Integer integer : stack) {
            children.put(integer, index);
        }

        Set<Integer> newStack = new HashSet<>(stack);
        newStack.add(index);

        for (SemanticGraphEdge edge : edges) {
            IndexedWord target = edge.getTarget();
//            String relation = edge.getRelation().toString();
//            if (relation.equals("punct")) {
//                continue;
//            }
            if (!used.contains(target)) {
                addChildren(children, newStack, target, semanticGraph, used);
            }
        }
    }
}
