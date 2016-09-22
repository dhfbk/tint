package eu.fbk.dh.tint.runner;

import ch.qos.logback.classic.Level;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
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
                    .withOption(null, "properties", "Additional properties for Stanford CoreNLP", "PROPS",
                            CommandLine.Type.STRING, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("i", File.class);
            final File outputPath = cmd.getOptionValue("o", File.class);
            final File configPath = cmd.getOptionValue("c", File.class);

            List<String> addProperties = cmd.getOptionValues("properties", String.class);
            Properties additionalProps = new Properties();
            for (String property : addProperties) {
                try {
                    additionalProps.load(new StringReader(property));
                } catch (Exception e) {
                    // continue
                }
            }

            if (outputPath == null) {
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.OFF);
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("eu.fbk")).setLevel(Level.OFF);
            }

            final String formatString = cmd.getOptionValue("f", String.class);
            OutputFormat format = getOutputFormat(formatString, OutputFormat.CONLL);

            // Input

            InputStream inputStream;
//            Reader reader;

            if (inputPath != null) {
                inputStream = new FileInputStream(inputPath);
            } else {
                inputStream = System.in;
            }

            // Text annotation

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.loadPropertiesFromFile(configPath);
            pipeline.addProperties(additionalProps);
            pipeline.load();
            System.err.println("Tint is ready");

            // Output

            OutputStream outputStream = System.out;
            if (outputPath != null) {
                outputStream = new FileOutputStream(outputPath);
            }

            pipeline.run(inputStream, outputStream, format);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    public static OutputFormat getOutputFormat(String formatString, OutputFormat outputFormat) {
        OutputFormat format = outputFormat;
        try {
            format = OutputFormat.valueOf(formatString.toUpperCase());
        } catch (Exception e) {
            // continue
        }
        return format;
    }
}
