package eu.fbk.dh.tint.runner.outputters;

import com.google.gson.*;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.ie.machinereading.structure.Span;
import edu.stanford.nlp.ie.util.RelationTriple;
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
import eu.fbk.dh.tint.json.AnnotationExclusionStrategy;
import eu.fbk.dh.tint.json.JSONLabel;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Output an Annotation to JSON.
 *
 * @author Alessio Palmero Aprosio
 */
@SuppressWarnings("unused")
public class JSONOutputter extends AnnotationOutputter {

    private final ThreadLocal<Annotation> annotationThreadLocal = new ThreadLocal<>();
    GsonBuilder gsonBuilder = new GsonBuilder();

    static private void add(Gson gson, JsonObject jsonObject, TypesafeMap annotation) {
        for (Class<?> myClass : annotation.keySet()) {
            Object o = annotation.get((Class) myClass);
            if (o != null) {
                if (myClass.isAnnotationPresent(JSONLabel.class)) {
                    JSONLabel JsonAnnotation = myClass.getAnnotation(JSONLabel.class);
                    String name = JsonAnnotation.value();
                    if (name != null && name.length() > 0) {
                        try {
                            jsonObject.add(name, gson.toJsonTree(o));
                        } catch (Exception e) {
                            // ignored
                        }
                    }

                    Class<?>[] serializerClasses = JsonAnnotation.serializer();
                    for (Class<?> serializerClass : serializerClasses) {
                        if (JsonSerializer.class.isAssignableFrom(serializerClass)) {
                            // do stuff
                        }
                    }

                }
            }
        }
    }

    class SpanSerializer implements JsonSerializer<Span> {

        @Override public JsonElement serialize(Span span, Type type,
                JsonSerializationContext jsonSerializationContext) {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(span.start());
            jsonArray.add(span.end());
            return jsonArray;
        }
    }

    class SemanticGraphSerializer implements JsonSerializer<SemanticGraph> {

