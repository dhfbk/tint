package eu.fbk.dh.tint.upos;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import eu.fbk.utils.corenlp.CustomAnnotations;

import java.util.*;

public class UPosAnnotator implements Annotator {

    static Map<String, String> uposMap = new HashMap<>();
    static String DEFAULT_UPOS = "X";

    static {
        uposMap.put("A", "ADJ");
        uposMap.put("AP", "DET");
        uposMap.put("B", "ADV");
        uposMap.put("BN", "ADV");
        uposMap.put("CC", "CCONJ");
        uposMap.put("CS", "SCONJ");
        uposMap.put("DD", "DET");
        uposMap.put("DE", "DET");
        uposMap.put("DI", "DET");
        uposMap.put("DQ", "DET");
        uposMap.put("DR", "DET");
        uposMap.put("E", "ADP");
        uposMap.put("FB", "PUNCT");
        uposMap.put("FC", "PUNCT");
        uposMap.put("FF", "PUNCT");
        uposMap.put("FS", "PUNCT");
        uposMap.put("I", "INTJ");
        uposMap.put("N", "NUM");
        uposMap.put("NO", "ADJ");
        uposMap.put("PART", "PART");
        uposMap.put("PC", "PRON");
        uposMap.put("PD", "PRON");
        uposMap.put("PE", "PRON");
        uposMap.put("PI", "PRON");
        uposMap.put("PP", "PRON");
        uposMap.put("PQ", "PRON");
        uposMap.put("PR", "PRON");
        uposMap.put("RD", "DET");
        uposMap.put("RI", "DET");
        uposMap.put("S", "NOUN");
        uposMap.put("SP", "PROPN");
        uposMap.put("SYM", "SYM");
        uposMap.put("T", "DET");
        uposMap.put("V", "VERB");
        uposMap.put("VA", "AUX");
        uposMap.put("VM", "AUX");
    }

    @Override
    public void annotate(Annotation annotation) {
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

            String[] parts = pos.split("\\+");
            StringBuffer upos = new StringBuffer();
            for (String part : parts) {
                String thisPos = uposMap.getOrDefault(part, DEFAULT_UPOS);
                upos.append("+").append(thisPos);
            }
            token.set(CustomAnnotations.UPosAnnotation.class, upos.substring(1));
        }

    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(CustomAnnotations.UPosAnnotation.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class
        )));
    }
}
