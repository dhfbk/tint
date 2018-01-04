package eu.fbk.dh.tint.readability;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import eu.fbk.dh.tint.verb.VerbMultiToken;
import eu.fbk.utils.core.FrequencyHashSet;

import java.util.*;

/**
 * Created by alessio on 09/03/17.
 */

public class SemanticGraphUtils {

    public static void smartRemoveEdge(SemanticGraph semanticGraph, IndexedWord indexedWord) {
        Set<IndexedWord> parents = semanticGraph.getParents(indexedWord);
//        if (parents)
    }

    public static Integer getHead(VerbMultiToken verb, SemanticGraph semanticGraph) {
        FrequencyHashSet<Integer> frequencies = new FrequencyHashSet<>();
        Set<Integer> indexes = new HashSet<>();

        for (CoreLabel token : verb.getTokens()) {
            int index = token.index();
            indexes.add(index);
            try {
                IndexedWord node = semanticGraph.getNodeByIndex(index);
                frequencies.add(index);
                List<IndexedWord> pathToRoot = semanticGraph.getPathToRoot(node);
                for (IndexedWord indexedWord : pathToRoot) {
                    frequencies.add(indexedWord.index());
                }

            } catch (Exception e) {
                // ignored
//                System.out.println("ERR: no node for index " + index);
//                System.out.println(token);
            }
        }

        Set<Integer> keys = new HashSet<>();
        for (Integer key : frequencies.keySet()) {
            keys.add(key);
        }

        for (Integer index : keys) {
            if (!indexes.contains(index)) {
                frequencies.remove(index);
            }
        }

        return frequencies.mostFrequent();
    }

    public static Map<Integer, String> getParent(VerbMultiToken verb, SemanticGraph semanticGraph) {
        Map<Integer, String> parents = new HashMap<>();
        Set<Integer> removeIndexes = new HashSet<>();

        for (CoreLabel token : verb.getTokens()) {
            int index = token.index();
            removeIndexes.add(index);
            try {
                IndexedWord node = semanticGraph.getNodeByIndex(index);
                List<SemanticGraphEdge> incomingEdgesSorted = semanticGraph
                        .getIncomingEdgesSorted(node);
//                System.out.println(token + " --- " + incomingEdgesSorted);
                for (SemanticGraphEdge edge : incomingEdgesSorted) {
                    int parentIndex = edge.getGovernor().index();
                    parents.put(parentIndex, edge.getRelation().getShortName());
                }
            } catch (Exception e) {
                // ignored
//                System.out.println("ERR: no node for index " + index);
//                System.out.println(token);
            }
        }

        for (Integer removeIndex : removeIndexes) {
            parents.remove(removeIndex);
        }

        return parents;
    }
}
