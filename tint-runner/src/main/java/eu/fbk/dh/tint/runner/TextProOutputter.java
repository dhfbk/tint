package eu.fbk.dh.tint.runner;

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

/**
 * <p>Write a subset of our CoreNLP output in CoNLL format.</p>
 * <p>
 * <p>The fields currently output are:</p>
 * <p>
 * <table>
 * <tr>
 * <td>Field Number</td>
 * <td>Field Name</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>ID</td>
 * <td>Token Counter, starting at 1 for each new sentence.</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>FORM</td>
 * <td>Word form or punctuation symbol.</td>
 * </tr>
 * <tr>
 * <td>3</td>
 * <td>LEMMA</td>
 * <td>Lemma of word form, or an underscore if not available.</td>
 * </tr>
 * <tr>
 * <td>4</td>
 * <td>POSTAG</td>
 * <td>Fine-grained part-of-speech tag, or underscore if not available.</td>
 * </tr>
 * <tr>
 * <td>5</td>
 * <td>NER</td>
 * <td>Named Entity tag, or underscore if not available.</td>
 * </tr>
 * <tr>
 * <td>6</td>
 * <td>HEAD</td>
 * <td>Head of the current token, which is either a value of ID or zero ('0').
 * This is underscore if not available.</td>
 * </tr>
 * <tr>
 * <td>7</td>
 * <td>DEPREL</td>
 * <td>Dependency relation to the HEAD, or underscore if not available.</td>
 * </tr>
 * </table>
 *
 * @author Gabor Angeli
 */
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
                    for (IndexedWord token : depTree.vertexListSorted()) {
                        int govIdx = -1;
                        GrammaticalRelation reln = null;
                        HashMap<Integer, String> additionalDeps = new HashMap<>();
                        for (IndexedWord parent : depTree.getParents(token)) {
                            SemanticGraphEdge edge = depTree.getEdge(parent, token);
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
                        String relnName = reln == null ? NULL_PLACEHOLDER : reln.toString();

                        CoreLabel coreLabel = token.backingLabel();
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