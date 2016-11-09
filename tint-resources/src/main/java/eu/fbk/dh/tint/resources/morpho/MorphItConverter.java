package eu.fbk.dh.tint.resources.morpho;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 18/05/16.
 */

public class MorphItConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MorphItConverter.class);
    private static Pattern morphoType = Pattern.compile("^([A-Z-]+):?");

    static HashMap<String, String> noLemmaTypes = new HashMap<>();

    static {
        noLemmaTypes.put("PON", "[PUNCT]");
        noLemmaTypes.put("SENT", "[PUNCT]");
        noLemmaTypes.put("SMI", "[SMILE]");
        noLemmaTypes.put("SYM", "[SYMBOL]");
    }

    public static void main(String[] args) {
        final CommandLine cmd = CommandLine
                .parser()
                .withName("morphit-converter")
                .withHeader("Convert Morph-It dataset to be compliant with fstan")
                .withOption("i", "input", "input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                .withOption("o", "output", "output file", "FILE", CommandLine.Type.FILE, true, false, true)
                .withLogger(LoggerFactory.getLogger("eu.fbk.dh")).parse(args);

        final File inputPath = cmd.getOptionValue("i", File.class);
        final File outputPath = cmd.getOptionValue("o", File.class);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

            List<String> lines = Files.readLines(inputPath, Charsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    LOGGER.error("Invalid line: {}", line);
                    continue;
                }

                String form = parts[0];
                String lemma = parts[1];
                String morpho = parts[2];

                Matcher matcher = morphoType.matcher(morpho);
                if (!matcher.find()) {
                    LOGGER.warn("Invalid pattern: {}", morpho);
                    continue;
                }

                String type = matcher.group(1);

                writer.append(form).append(" ");
                if (!noLemmaTypes.containsKey(type)) {
                    writer.append(lemma).append("+");
                }
                writer.append(morpho).append("\n");
            }

            writer.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
