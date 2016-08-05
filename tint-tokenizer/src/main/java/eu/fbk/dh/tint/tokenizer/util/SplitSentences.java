package eu.fbk.dh.tint.tokenizer.util;

import com.google.common.base.Charsets;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

/**
 * Created by alessio on 22/07/16.
 */

public class SplitSentences {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplitSentences.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./annotate-sentences")
                    .withHeader("Annotate sentences")
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
            props.setProperty("annotators", "ita_toksent");
            props.setProperty("ita_toksent.newlineIsSentenceBreak", "1");
            props.setProperty("customAnnotatorClass.ita_toksent",
                    "eu.fbk.dh.tint.tokenizer.annotators.ItalianTokenizerAnnotator");

            StanfordCoreNLP ITApipeline = new StanfordCoreNLP(props);
            Annotation annotation = new Annotation(text);
            ITApipeline.annotate(annotation);

            List<CoreMap> sents = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            for (CoreMap thisSent : sents) {
                writer.append(thisSent.get(CoreAnnotations.TextAnnotation.class)).append("\n");
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
