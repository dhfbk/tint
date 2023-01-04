import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.verb.VerbAnnotations;
import eu.fbk.dh.tint.verb.VerbMultiToken;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

import java.io.IOException;
import java.util.List;

public class VerbTest {
    public static void main(String[] args) {
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent, pos, ita_upos, ita_splitter, ita_morpho, ita_lemma, ita_verb");
        String text = "Il cane ha mangiato la mela.";
        Annotation annotation = pipeline.runRaw(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            List<VerbMultiToken> verbs = sentence.get(VerbAnnotations.VerbsAnnotation.class);
            System.out.println(verbs);
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                System.out.println(token.get(CoreAnnotations.FeaturesAnnotation.class));
            }

        }

//        for (VerbMultiToken verb : verbs) {
//            System.out.println(verb);
//        }
    }
}
