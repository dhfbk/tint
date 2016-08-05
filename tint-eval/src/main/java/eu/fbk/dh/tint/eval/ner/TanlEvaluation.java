package eu.fbk.dh.tint.eval.ner;

import edu.stanford.nlp.stats.MultiClassChunkEvalStats;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.diff_match_patch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by alessio on 20/07/16.
 */

public class TanlEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TanlEvaluation.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./evaluate-tanl")
                    .withHeader("Calculate NER evaluation for Tanl")
                    .withOption("t", "tanl", "Input file from Tanl", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("g", "gold-standard", "Input gold standard file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File tanl = cmd.getOptionValue("tanl", File.class);
            File gold = cmd.getOptionValue("gold-standard", File.class);

            LinkedHashMap<Integer, String> goldLabels = new LinkedHashMap<>();
            LinkedHashMap<Integer, String> tanlLabels = new LinkedHashMap<>();
            HashMap<Integer, Integer> indexMap = new HashMap<>();

            BufferedReader tReader = new BufferedReader(new FileReader(tanl));
            BufferedReader gReader = new BufferedReader(new FileReader(gold));

            String t, g;

            String line;
            StringBuilder builder;

            builder = new StringBuilder();
            while ((line = tReader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }
                String token = parts[0];
                String ner = parts[2];
                ner = ner.replaceAll("^[A-Za-z]-", "");
                if (ner.equals("GPE")) {
                    ner = "LOC";
                }
                tanlLabels.put(builder.length(), ner);
                builder.append(token);
            }
            t = builder.toString();

            builder = new StringBuilder();
            while ((line = gReader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }
                String token = parts[0];
                String ner = parts[1];
                goldLabels.put(builder.length(), ner);
                builder.append(token);
            }
            g = builder.toString();

            diff_match_patch diffMatchPatch = new diff_match_patch();
            LinkedList<diff_match_patch.Diff> diffs = diffMatchPatch.diff_main(t, g);
            diffMatchPatch.diff_cleanupSemanticLossless(diffs);

            int goldIndex = 0;
            int tanlIndex = 0;
            for (diff_match_patch.Diff diff : diffs) {
                String text = diff.text;
                switch (diff.operation) {
                case INSERT:
                    for (int i = 0; i < text.length(); i++) {
                        indexMap.put(goldIndex, tanlIndex);
                        goldIndex++;
                    }
                    break;
                case DELETE:
                    tanlIndex += text.length();
                    break;
                case EQUAL:
                    for (int i = 0; i < text.length(); i++) {
                        indexMap.put(goldIndex, tanlIndex);
                        goldIndex++;
                        tanlIndex++;
                    }
                    break;
                }
            }

            List<String> guesses = new ArrayList<>();
            List<String> trueLabels = new ArrayList<>();

            for (Integer key : goldLabels.keySet()) {
                String label = goldLabels.get(key);
                String ner = "O";
                Integer mappedIndex = indexMap.get(key);
                if (mappedIndex != null) {
                    ner = tanlLabels.get(mappedIndex);
                }
                if (ner == null) {
                    ner = "O";
                }

                trueLabels.add(label);
                guesses.add(ner);
            }

            MultiClassChunkEvalStats stats = new MultiClassChunkEvalStats("O");
            stats.score(guesses, trueLabels);
            System.out.println(stats.getConllEvalString());

            tReader.close();
            gReader.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
