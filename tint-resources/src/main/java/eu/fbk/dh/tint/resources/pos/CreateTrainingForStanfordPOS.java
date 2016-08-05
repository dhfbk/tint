package eu.fbk.dh.tint.resources.pos;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 03/05/16.
 */

public class CreateTrainingForStanfordPOS {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTrainingForStanfordPOS.class);
    private static final int DEFAULT_COL = 3;

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./create-pos-training")
                    .withHeader("Create training for Stanford POS tagger")
                    .withOption("i", "input", "Input file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("t", "only-tokens", "Output file for tokens", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("p", "only-pos", "Output file for pos", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("x", "text", "Output text", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("c", "conll", "Output in CoNLL format", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption(null, "column", String.format("Column for POS (default %d)", DEFAULT_COL), "FILE",
                            CommandLine.Type.INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File input = cmd.getOptionValue("input", File.class);
            File output = cmd.getOptionValue("output", File.class);
            File onlyTokens = cmd.getOptionValue("only-tokens", File.class);
            File onlyPos = cmd.getOptionValue("only-pos", File.class);
            File onlyText = cmd.getOptionValue("text", File.class);
            File conll = cmd.getOptionValue("conll", File.class);

            Integer column = cmd.getOptionValue("column", Integer.class, DEFAULT_COL);

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            BufferedWriter tokensWriter = null;
            BufferedWriter posWriter = null;
            BufferedWriter textWriter = null;
            BufferedWriter conllWriter = null;

            if (onlyTokens != null) {
                tokensWriter = new BufferedWriter(new FileWriter(onlyTokens));
            }
            if (onlyPos != null) {
                posWriter = new BufferedWriter(new FileWriter(onlyPos));
            }
            if (onlyText != null) {
                textWriter = new BufferedWriter(new FileWriter(onlyText));
            }
            if (conll != null) {
                conllWriter = new BufferedWriter(new FileWriter(conll));
            }

            List<String> lines = Files.readAllLines(input.toPath());
            StringBuffer lineBuffer = new StringBuffer();

            String multiToken = null;
            String multiLemma = null;
            StringBuffer multiPos = new StringBuffer();
            Pattern fromPattern = Pattern.compile("^([0-9]+)");
            Pattern endPattern = Pattern.compile("([0-9]+)$");
            Integer from = null;
            Integer end = null;

            for (String line : lines) {
                line = line.trim();

                if (line.startsWith("#")) {
                    continue;
                }

                if (line.length() == 0) {
                    writer.append(lineBuffer.toString().trim());
                    writer.append("\n");
                    lineBuffer = new StringBuffer();

                    if (tokensWriter != null) {
                        tokensWriter.append("<eos>\n");
                    }
                    if (posWriter != null) {
                        posWriter.append("<eos>\n");
                    }
                    if (textWriter != null) {
                        textWriter.append("\n");
                    }
                    if (conllWriter != null) {
                        conllWriter.append("\n");
                    }

                    continue;
                }

                String[] parts = line.split("\\s+");

                String id = parts[0];
                String token = parts[1];
                String lemma = parts[2];
                String pos;
                try {
                    pos = parts[column];
                } catch (Exception e) {
                    LOGGER.error("Invalid column");
                    break;
                }
                Integer numericId = null;

                if (id.contains("-")) {
                    multiToken = token;
                    multiLemma = lemma;
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

                    continue;
                }

                numericId = Integer.parseInt(id);
                if (end != null && from != null) {
                    if (numericId <= end || numericId >= from) {
                        if (multiPos.length() > 0) {
                            multiPos.append("+");
                        }
                        multiPos.append(pos);
                    }

                    if (numericId.equals(end)) {
                        StringBuilder buffer = new StringBuilder();
                        buffer.append(multiToken);
                        buffer.append("_");
                        buffer.append(multiPos.toString());
                        buffer.append(" ");
                        lineBuffer.append(buffer.toString());
                        if (tokensWriter != null) {
                            tokensWriter.append(multiToken).append("\n");
                        }
                        if (posWriter != null) {
                            posWriter.append(multiPos).append("\n");
                        }
                        if (textWriter != null) {
                            textWriter.append(multiToken).append(" ");
                        }
                        if (conllWriter != null) {
                            conllWriter.append(multiToken).append("\t")
                                    .append(multiLemma).append("\t")
                                    .append(multiPos).append("\n");
                        }

                        multiPos = new StringBuffer();
                        multiToken = null;
                        multiLemma = null;
                        end = null;
                        from = null;
                    }

                    continue;
                }

                if (token.equals("_")) {
                    LOGGER.error("Error in token {}", token);
                    continue;
                }

                StringBuffer buffer = new StringBuffer();
                buffer.append(token);
                buffer.append("_");
                buffer.append(pos);
                buffer.append(" ");
                lineBuffer.append(buffer.toString());

                if (tokensWriter != null) {
                    tokensWriter.append(token).append("\n");
                }
                if (posWriter != null) {
                    posWriter.append(pos).append("\n");
                }
                if (textWriter != null) {
                    textWriter.append(token).append(" ");
                }
                if (conllWriter != null) {
                    conllWriter.append(token).append("\t")
                            .append(lemma).append("\t")
                            .append(pos).append("\n");
                }

            }

            writer.append(lineBuffer.toString().trim());
            writer.append("\n");
            if (tokensWriter != null) {
                tokensWriter.append("\n");
            }
            if (posWriter != null) {
                posWriter.append("\n");
            }
            if (textWriter != null) {
                textWriter.append("\n");
            }
            if (conllWriter != null) {
                conllWriter.append("\n");
            }

            writer.close();
            if (tokensWriter != null) {
                tokensWriter.close();
            }
            if (posWriter != null) {
                posWriter.close();
            }
            if (textWriter != null) {
                textWriter.close();
            }
            if (conllWriter != null) {
                conllWriter.close();
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
