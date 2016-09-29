package eu.fbk.dh.tint.runner.outputters;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationOutputter;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TextProOutputter extends AnnotationOutputter {

    private static final String NULL_PLACEHOLDER = "_";
    private static final String ENTITY_NULL_PLACEHOLDER = "O";

    public TextProOutputter() {
    }

    private static String orNull(String in) {
        return orNull(in, NULL_PLACEHOLDER);
    }

    private static String orNull(String in, String ph) {
        if (in == null) {
            return ph;
        } else {
            return in;
        }
    }

    /**
     * Produce a line of the CoNLL output.
     */
    private static String line(int index, CoreLabel token, int head, String deprel) {
        return line(index, token, head, deprel, 0);
    }

    private static String line(int index, CoreLabel token, int head, String deprel, int lastIndex) {
        ArrayList<String> fields = new ArrayList<>(16);

        fields.add(Integer.toString(index)); // 1
        fields.add(Integer.toString(index - lastIndex)); // 1
        fields.add(orNull(token.word()));    // 2
        fields.add(orNull(token.lemma()));   // 3
        fields.add(orNull(token.tag()));     // 4
        fields.add(orNull(token.ner()));     // 5
        if (head >= 0) {
            fields.add(Integer.toString(Math.max(0, head - lastIndex)));  // 6
            fields.add(deprel);                  // 7
        } else {
            fields.add(NULL_PLACEHOLDER);
            fields.add(NULL_PLACEHOLDER);
        }

        return StringUtils.join(fields, "\t");
    }

    @Override
    public void print(Annotation doc, OutputStream target, Options options) throws IOException {
        PrintWriter writer = new PrintWriter(IOUtils.encodedOutputStreamWriter(target, options.encoding));

        List<String> fields = new ArrayList<>();
        fields.add("tokenid");
        fields.add("parserid");
        fields.add("token");
        fields.add("lemma");
        fields.add("pos");
        fields.add("entity");
        fields.add("head");
        fields.add("deprel");
        writer.println("# FIELDS: " + StringUtils.join(fields, "\t"));

        // vv A bunch of nonsense to get tokens vv
        int lastIndex = 0;
        if (doc.get(CoreAnnotations.SentencesAnnotation.class) != null) {
            for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
                if (sentence.get(CoreAnnotations.TokensAnnotation.class) != null) {
                    SemanticGraph depTree = sentence
                            .get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
                    Integer index = 0;

                    List tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                    if (depTree != null) {
                        tokens = depTree.vertexListSorted();
                    }

                    int govIdx = -1;
                    String relnName = null;

                    for (int i = 0; i < tokens.size(); i++) {
                        Object token = tokens.get(i);
                        CoreLabel coreLabel;

                        if (token instanceof IndexedWord) {
                            IndexedWord thisToken = (IndexedWord) token;
                            govIdx = -1;
                            GrammaticalRelation reln = null;
                            HashMap<Integer, String> additionalDeps = new HashMap<>();
                            for (IndexedWord parent : depTree.getParents(thisToken)) {
                                SemanticGraphEdge edge = depTree.getEdge(parent, thisToken);
                                if (govIdx == -1 && !edge.isExtra()) {
                                    govIdx = parent.index();
                                    reln = edge.getRelation();
                                } else {
                                    additionalDeps.put(parent.index(), edge.getRelation().toString());
                                }
                            }
                            if (govIdx == -1) {
                                govIdx = 0;
                                reln = GrammaticalRelation.ROOT;
                            }
                            relnName = reln == null ? NULL_PLACEHOLDER : reln.toString();
                            coreLabel = thisToken.backingLabel();
                        } else {
                            coreLabel = (CoreLabel) token;
                        }

                        index = coreLabel.get(CoreAnnotations.IndexAnnotation.class);
                        writer.print(line(index, coreLabel, govIdx, relnName, lastIndex));
                        writer.println();
                    }
                    lastIndex = index;
                    writer.println();
                }
            }
        }
        writer.flush();
    }

    public static void tpPrint(Annotation annotation, OutputStream os) throws IOException {
        new TextProOutputter().print(annotation, os);
    }

    public static void tpPrint(Annotation annotation, OutputStream os, StanfordCoreNLP pipeline) throws IOException {
        new TextProOutputter().print(annotation, os, pipeline);
    }

    public static void tpPrint(Annotation annotation, OutputStream os, Options options) throws IOException {
        new TextProOutputter().print(annotation, os, options);
    }

}