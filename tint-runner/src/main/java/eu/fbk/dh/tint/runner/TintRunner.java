package eu.fbk.dh.tint.runner;

import ch.qos.logback.classic.Level;
import edu.stanford.nlp.pipeline.*;
import eu.fbk.dkm.pikes.tintop.AnnotationPipeline;
import eu.fbk.dkm.pikes.tintop.server.AbstractHandler;
import eu.fbk.dkm.utils.CommandLine;
import ixa.kaflib.KAFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by alessio on 03/08/16.
 */

public class TintRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TintRunner.class);

    public static enum OutputFormat {
        READABLE, JSON, XML, CONLL, NAF, TEXTPRO
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("tint")
                    .withHeader("Run the Tint pipeline.")
                    .withOption("c", "config-file", "Configuration file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("i", "input-file", "Input text file (default stdin)", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("o", "output-file", "Output processed file (default stdout)", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("f", "output-format",
                            "Output format: textpro, json, xml, conll, naf, readable (default conll)",
                            "FORMAT",
                            CommandLine.Type.STRING, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("i", File.class);
            final File outputPath = cmd.getOptionValue("o", File.class);
            final File configPath = cmd.getOptionValue("c", File.class);

            if (outputPath == null) {
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.OFF);
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("eu.fbk")).setLevel(Level.OFF);
            }

            final String formatString = cmd.getOptionValue("f", String.class);

            OutputFormat format = OutputFormat.CONLL;
            try {
                format = OutputFormat.valueOf(formatString.toUpperCase());
            } catch (Exception e) {
                // continue
            }

            // Load properties

            InputStream configStream = null;
            Properties stanfordConfig = new Properties();

            configStream = TintRunner.class.getResourceAsStream("/default-config.properties");
            stanfordConfig.load(configStream);

            if (configPath != null) {
                configStream = new FileInputStream(configPath);
                stanfordConfig.load(configStream);
            }

            // Input

            StringBuilder inputText = new StringBuilder();

            if (inputPath != null) {
                BufferedReader reader = new BufferedReader(new FileReader(inputPath));
                int i;
                while ((i = reader.read()) != -1) {
                    inputText.append((char) i);
                }
                reader.close();
            } else {
                InputStreamReader reader = new InputStreamReader(System.in);
                int i;
                while ((i = reader.read()) != -1) {
                    inputText.append((char) i);
                }
                reader.close();
            }

            String text = inputText.toString();

            // Text annotation

            StanfordCoreNLP pipeline = new StanfordCoreNLP(stanfordConfig);
            Annotation annotation = new Annotation(text);
            pipeline.annotate(annotation);

            // Output

            OutputStream outputStream = System.out;
            if (outputPath != null) {
                outputStream = new FileOutputStream(outputPath);
            }

            switch (format) {
            case CONLL:
                CoNLLUOutputter.conllUPrint(annotation, outputStream, pipeline);
                break;
            case READABLE:
                TextOutputter.prettyPrint(annotation, outputStream, pipeline);
                break;
            case XML:
                XMLOutputter.xmlPrint(annotation, outputStream, pipeline);
                break;
            case JSON:
                JSONOutputter.jsonPrint(annotation, outputStream, pipeline);
                break;
            case TEXTPRO:
                TextProOutputter.tpPrint(annotation, outputStream, pipeline);
                break;
            case NAF:
                KAFDocument doc = AbstractHandler.text2naf(text, new HashMap<>());
                AnnotationPipeline pikesPipeline = new AnnotationPipeline(null, null);
                pikesPipeline.addToNerMap("PER", "PERSON");
                pikesPipeline.addToNerMap("ORG", "ORGANIZATION");
                pikesPipeline.addToNerMap("LOC", "LOCATION");
                pikesPipeline.annotateStanford(new Properties(), annotation, doc);
                outputStream.write(doc.toString().getBytes());
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
