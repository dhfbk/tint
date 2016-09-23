package eu.fbk.dh.tint.runner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.StringOutputStream;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.TypesafeMap;
import eu.fbk.dh.tint.digimorph.annotator.DigiMorphAnnotations;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Output an Annotation to JSON.
 *
 * @author Alessio Palmero Aprosio
 */
@SuppressWarnings("unused")
public class JSONOutputter extends AnnotationOutputter {

    static private void add(Gson gson, JsonObject jsonObject, TypesafeMap annotation) {
        for (Class<?> myClass : annotation.keySet()) {
            Object o = annotation.get((Class) myClass);
            if (o != null && o instanceof JSONable) {
                String name = ((JSONable) o).getName();
                jsonObject.add(name, ((JSONable) o).getJson(gson));
            }
        }
    }

    static private void add(Gson gson, String label, JsonObject jsonObject, SemanticGraph graph) {
        if (graph == null) {
            return;
        }

        JsonArray jsonArray = new JsonArray();

        for (IndexedWord root : graph.getRoots()) {
            JsonObject object = new JsonObject();
            object.addProperty("dep", "ROOT");
            object.addProperty("governor", 0);
            object.addProperty("governorGloss", "ROOT");
            object.addProperty("dependent", root.index());
            object.addProperty("dependentGloss", root.word());
            jsonArray.add(object);
        }
        for (SemanticGraphEdge edge : graph.edgeListSorted()) {
            JsonObject object = new JsonObject();
            object.addProperty("dep", edge.getRelation().toString());
            object.addProperty("governor", edge.getGovernor().index());
            object.addProperty("governorGloss", edge.getGovernor().word());
            object.addProperty("dependent", edge.getDependent().index());
            object.addProperty("dependentGloss", edge.getDependent().word());
            jsonArray.add(object);
        }

        jsonObject.add(label, jsonArray);
    }

