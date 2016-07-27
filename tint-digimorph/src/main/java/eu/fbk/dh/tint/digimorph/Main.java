package eu.fbk.dh.tint.digimorph;

import org.apache.commons.cli.*;

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

    private static void retrain(String filepath, boolean include_lemma) {
        DigiMorph dm = new DigiMorph("italian.db");
        dm.re_train(filepath, include_lemma);
        System.exit(0);
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(
                Option.builder("r").hasArg().argName("path to file").desc("Retrain Morphological Analyzer").build());
        options.addOption("h", "help", false, "show help");
        options.addOption("l", "lemma", false, "include lemma");
        options.addOption("v", "version", false, "print the tool version");
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("version")) {
                System.out.println(DigiMorph.getVersion());
                System.exit(0);

            }

            if (cmd.hasOption("help")) {
                printUsage(options);
            }

            if (cmd.hasOption('r')) {
                if (cmd.getOptionValue('r') != null) {
                    retrain(cmd.getOptionValue('r'), cmd.hasOption("lemma"));
                } else {
                    printUsage(options);
                }
            }

        } catch (Exception e) {
            printUsage(options);
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

