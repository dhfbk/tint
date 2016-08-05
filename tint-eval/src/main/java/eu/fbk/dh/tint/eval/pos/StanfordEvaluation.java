package eu.fbk.dh.tint.eval.pos;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.stats.MultiClassChunkEvalStats;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by alessio on 20/07/16.
 */

public class StanfordEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(StanfordEvaluation.class);

    private enum SimplePOS {VERB, NOUN, ADJECTIVE, ADVERB, OTHER}

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./evaluate-pos")
                    .withHeader("Calculate POS evaluation for Stanford")
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

            for (int i = 0; i < guesses.size(); i++) {
                String guess = guesses.get(i);
                String goldLabel = trueLabels.get(i);

                if (goldLabel.equals("<eos>") || goldLabel.length() == 0) {
                    continue;
                }

                total++;

                String[] parts = guess.split("\t");
                guess = parts[1];

                SimplePOS goldPos = SimplePOS.OTHER;
                SimplePOS guessedPos = SimplePOS.OTHER;

                if (goldLabel.startsWith("V")) {
                    goldPos = SimplePOS.VERB;
                } else if (goldLabel.startsWith("S")) {
                    goldPos = SimplePOS.NOUN;
                } else if (goldLabel.startsWith("A")) {
                    goldPos = SimplePOS.ADJECTIVE;
                } else if (goldLabel.startsWith("B")) {
                    goldPos = SimplePOS.ADVERB;
                }

                if (guess.startsWith("B")) {
                    guessedPos = SimplePOS.ADVERB;
                } else if (guess.startsWith("V")) {
                    guessedPos = SimplePOS.VERB;
                } else if (guess.startsWith("S")) {
                    guessedPos = SimplePOS.NOUN;
                } else if (guess.startsWith("A")) {
                    guessedPos = SimplePOS.ADJECTIVE;
                }

                if (goldPos.equals(guessedPos)) {
                    correct++;
                }
            }

            System.out.println(correct);
            System.out.println(total);
            System.out.println(correct * 1.0 / total);
            System.exit(1);

            MultiClassChunkEvalStats stats = new MultiClassChunkEvalStats("O");
            stats.score(guesses, trueLabels);
            System.out.println(stats.getConllEvalString());
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
