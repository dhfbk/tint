package eu.fbk.dh.tint.simplifier.rules;

import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.digimorph.annotator.DigiMorphAnnotations;
import eu.fbk.dh.tint.inverse.digimorph.annotator.InverseDigiMorph;

import java.util.*;

import static eu.fbk.dh.tint.simplifier.Simplifier.getHistory;

/**
 * Created by alessio on 15/02/17.
 */

public class ReplaceSubordinateRule implements SimplificationRule {

    static TreeSet<String> getPersons(SemanticGraph semanticGraph, IndexedWord word, CoreMap sentence) {
        Stack<IndexedWord> wordsToCheck = new Stack<>();
        wordsToCheck.add(word);

        int index = word.index();

        while (!wordsToCheck.isEmpty()) {
            IndexedWord thisWord = wordsToCheck.pop();
            List<SemanticGraphEdge> outEdgesSorted = semanticGraph.getOutEdgesSorted(thisWord);
            for (SemanticGraphEdge semanticGraphEdge : outEdgesSorted) {
                IndexedWord dependent = semanticGraphEdge.getDependent();
                String pos = dependent.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (pos.equals("VA")) {
                    index = Math.min(index, dependent.index());
                    wordsToCheck.push(dependent);
                }
            }
        }

        CoreLabel token = sentence.get(CoreAnnotations.TokensAnnotation.class).get(index - 1);
        String morpho = token.get(DigiMorphAnnotations.MorphoAnnotation.class);
        String[] parts = morpho.split("\\s+");
        TreeSet<String> persons = new TreeSet<>();
        for (int i = 1; i < parts.length; i++) {
            String[] vParts = parts[i].split("\\+");
            if (!vParts[1].equals("v")) {
                continue;
            }

            persons.add(vParts[5] + "+" + vParts[6]);
        }
        return persons;
    }

    @Override public String apply(Annotation annotation, Map<Integer, HashMultimap<Integer, Integer>> children) {

        InverseDigiMorph dm = new InverseDigiMorph();

        int conj = 0;
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        CoreMap sentence = sentences.get(0);

        //

        SemanticGraph semanticGraph = sentence
                .get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);

        IndexedWord node = semanticGraph.getNodeByIndex(conj + 1);
        List<IndexedWord> history = getHistory(semanticGraph, node);
        if (history.size() == 1) {
            return null;
        }
        IndexedWord verb = history.get(1);
        CoreLabel token = sentence.get(CoreAnnotations.TokensAnnotation.class).get(verb.index() - 1);
        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        if (!pos.startsWith("V")) {
            return null;
        }

        // todo: check subject in parse tree
        // todo: check clitics

        String morpho = token.get(DigiMorphAnnotations.MorphoAnnotation.class);
        String[] parts = morpho.split("\\s+");
        TreeSet<String> persons = new TreeSet<>();
        String tempo = null;
        for (int i = 1; i < parts.length; i++) {
            String[] vParts = parts[i].split("\\+");
            if (!vParts[1].equals("v")) {
                continue;
            }

            String modo = vParts[2];
            if (!modo.equals("cong")) {
                continue;
            }

            tempo = vParts[3];
            persons.add(vParts[5] + "+" + vParts[6]);
        }

        IndexedWord next = null;
        if (persons.size() != 1) {
            for (int i = 2; i < history.size(); i++) {
                if (history.get(i).get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("V")) {
                    next = history.get(i);
                    break;
                }
            }
            persons = getPersons(semanticGraph, next, sentence);
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(token.lemma());
        stringBuffer.append("+v+indic+").append(tempo);
        stringBuffer.append("+nil+");

        // Add person
        stringBuffer.append(persons.last());

        String find = stringBuffer.toString();

        System.out.println(find);
        String inverseMorphology = dm.getInverseMorphology(find);

        System.out.println(inverseMorphology);
        System.out.println(morpho);
        System.out.println(tempo);
        System.out.println(persons);

//        System.out.println(annotation.get(UDPipeAnnotations.UDPipeOriginalAnnotation.class));
//        System.out.println(sentence.get(CoreAnnotations.TokensAnnotation.class).get(2)
//                .get(UDPipeAnnotations.FeaturesAnnotation.class));
//        System.out.println(token
//                .get(UDPipeAnnotations.FeaturesAnnotation.class));
//
//        System.out.println(children.get(0).get(verb.index()));
//        System.out.println(children);
//        System.out.println(verb.get(UDPipeAnnotations.FeaturesAnnotation.class));

//        try {
//            System.out.println(JSONOutputter.jsonPrint(annotation));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(getHistory(semanticGraph, node));
//        System.out.println(semanticGraph.getOutEdgesSorted(node));
//        System.out.println(semanticGraph.getIncomingEdgesSorted(node));
//        System.out.println(node);
        return null;
    }
}