        @Override public JsonElement serialize(SemanticGraph semanticGraph, Type type,
                JsonSerializationContext jsonSerializationContext) {
            JsonArray jsonArray = new JsonArray();

            for (IndexedWord root : semanticGraph.getRoots()) {
                JsonObject object = new JsonObject();
                object.addProperty("dep", "ROOT");
                object.addProperty("governor", 0);
                object.addProperty("governorGloss", "ROOT");
                object.addProperty("dependent", root.index());
                object.addProperty("dependentGloss", root.word());
                jsonArray.add(object);
            }
            for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
                JsonObject object = new JsonObject();
                object.addProperty("dep", edge.getRelation().toString());
                object.addProperty("governor", edge.getGovernor().index());
                object.addProperty("governorGloss", edge.getGovernor().word());
                object.addProperty("dependent", edge.getDependent().index());
                object.addProperty("dependentGloss", edge.getDependent().word());
                jsonArray.add(object);
            }
            return jsonArray;
        }
    }

    class RelationTripleSerializer implements JsonSerializer<RelationTriple> {

        @Override public JsonElement serialize(RelationTriple triple, Type type,
                JsonSerializationContext jsonSerializationContext) {
            JsonObject ieObject = new JsonObject();
            ieObject.addProperty("subject", triple.subjectGloss());
            ieObject.add("subjectSpan", jsonSerializationContext.serialize(Span.fromPair(triple.subjectTokenSpan())));
            ieObject.addProperty("relation", triple.relationGloss());
            ieObject.add("relationSpan", jsonSerializationContext.serialize(Span.fromPair(triple.relationTokenSpan())));
            ieObject.addProperty("object", triple.objectGloss());
            ieObject.add("objectSpan", jsonSerializationContext.serialize(Span.fromPair(triple.objectTokenSpan())));
            return ieObject;
        }
    }

    class TimexSerializer implements JsonSerializer<Timex> {

        @Override public JsonElement serialize(Timex time, Type type,
                JsonSerializationContext jsonSerializationContext) {
            JsonObject timexObj = new JsonObject();
            timexObj.addProperty("tid", time.tid());
            timexObj.addProperty("type", time.timexType());
            timexObj.addProperty("value", time.value());
            timexObj.addProperty("altValue", time.altVal());
            return timexObj;
        }
    }

    class CorefChainSerializer implements JsonSerializer<CorefChain> {

        @Override public JsonElement serialize(CorefChain chain, Type type,
                JsonSerializationContext jsonSerializationContext) {
            CorefChain.CorefMention representative = chain.getRepresentativeMention();
            JsonArray chainArray = new JsonArray();
            for (CorefChain.CorefMention mention : chain.getMentionsInTextualOrder()) {
                JsonObject mentionObj = new JsonObject();
                mentionObj.addProperty("id", mention.mentionID);
                mentionObj.addProperty("text", Sentence
                        .listToOriginalTextString(
                                annotationThreadLocal.get().get(CoreAnnotations.SentencesAnnotation.class)
                                        .get(mention.sentNum - 1)
                                        .get(CoreAnnotations.TokensAnnotation.class)
                                        .subList(mention.startIndex - 1,
                                                mention.endIndex - 1)).trim());
                mentionObj.add("type", jsonSerializationContext.serialize(mention.mentionType));
                mentionObj.add("number", jsonSerializationContext.serialize(mention.number));
                mentionObj.add("gender", jsonSerializationContext.serialize(mention.gender));
                mentionObj.add("animacy", jsonSerializationContext.serialize(mention.animacy));
                mentionObj.addProperty("startIndex", mention.startIndex);
                mentionObj.addProperty("endIndex", mention.endIndex);
                mentionObj.addProperty("sentNum", mention.sentNum);
                mentionObj.add("position", jsonSerializationContext.serialize(mention.position.elems()));
                mentionObj.addProperty("isRepresentativeMention", mention == representative);
                chainArray.add(mentionObj);
            }
            return chainArray;
        }
    }

    public JSONOutputter(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
    }

    public JSONOutputter() {
        this.gsonBuilder = new GsonBuilder();
    }

    @Override
    public void print(Annotation doc, OutputStream target, Options options) throws IOException {

        if (options.pretty) {
            gsonBuilder.setPrettyPrinting();
        }
        gsonBuilder.registerTypeAdapter(SemanticGraph.class, new SemanticGraphSerializer());
        gsonBuilder.registerTypeAdapter(Span.class, new SpanSerializer());
        gsonBuilder.registerTypeAdapter(RelationTriple.class, new RelationTripleSerializer());
        gsonBuilder.registerTypeAdapter(Timex.class, new TimexSerializer());
        gsonBuilder.registerTypeAdapter(CorefChain.class, new CorefChainSerializer());
        gsonBuilder.serializeSpecialFloatingPointValues();
        gsonBuilder.setExclusionStrategies(new AnnotationExclusionStrategy());
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
            addSentences(gson, jsonObject, doc.get(CoreAnnotations.SentencesAnnotation.class), options);
        }

        // Add coref values
        annotationThreadLocal.set(doc);
        jsonObject.add("corefs", gson.toJsonTree(doc.get(CorefCoreAnnotations.CorefChainAnnotation.class)));

