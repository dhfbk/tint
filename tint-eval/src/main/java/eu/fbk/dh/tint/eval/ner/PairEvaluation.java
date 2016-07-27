package eu.fbk.dh.tint.eval.ner;

import edu.stanford.nlp.stats.MultiClassChunkEvalStats;
import eu.fbk.dkm.utils.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessio on 20/07/16.
 */

public class PairEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(PairEvaluation.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./evaluate-ner")
                    .withHeader("Calculate NER evaluation")
                    .withOption("t", "guessed", "Input file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("g", "gold-standard", "Input gold standard file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File guessed = cmd.getOptionValue("guessed", File.class);
            File gold = cmd.getOptionValue("gold-standard", File.class);

            List<String> guesses = new ArrayList<>();
            List<String> trueLabels = new ArrayList<>();

            BufferedReader tReader = new BufferedReader(new FileReader(guessed));
            BufferedReader gReader = new BufferedReader(new FileReader(gold));

            String line;

            while ((line = tReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                String ner = parts[parts.length - 1];
                ner = ner.replaceAll("^[A-Za-z]-", "");
                if (ner.equals("GPE")) {
                    ner = "LOC";
                }

                guesses.add(ner);
            }

            while ((line = gReader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                String ner = parts[parts.length - 1];
                trueLabels.add(ner);
            }

            if (guesses.size() != trueLabels.size()) {
                LOGGER.error("Sizes are not identical");
            }
            else {
                MultiClassChunkEvalStats stats = new MultiClassChunkEvalStats("O");
                stats.score(guesses, trueLabels);
                System.out.println(stats.getConllEvalString());
            }
            tReader.close();
            gReader.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
