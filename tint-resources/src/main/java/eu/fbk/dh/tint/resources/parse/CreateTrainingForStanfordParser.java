package eu.fbk.dh.tint.resources.parse;

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

    public static void main(String[] args) {
//        String input = args[0];
//        String output = args[1];

        String input = "/Users/alessio/Documents/Resources/universal-dependencies-1.2/UD_Italian/it-ud-dev.conllu";
        String output = "/Users/alessio/Documents/Resources/universal-dependencies-1.2/UD_Italian/it-ud-dev.conllu.parse.stanford";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));

            List<String> lines = Files.readAllLines((new File(input)).toPath());

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
                String pos = parts[4];

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

//                        writer.append(Integer.toString(from - offset)).append("\t");
//                        writer.append(multiToken).append("\t");
//                        writer.append(multiToken).append("\t");
//                        writer.append(multiPos.toString()).append("\t");
//                        writer.append(multiParseParent.toString()).append("\t");
//                        writer.append(multiParseLabel).append("\n");

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

//                writer.append(Integer.toString(Integer.parseInt(id) - offset)).append("\t");
//                writer.append(token).append("\t");
//                writer.append(lemma).append("\t");
//                writer.append(pos).append("\t");
//                writer.append(parseParent.toString()).append("\t");
//                writer.append(parseLabel).append("\n");
            }

            writeSentence(sentence, sentenceOffsets, writer);
            sentence = new ArrayList<>();
            sentenceOffsets = new HashMap<>();
//            writer.append("\n");

            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
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
