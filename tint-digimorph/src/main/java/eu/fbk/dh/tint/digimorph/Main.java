package eu.fbk.dh.tint.digimorph;

import eu.fbk.utils.core.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static void printUsage(Options opt) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
                "echo <word> | java -jar DigiMorph.jar \n       cat <path to file> | java -jar DigiMorph.jar\n\n", opt);
        System.exit(1);
    }

    private static void retrain(File filepath, File outputPath, boolean include_lemma) {
        DigiMorph.re_train(filepath, outputPath, include_lemma);
        System.exit(0);
    }

    public static void main(String[] args) {

        try {
            final eu.fbk.utils.core.CommandLine cmd = eu.fbk.utils.core.CommandLine
                    .parser()
                    .withName("run-digimorph")
                    .withHeader("Run or retrain DigiMorph tool")
                    .withOption("r", "retrain-input-file", "Input file in Morph-IT for retraining", "FILE",
                            eu.fbk.utils.core.CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("w", "retrain-output-file", "Output file for retraining", "FILE",
                            eu.fbk.utils.core.CommandLine.Type.FILE, true, false, false)
                    .withOption("l", "lemma", "Include lemma")
                    .withOption("v", "version", "Print the tool version")
                    .withOption("R", "retrain", "Retrain using default file")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            if (cmd.hasOption("version")) {
                System.out.println(DigiMorph.getVersion());
                System.exit(0);
            }

            File retrainInputFile = cmd.getOptionValue("retrain-input-file", File.class);
            File retrainOutputFile = cmd.getOptionValue("retrain-output-file", File.class);
            boolean lemma = cmd.hasOption("lemma");
            boolean retrain = cmd.hasOption("retrain");

            if (retrainInputFile != null || retrainOutputFile != null) {
                if ((retrainInputFile != null || retrain) && retrainOutputFile != null) {
                    retrain(retrainInputFile, retrainOutputFile, lemma);
                } else {
                    throw new CommandLine.Exception("Input file or output path missing for retrain");
                }
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }

        List<String> text = new LinkedList<String>();
        Scanner scanner = new Scanner(System.in);
        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.compareToIgnoreCase("morph()") == 0) {
                break;
            }
            text.add(line);

        }
        DigiMorph dm = new DigiMorph();

        for (String s : dm.getMorphology(text)) {
            System.out.println(s);
        }

    }
}

