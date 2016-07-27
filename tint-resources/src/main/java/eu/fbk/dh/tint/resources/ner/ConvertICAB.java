package eu.fbk.dh.tint.resources.ner;

import eu.fbk.dkm.utils.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by alessio on 20/07/16.
 */

public class ConvertICAB {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertICAB.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./convert-icab")
                    .withHeader("Convert I-CAB dataset for Stanford training")
                    .withOption("i", "input", "Input training/test file in IOB2 format", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output-stanford", "Output file for Stanford", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withOption("t", "output-text", "Output file text only", "FILE",
                            CommandLine.Type.FILE, true, false, false)
                    .withOption("k", "output-text-br", "Output file one-token-per-line", "FILE",
                            CommandLine.Type.FILE, true, false, false)
                    .withOption("g", "keep-gpe", "Keep GPE tags (default is to remove them)")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File input = cmd.getOptionValue("input", File.class);
            File output = cmd.getOptionValue("output-stanford", File.class);
            File textOut = cmd.getOptionValue("output-text", File.class);
            File textTok = cmd.getOptionValue("output-text-br", File.class);

            boolean keepGpe = cmd.hasOption("keep-gpe");

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(input), Charset.forName("ISO-8859-1")));
            BufferedWriter textWriter = null;
            BufferedWriter textTokWriter = null;
            if (textOut != null) {
                textWriter = new BufferedWriter(new FileWriter(textOut));
            }
            if (textTok != null) {
                textTokWriter = new BufferedWriter(new FileWriter(textTok));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.length() == 0) {
                    if (textWriter != null) {
                        textWriter.write("\n");
                    }
                    if (textTokWriter != null) {
                        textTokWriter.write("\n");
                    }
                    writer.write("\n");
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                String token = parts[0];
                String ner = parts[parts.length - 1];
                ner = ner.replaceAll("^[A-Za-z]-", "");
                if (!keepGpe && ner.equals("GPE")) {
                    ner = "LOC";
                }

                writer.append(token).append("\t").append(ner).append("\n");
                if (textWriter != null) {
                    textWriter.append(token).append(" ");
                }
                if (textTokWriter != null) {
                    textTokWriter.append(token).append("\n");
                }
            }

            if (textWriter != null) {
                textWriter.close();
            }
            if (textTokWriter != null) {
                textTokWriter.close();
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
