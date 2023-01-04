package eu.fbk.dh.tint.simplification;

import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import eu.fbk.dh.tint.readability.DescriptionForm;
import eu.fbk.dh.tint.readability.GlossarioEntry;
import eu.fbk.dh.tint.readability.it.ItalianReadability;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

/**
 * Created by alessio on 19/12/16.
 */

public class SimpleSimAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSimAnnotator.class);
    private FakeSimModel model;
    private static Pattern firstLinePattern = Pattern.compile("1. ([^2]+)");
    private static Pattern firstResPattern = Pattern.compile("1. ([^,]+)");
    private boolean onlyOne = false;
    SkipModel skipModel;

    public SimpleSimAnnotator(String annotatorName, Properties props) {
        Properties globalProperties = props;
        Properties localProperties = PropertiesUtils.dotConvertedProperties(props, annotatorName);
        this.onlyOne = PropertiesUtils.getBoolean(localProperties.getProperty("only_one", "false"), false);
        this.skipModel = SkipModel.getInstance(localProperties.getProperty("skipLemmaFile"));
        this.model = FakeSimModel.getInstance(globalProperties, localProperties, this.skipModel);
    }

    @Override
    public void annotate(Annotation annotation) {

        List<RawSimplification> simplificationList = new ArrayList<>();
        Map<String, List<RawSimplification>> ffs = new HashMap<>();

        int lemmaIndex = 0;
        HashMap<Integer, Integer> lemmaIndexes = new HashMap<>();
        HashMap<Integer, Integer> tokenIndexes = new HashMap<>();
        HashMap<Integer, Integer> endIndexes = new HashMap<>();

//        HashSet<Integer> skipIndexes = new HashSet<>();

        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        StringBuffer lemmaText = new StringBuffer();

        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            lemmaText.append(token.lemma()).append(" ");
            lemmaIndexes.put(lemmaText.length(), lemmaIndex);
            tokenIndexes.put(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), lemmaIndex);
            endIndexes.put(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
                    token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));

//            if (skipModel.getSkipList().contains(token.lemma())) {
//                skipIndexes.add(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class));
//            }

            lemmaIndex++;
        }

        HashMap<String, GlossarioEntry> glossario = model.getGlossario();
        Map<String, Integer> frequencies = model.getFrequencies();
        List<String> glossarioKeys = new ArrayList<>(glossario.keySet());
        TreeMap<Integer, DescriptionForm> forms = new TreeMap<>();

        HashMultimap<String, String> ffReverse = HashMultimap.create();
        HashMultimap<Integer, String> ffOffsets = HashMultimap.create();

        for (String lang : model.getFf().keySet()) {
            ffs.put(lang, new ArrayList<>());
            for (String word : model.getFf().get(lang)) {
                ffReverse.put(word, lang);
            }
        }

        for (String form : ffReverse.keySet()) {
            if (form.length() < 4) {
                continue;
            }

            List<Integer> allOccurrences = ItalianReadability.findAllOccurrences(text, form);
            for (Integer occurrence : allOccurrences) {
                ffOffsets.putAll(occurrence, ffReverse.get(form));
            }

        }

        for (Integer offset : ffOffsets.keySet()) {

            // tokenIndex can be null because the offset can be inside a word (sic!)
            Integer tokenIndex = tokenIndexes.get(offset);
            if (tokenIndex == null) {
                continue;
            }

            CoreLabel token = annotation.get(CoreAnnotations.TokensAnnotation.class).get(tokenIndex);
            token.set(SimpaticoAnnotations.FfAnnotation.class, new ArrayList<>(ffOffsets.get(offset)));

            RawSimplification simplification = new RawSimplification(
                    token.beginPosition(),
                    token.endPosition(),
                    "[no simplification]"
            );

            for (String lang : ffOffsets.get(offset)) {
                ffs.get(lang).add(simplification);
            }

        }

        annotation.set(SimpaticoAnnotations.FfsAnnotation.class, ffs);

        for (String form : glossarioKeys) {

            if (form.length() < 4) {
                continue;
            }

            int numberOfTokens = form.split("\\s+").length;
            List<Integer> allOccurrences = ItalianReadability.findAllOccurrences(text, form);

            for (Integer occurrence : allOccurrences) {
                ItalianReadability
                        .addDescriptionForm(form, tokenIndexes, occurrence, numberOfTokens, forms, annotation,
                                glossario);
            }
        }

        formsLoop:
        for (Integer integer : forms.keySet()) {
            Integer end = endIndexes.get(integer);
            if (end == null) {
                continue;
            }

            Integer tokenIndex = tokenIndexes.get(integer);
            CoreLabel token = annotation.get(CoreAnnotations.TokensAnnotation.class).get(tokenIndex);

//            if (token.word().equals("socio")) {
//                continue;
//            }

            //firstResPattern
            String simplifiedVersion = forms.get(integer).getDescription().getDescription();
            Matcher matcher;
            if (onlyOne) {
                matcher = firstResPattern.matcher(simplifiedVersion);
            } else {
                matcher = firstLinePattern.matcher(simplifiedVersion);
            }
            if (matcher.find()) {
                simplifiedVersion = matcher.group(1).trim();
            }

            String[] strings = simplifiedVersion.split(",");
            LinkedHashMap<String, Integer> results = new LinkedHashMap<>();
            for (String string : strings) {
                string = string.trim();
                Integer frequency = frequencies.getOrDefault(string.toLowerCase(), 1);
                results.put(string, frequency);
            }

            Map<String, Integer> sorted = results
                    .entrySet()
                    .stream()
                    .sorted(reverseOrder(comparingByValue()))
                    .collect(
                            Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                                    LinkedHashMap::new));
//            System.out.println(simplifiedVersion);
//            System.out.println(sorted);
//            LinkedHashSet<String> results = new LinkedHashSet<>();
//            for (String string : strings) {
//                if (string.contains(" ")) {
//                    continue;
//                }
//                results.add(string);
//            }
//            for (String string : strings) {
//                results.add(string);
//            }

            StringBuffer buffer = new StringBuffer();
            for (String result : sorted.keySet()) {
                buffer.append(" ").append(result).append(",");
            }
            simplifiedVersion = buffer.delete(buffer.length() - 1, buffer.length()).toString().trim();
            simplifiedVersion = simplifiedVersion.replaceAll("\\s+", " ");

//            System.out.println(String.format("Simplified version [%s]: %s", token.toString(), simplifiedVersion));

            for (String skipTerm : skipModel.getSkipList()) {
                if (simplifiedVersion.startsWith(skipTerm)) {
//                    System.out.println("Skipping " + skipTerm);
                    continue formsLoop;
                }
            }
            for (String replaceTerm : skipModel.getReplaceList().keySet()) {
                if (simplifiedVersion.startsWith(replaceTerm)) {
                    simplifiedVersion = skipModel.getReplaceList().get(replaceTerm);
                    break;
                }
            }

            RawSimplification simplification = new RawSimplification(
                    token.beginPosition(),
                    token.endPosition(),
                    simplifiedVersion
            );
            simplification.setOriginalValue(token.word());
            simplificationList.add(simplification);
            token.set(SimpaticoAnnotations.SimplifiedAnnotation.class, simplifiedVersion);
        }

        annotation.set(SimpaticoAnnotations.SimplificationsAnnotation.class, simplificationList);
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.emptySet();
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class,
                CoreAnnotations.LemmaAnnotation.class,
                CoreAnnotations.SentencesAnnotation.class
        )));

    }
}