    @Override
    public void print(Annotation doc, OutputStream target, Options options) throws IOException {

        GsonBuilder gsonBuilder = new GsonBuilder();
        if (options.pretty) {
            gsonBuilder.setPrettyPrinting();
        }
        Gson gson = gsonBuilder.create();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("docId", doc.get(CoreAnnotations.DocIDAnnotation.class));
        jsonObject.addProperty("docDate", doc.get(CoreAnnotations.DocDateAnnotation.class));
        jsonObject.addProperty("docSourceType", doc.get(CoreAnnotations.DocSourceTypeAnnotation.class));
        jsonObject.addProperty("docType", doc.get(CoreAnnotations.DocTypeAnnotation.class));
        jsonObject.addProperty("author", doc.get(CoreAnnotations.AuthorAnnotation.class));
        jsonObject.addProperty("location", doc.get(CoreAnnotations.LocationAnnotation.class));
        if (options.includeText) {
            jsonObject.addProperty("text", doc.get(CoreAnnotations.TextAnnotation.class));
        }

        add(gson, jsonObject, doc);

        // Sentences
        if (doc.get(CoreAnnotations.SentencesAnnotation.class) != null) {
            JsonArray jsonSentenceArray = new JsonArray();
            for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
                JsonObject sentenceObj = new JsonObject();

                sentenceObj.addProperty("id", sentence.get(CoreAnnotations.SentenceIDAnnotation.class));
                sentenceObj.addProperty("index", sentence.get(CoreAnnotations.SentenceIndexAnnotation.class));
                sentenceObj.addProperty("line", sentence.get(CoreAnnotations.LineNumberAnnotation.class));

                // Dependencies
                add(gson, "basic-dependencies", sentenceObj,
                        sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class));
                add(gson, "collapsed-dependencies", sentenceObj,
                        sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class));
                add(gson, "collapsed-ccprocessed-dependencies", sentenceObj,
                        sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class));

                // Constituents
                StringWriter treeStrWriter = new StringWriter();
                TreePrint treePrinter = options.constituentTreePrinter;
                if (treePrinter == AnnotationOutputter.DEFAULT_CONSTITUENT_TREE_PRINTER) {
                    // note the '==' -- we're overwriting the default, but only if it was not explicitly set otherwise
                    treePrinter = new TreePrint("oneline");
                }
                treePrinter.printTree(sentence.get(TreeCoreAnnotations.TreeAnnotation.class),
                        new PrintWriter(treeStrWriter, true));
                sentenceObj.addProperty("parse", treeStrWriter.toString().trim());

                // Sentiment
                Tree sentimentTree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                if (sentimentTree != null) {
                    int sentiment = RNNCoreAnnotations.getPredictedClass(sentimentTree);
                    String sentimentClass = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                    sentenceObj.addProperty("sentimentValue", Integer.toString(sentiment));
                    sentenceObj.addProperty("sentiment", sentimentClass.replaceAll("\\s+", ""));
                }

                // OpenIE
                Collection<RelationTriple> openIETriples = sentence
                        .get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
                if (openIETriples != null) {
                    JsonArray jsonArray = new JsonArray();
                    for (RelationTriple triple : openIETriples) {
                        JsonObject ieObject = new JsonObject();
                        ieObject.addProperty("subject", triple.subjectGloss());
                        ieObject.add("subjectSpan", gson.toJsonTree(Span.fromPair(triple.subjectTokenSpan())));
                        ieObject.addProperty("relation", triple.relationGloss());
                        ieObject.add("relationSpan", gson.toJsonTree(Span.fromPair(triple.relationTokenSpan())));
                        ieObject.addProperty("object", triple.objectGloss());
                        ieObject.add("objectSpan", gson.toJsonTree(Span.fromPair(triple.objectTokenSpan())));
                        jsonArray.add(ieObject);
                    }
                    sentenceObj.add("openie", jsonArray);
                }

                // Tokens
                if (sentence.get(CoreAnnotations.TokensAnnotation.class) != null) {
                    JsonArray jsonTokenArray = new JsonArray();
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        JsonObject tokenObj = new JsonObject();

                        tokenObj.addProperty("index", token.index());
                        tokenObj.addProperty("word", token.word());
                        tokenObj.addProperty("originalText", token.originalText());
                        tokenObj.addProperty("lemma", token.lemma());
                        tokenObj.addProperty("full_morpho", token.get(DigiMorphAnnotations.MorphoAnnotation.class));
                        tokenObj.addProperty("characterOffsetBegin", token.beginPosition());
                        tokenObj.addProperty("characterOffsetEnd", token.endPosition());
                        tokenObj.addProperty("pos", token.tag());
                        tokenObj.addProperty("ner", token.ner());
                        tokenObj.addProperty("normalizedNER",
                                token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class));
                        tokenObj.addProperty("speaker", token.get(CoreAnnotations.SpeakerAnnotation.class));
                        tokenObj.addProperty("truecase", token.get(CoreAnnotations.TrueCaseAnnotation.class));
                        tokenObj.addProperty("truecaseText", token.get(CoreAnnotations.TrueCaseTextAnnotation.class));
                        tokenObj.addProperty("before", token.get(CoreAnnotations.BeforeAnnotation.class));
                        tokenObj.addProperty("after", token.get(CoreAnnotations.AfterAnnotation.class));

                        // Timex
                        Timex time = token.get(TimeAnnotations.TimexAnnotation.class);
                        if (time != null) {
                            JsonObject timexObj = new JsonObject();
                            timexObj.addProperty("tid", time.tid());
                            timexObj.addProperty("type", time.timexType());
                            timexObj.addProperty("value", time.value());
                            timexObj.addProperty("altValue", time.altVal());
                            tokenObj.add("timex", timexObj);
                        }

                        add(gson, tokenObj, token);

                        jsonTokenArray.add(tokenObj);
                    }

                    sentenceObj.add("tokens", jsonTokenArray);
                }

                add(gson, sentenceObj, sentence);

                jsonSentenceArray.add(sentenceObj);
            }
            jsonObject.add("sentences", jsonSentenceArray);
        }

        // Add coref values
        if (doc.get(CorefCoreAnnotations.CorefChainAnnotation.class) != null) {
            Map<Integer, CorefChain> corefChains =
                    doc.get(CorefCoreAnnotations.CorefChainAnnotation.class);
            if (corefChains != null) {
                JsonObject corefChainsJson = new JsonObject();
                for (CorefChain chain : corefChains.values()) {
                    CorefChain.CorefMention representative = chain.getRepresentativeMention();
                    String key = Integer.toString(chain.getChainID());
                    JsonArray chainArray = new JsonArray();
                    for (CorefChain.CorefMention mention : chain.getMentionsInTextualOrder()) {
                        JsonObject mentionObj = new JsonObject();
                        mentionObj.addProperty("id", mention.mentionID);
                        mentionObj.addProperty("text", Sentence
                                .listToOriginalTextString(
                                        doc.get(CoreAnnotations.SentencesAnnotation.class)
                                                .get(mention.sentNum - 1)
                                                .get(CoreAnnotations.TokensAnnotation.class)
                                                .subList(mention.startIndex - 1,
                                                        mention.endIndex - 1)).trim());
                        mentionObj.addProperty("type", gson.toJson(mention.mentionType));
                        mentionObj.addProperty("number", gson.toJson(mention.number));
                        mentionObj.addProperty("gender", gson.toJson(mention.gender));
                        mentionObj.addProperty("animacy", gson.toJson(mention.animacy));
                        mentionObj.addProperty("startIndex", mention.startIndex);
                        mentionObj.addProperty("endIndex", mention.endIndex);
                        mentionObj.addProperty("sentNum", mention.sentNum);
                        mentionObj.addProperty("position", gson.toJson(
                                Arrays.stream(mention.position.elems()).boxed().collect(Collectors.toList())));
                        mentionObj.addProperty("isRepresentativeMention", mention == representative);
                        chainArray.add(mentionObj);
                    }
                    corefChainsJson.add(key, chainArray);
                }

                jsonObject.add("corefs", corefChainsJson);

            }
        }

        Writer w = new OutputStreamWriter(target, "UTF-8");
        w.write(gson.toJson(jsonObject));
        w.flush();
    }

    public static String jsonPrint(Annotation annotation) throws IOException {
        StringOutputStream os = new StringOutputStream();
        new JSONOutputter().print(annotation, os);
        return os.toString();
    }

    public static void jsonPrint(Annotation annotation, OutputStream os) throws IOException {
        new JSONOutputter().print(annotation, os);
    }

    public static void jsonPrint(Annotation annotation, OutputStream os, StanfordCoreNLP pipeline) throws IOException {
        new JSONOutputter().print(annotation, os, pipeline);
    }

    public static void jsonPrint(Annotation annotation, OutputStream os, Options options) throws IOException {
        new JSONOutputter().print(annotation, os, options);
    }

}