//        System.out.println(gson.toJson(jsonObject));

        Writer w = new OutputStreamWriter(target);
        w.write(gson.toJson(jsonObject));
        w.flush();
    }

    private static void addSentences(Gson gson, JsonObject jsonObject, List<CoreMap> sentences,
            Options options) {
        JsonArray jsonSentenceArray = new JsonArray();
        for (CoreMap sentence : sentences) {
            JsonObject sentenceObj = new JsonObject();

            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            sentenceObj.addProperty("id", sentence.get(CoreAnnotations.SentenceIDAnnotation.class));
            sentenceObj.addProperty("index", sentence.get(CoreAnnotations.SentenceIndexAnnotation.class));
            sentenceObj.addProperty("line", sentence.get(CoreAnnotations.LineNumberAnnotation.class));
            sentenceObj.addProperty("characterOffsetBegin", tokens.get(0).beginPosition());
            sentenceObj.addProperty("characterOffsetEnd", tokens.get(tokens.size() - 1).endPosition());
            sentenceObj.addProperty("text", sentence.get(CoreAnnotations.TextAnnotation.class));

            // Dependencies
            sentenceObj.add("basic-dependencies",
                    gson.toJsonTree(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class)));
            sentenceObj.add("collapsed-dependencies", gson.toJsonTree(
                    sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class)));
            sentenceObj.add("collapsed-ccprocessed-dependencies", gson.toJsonTree(
                    sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class)));

            // Constituents
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            StringWriter treeStrWriter = new StringWriter();
            TreePrint treePrinter = options.constituentTreePrinter;
            if (treePrinter == AnnotationOutputter.DEFAULT_CONSTITUENT_TREE_PRINTER) {
                // note the '==' -- we're overwriting the default, but only if it was not explicitly set otherwise
                treePrinter = new TreePrint("oneline");
            }
            treePrinter.printTree(tree,
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
            sentenceObj.add("openie", gson.toJsonTree(sentence
                    .get(NaturalLogicAnnotations.RelationTriplesAnnotation.class)));

            // Tokens
            if (sentence.get(CoreAnnotations.TokensAnnotation.class) != null) {
                addTokens(gson, sentenceObj, sentence.get(CoreAnnotations.TokensAnnotation.class));
            }

            add(gson, sentenceObj, sentence);

            jsonSentenceArray.add(sentenceObj);
        }
        jsonObject.add("sentences", jsonSentenceArray);
    }

    private static void addTokens(Gson gson, JsonObject sentenceObj, List<CoreLabel> tokens) {
        JsonArray jsonTokenArray = new JsonArray();
        for (CoreLabel token : tokens) {
            JsonObject tokenObj = new JsonObject();

            tokenObj.addProperty("index", token.index());
            tokenObj.addProperty("word", token.word());
            tokenObj.addProperty("originalText", token.originalText());
            tokenObj.addProperty("lemma", token.lemma());
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
            tokenObj.add("timex", gson.toJsonTree(token.get(TimeAnnotations.TimexAnnotation.class)));

            add(gson, tokenObj, token);

            jsonTokenArray.add(tokenObj);
        }

        sentenceObj.add("tokens", jsonTokenArray);
    }

    public static String jsonPrint(GsonBuilder gsonBuilder, Annotation annotation) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new JSONOutputter(gsonBuilder).print(annotation, outputStream);
        return new String(outputStream.toByteArray(), "UTF-8");
    }

    public static void jsonPrint(GsonBuilder gsonBuilder, Annotation annotation, OutputStream os) throws IOException {
        new JSONOutputter(gsonBuilder).print(annotation, os);
    }

    public static void jsonPrint(GsonBuilder gsonBuilder, Annotation annotation, OutputStream os,
            StanfordCoreNLP pipeline) throws IOException {
        new JSONOutputter(gsonBuilder).print(annotation, os, pipeline);
    }

    public static void jsonPrint(GsonBuilder gsonBuilder, Annotation annotation, OutputStream os, Options options)
            throws IOException {
        new JSONOutputter(gsonBuilder).print(annotation, os, options);
    }

    public static String jsonPrint(Annotation annotation) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new JSONOutputter().print(annotation, outputStream);
        return new String(outputStream.toByteArray(), "UTF-8");
    }

    public static void jsonPrint(Annotation annotation, OutputStream os) throws IOException {
        new JSONOutputter().print(annotation, os);
    }

    public static void jsonPrint(Annotation annotation, OutputStream os,
            StanfordCoreNLP pipeline) throws IOException {
        new JSONOutputter().print(annotation, os, pipeline);
    }

    public static void jsonPrint(Annotation annotation, OutputStream os, Options options)
            throws IOException {
        new JSONOutputter().print(annotation, os, options);
    }

}
