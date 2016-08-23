package eu.fbk.dh.tint.resources.parse;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 03/05/16.
 */

public class CreateTrainingForStanfordParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTrainingForStanfordParser.class);
    private static final int DEFAULT_POS_COL = 3;

    public static void main(String[] args) {

        try {

            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./create-parse-training")
                    .withHeader("Create training for Stanford Parser")
                    .withOption("i", "input", "Input file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption(null, "column", String.format("Column for POS (default %d)", DEFAULT_POS_COL), "NUM",
                            CommandLine.Type.INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File input = cmd.getOptionValue("input", File.class);
            File output = cmd.getOptionValue("output", File.class);
            Integer column = cmd.getOptionValue("column", Integer.class, DEFAULT_POS_COL);

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));

            List<String> lines = Files.readAllLines(input.toPath());

            ArrayList<HashMap<String, Object>> sentence = new ArrayList<>();
            HashMap<Integer, Integer> sentenceOffsets = new HashMap<>();

            String multiToken = null;
            StringBuffer multiPos = new StringBuffer();
            String multiParseLabel = null;
            Integer multiParseParent = null;

            Pattern fromPattern = Pattern.compile("^([0-9]+)");
            Pattern endPattern = Pattern.compile("([0-9]+)$");
            Integer from = null;
            Integer end = null;
            HashSet<Integer> internals = new HashSet<>();
            Integer offset = 0;

            for (String line : lines) {
                line = line.trim();

                if (line.startsWith("#")) {
                    continue;
                }

                if (line.length() == 0) {
                    writeSentence(sentence, sentenceOffsets, writer);
                    sentence = new ArrayList<>();
                    sentenceOffsets = new HashMap<>();
//                    writer.append("\n");
                    offset = 0;
                    continue;
                }

                String[] parts = line.split("\\s+");

                String id = parts[0];
                String token = parts[1];
                String lemma = parts[2];
                String pos = parts[column];

                Integer parseParent = null;
                try {
                    parseParent = Integer.parseInt(parts[6]);
                } catch (Exception e) {
                    // ignored
                }
                String parseLabel = parts[7];

                Integer numericId = null;

                if (id.contains("-")) {
                    multiToken = token;
                    multiPos = new StringBuffer();

                    Matcher matcher;

                    matcher = fromPattern.matcher(id);
                    if (matcher.find()) {
                        from = Integer.parseInt(matcher.group(1));
                    }
                    matcher = endPattern.matcher(id);
                    if (matcher.find()) {
                        end = Integer.parseInt(matcher.group(1));
                    }

                    for (int i = from; i <= end; i++) {
                        internals.add(i);
                    }

                    continue;
                }

                numericId = Integer.parseInt(id);
                if (end != null && from != null) {
                    if (numericId <= end || numericId >= from) {
                        if (multiPos.length() > 0) {
                            multiPos.append("+");
                        }
                        multiPos.append(pos);
                        if (!internals.contains(parseParent) && !parseLabel.equals("det")) {
                            multiParseLabel = parseLabel;
                            multiParseParent = parseParent;
                        }
                    }

                    sentenceOffsets.put(numericId, offset + numericId - from);

                    if (numericId.equals(end)) {
                        HashMap<String, Object> thisToken = new HashMap<>();
                        thisToken.put("id", from);
                        thisToken.put("form", multiToken);
                        thisToken.put("lemma", multiToken);
                        thisToken.put("pos", multiPos);
                        thisToken.put("parseParent", multiParseParent);
                        thisToken.put("parseLabel", multiParseLabel);
                        sentence.add(thisToken);
                        sentenceOffsets.put(from, offset);

                        multiPos = new StringBuffer();
                        multiToken = null;
                        offset += end - from;
                        end = null;
                        from = null;
                        internals = new HashSet<>();

                    }

                    continue;
                }

                if (token.equals("_")) {
                    LOGGER.error("Error in token {}", token);
                    continue;
                }

                HashMap<String, Object> thisToken = new HashMap<>();
                thisToken.put("id", Integer.parseInt(id));
                thisToken.put("form", token);
                thisToken.put("lemma", lemma);
                thisToken.put("pos", pos);
                thisToken.put("parseParent", parseParent);
                thisToken.put("parseLabel", parseLabel);
                sentence.add(thisToken);
                sentenceOffsets.put(Integer.parseInt(id), offset);

            }

            writeSentence(sentence, sentenceOffsets, writer);
//            sentence = new ArrayList<>();
//            sentenceOffsets = new HashMap<>();

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    private static void writeSentence(ArrayList<HashMap<String, Object>> sentence,
            HashMap<Integer, Integer> sentenceOffsets, BufferedWriter writer) throws IOException {
        if (sentence.size() == 0) {
            return;
        }

        for (HashMap<String, Object> map : sentence) {
            int id = (int) map.get("id");
            id -= sentenceOffsets.get(id);

            int parseParent = (int) map.get("parseParent");
            if (parseParent != 0) {
                try {
                    parseParent -= sentenceOffsets.get(parseParent);
                } catch (Exception e) {
                    // Fix for UD, bad!
                    LOGGER.warn("Fix for token _");

//                    System.out.println(parseParent);
//                    System.out.println(sentence);
//                    System.out.println(sentenceOffsets);

                    parseParent = 11;
                    parseParent += sentenceOffsets.get(parseParent);
                }
            }

            writer.append(Integer.toString(id)).append("\t");
            writer.append(map.get("form").toString()).append("\t");
            writer.append(map.get("lemma").toString()).append("\t");
            writer.append(map.get("pos").toString()).append("\t");
            writer.append(map.get("pos").toString()).append("\t");
            writer.append("_").append("\t");
            writer.append(Integer.toString(parseParent)).append("\t");
            writer.append(map.get("parseLabel").toString()).append("\t");
            writer.append(Integer.toString(parseParent)).append("\t");
            writer.append(map.get("parseLabel").toString()).append("\n");
        }

        writer.append("\n");
    }
}
