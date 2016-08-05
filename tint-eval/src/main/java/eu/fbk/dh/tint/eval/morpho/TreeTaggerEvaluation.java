package eu.fbk.dh.tint.eval.morpho;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by alessio on 20/07/16.
 */

public class TreeTaggerEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeTaggerEvaluation.class);

    private enum SimplePOS {VERB, NOUN, ADJECTIVE, ADVERB, OTHER}

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./evaluate-lemma")
                    .withHeader("Calculate lemma evaluation for TreeTagger")
                    .withOption("t", "guessed", "Input file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("g", "gold-standard", "Input gold standard file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File guessed = cmd.getOptionValue("guessed", File.class);
            File gold = cmd.getOptionValue("gold-standard", File.class);

            List<String> guesses = Files.readLines(guessed, Charsets.UTF_8);
            List<String> trueLabels = Files.readLines(gold, Charsets.UTF_8);

            int total = 0;
            int correct = 0;

            for (int i = 0; i < trueLabels.size(); i++) {
                String goldLabel = trueLabels.get(i);

                if (goldLabel.length() == 0) {
                    continue;
                }

                String guess = guesses.get(i);
                String[] parts;

                parts = goldLabel.split("\t");
                goldLabel = parts[1];
                String pos = parts[2];

                boolean doIt = false;
                if (pos.startsWith("V")) {
                    doIt = true;
                } else if (pos.startsWith("S")) {
                    doIt = true;
                } else if (pos.startsWith("A")) {
                    doIt = true;
                } else if (pos.startsWith("B")) {
                    doIt = true;
                }

                if (goldLabel.equals("_")) {
                    doIt = false;
                }

                if (!doIt) {
                    continue;
                }

                parts = guess.split("\t");
                guess = parts[2];
                guess = guess.replaceAll("\\|.*", "");

                total++;
                if (guess.equalsIgnoreCase(goldLabel)) {
                    correct++;
                } else {
                    System.out.printf("%s -> %s\n", guess, goldLabel);
                }

            }

            System.out.println(correct);
            System.out.println(total);
            System.out.println(correct * 1.0 / total);
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
