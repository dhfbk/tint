package eu.fbk.dh.tint.simplifier;

import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;
import eu.fbk.dh.tint.simplifier.rules.ReplaceSubordinateRule;
import eu.fbk.dh.tint.simplifier.rules.SimplificationRule;

import java.util.*;

/**
 * Created by alessio on 14/02/17.
 */

public class Simplifier {

    public static void main(String[] args) {
        String sentenceText;
        sentenceText = "Per gli interventi di seguito descritti, la cui autorizzazione può risultare di competenza dei Comuni o delle CTC in relazione alla tipologia ed alla localizzazione dell'intervento, si indicano i seguenti elaborati, precisando che essi sono orientativi e che comunque devono mostrare chiaramente dove si interviene e come si interviene.";
        sentenceText = "Il mondo, precisando che si tratta della Terra, è molto bello.";
        sentenceText = "In particolare, andranno rilevati e descritti tutti gli elementi di criticità paesaggistica, insiti nel progetto, e andranno messi in relazione a quanto è stato operato, per eliminare o mitigare tali criticità (impatti), garantendo così un migliore inserimento paesaggistico dell'intervento.";
        sentenceText = "In funzione della tipologia dell'opera, oggetto di richiesta di autorizzazione, sono previste due forme diverse di relazione paesaggistica, denominate rispettivamente:";
        sentenceText = "Sebbene non sappia l'inglese, si è fatto capire dai turisti.";
//        sentenceText = "Io cancello il gesso dalla lavagna.";

        try {

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, udpipe, ita_morpho, ita_lemma, ita_comp_morpho");
            pipeline.setProperty("customAnnotatorClass.udpipe", "eu.fbk.dh.fcw.udpipe.api.UDPipeAnnotator");
            pipeline.setProperty("customAnnotatorClass.ita_comp_morpho", "eu.fbk.dh.tint.digimorph.annotator.DigiCompMorphAnnotator");
            pipeline.setProperty("udpipe.server", "gardner");
            pipeline.setProperty("udpipe.port", "50020");
            pipeline.setProperty("udpipe.keepOriginal", "1");
            pipeline.load();

            Annotation annotation = pipeline.runRaw(sentenceText);
            System.out.println(JSONOutputter.jsonPrint(annotation));

            Map<Integer, HashMultimap<Integer, Integer>> children = new HashMap<>();
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            for (int sentIndex = 0; sentIndex < sentences.size(); sentIndex++) {
                CoreMap sentence = sentences.get(sentIndex);

                children.put(sentIndex, HashMultimap.create());

                SemanticGraph semanticGraph = sentence
                        .get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
                Collection<IndexedWord> rootNodes = semanticGraph.getRoots();
                if (rootNodes.isEmpty()) {
                    continue;
                }

                for (IndexedWord root : rootNodes) {
                    Set<Integer> stack = new HashSet<>();
                    Set<IndexedWord> used = new HashSet<>();
                    addChildren(children.get(sentIndex), stack, root, semanticGraph, used);
                }
            }

            SimplificationRule rule;
            String output;

            rule = new ReplaceSubordinateRule();
            output = rule.apply(annotation, children);

            System.out.println(output);

//            rule = new DenominatiSplittingRule();
//            output = rule.apply(annotation, children);
//
//            System.out.println(output);
//
//            rule = new GarantendoSplittingRule();
//            output = rule.apply(annotation, children);
//
//            System.out.println(output);
//
//            rule = new GarantendoSplittingRule();
//            output = rule.apply(annotation, children);
//
//            System.out.println(output);
//
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
