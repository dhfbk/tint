package eu.fbk.dh.tint.eval.morpho;

import com.google.common.base.Charsets;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dkm.utils.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

/**
 * Created by alessio on 21/07/16.
 */

public class AnnotateLemma {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotateLemma.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./annotate-lemmas")
                    .withHeader("Annotate lemmas")
                    .withOption("i", "input", "Input file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Input file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File input = cmd.getOptionValue("input", File.class);
            File output = cmd.getOptionValue("output", File.class);

            String text = new String(Files.readAllBytes(input.toPath()), Charsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));

            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, ita_morpho, ita_lemma");
            props.setProperty("tokenize.whitespace", "true");
            props.setProperty("ssplit.eolonly", "true");

//            props.setProperty("ita_toksent.newlineIsSentenceBreak", "1");

            props.setProperty("pos.model", "/Users/alessio/Documents/Resources/ita-models/italian5.tagger");

            props.setProperty("customAnnotatorClass.ita_toksent",
                    "eu.fbk.dkm.pikes.tintop.ita.annotators.ItalianTokenizerAnnotator");
            props.setProperty("customAnnotatorClass.ita_lemma", "eu.fbk.dh.digimorph.annotator.DigiLemmaAnnotator");
            props.setProperty("customAnnotatorClass.ita_morpho", "eu.fbk.dh.digimorph.annotator.DigiMorphAnnotator");
            props.setProperty("ita_morpho.model", "/Users/alessio/Documents/Resources/ita-models/italian.db");

            StanfordCoreNLP ITApipeline = new StanfordCoreNLP(props);
            Annotation annotation = new Annotation(text);
            ITApipeline.annotate(annotation);

            System.out.println(ITApipeline.timingInformation());

            List<CoreMap> sents = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap thisSent : sents) {
                List<CoreLabel> tokens = thisSent.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel token : tokens) {
                    writer.append(token.originalText().replaceAll("\\s+", ""))
                            .append("\t")
                            .append(token.get(CoreAnnotations.PartOfSpeechAnnotation.class))
                            .append("\t")
                            .append(token.get(CoreAnnotations.LemmaAnnotation.class))
                            .append("\n");
                }
                writer.append("\n");
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
