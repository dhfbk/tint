package eu.fbk.dh.tint.resources.pos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by alessio on 03/05/16.
 */

public class CreateTrainingForStanfordPOS2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateTrainingForStanfordPOS2.class);

    /*
    Tag set: [FF, DD, PP, A, NO, DE, PQ, PR, B, B+PC, E, DI, I, VA+PC, BN, FS, DQ, PC+PC, N, DR, S, T, .$$., V, E+RD, VM+PC, X, SP, CC, SW, V+PC+PC, VA, AP, V+PC, CS, RD, PC, PD, PE, RI, VM, PI, FB, VM+PC+PC, FC]
    */

    public static void main(String[] args) {
//        String input = args[0];
//        String output = args[1];

        String input = "/Users/alessio/Documents/Resources/universal_treebanks_v2.0/std/it/it-universal-test.conll";
        String output = "/Users/alessio/Documents/Resources/universal_treebanks_v2.0/std/it/it-universal-test.conll.stanford";

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));

            List<String> lines = Files.readAllLines((new File(input)).toPath());
            StringBuffer lineBuffer = new StringBuffer();
            for (String line : lines) {
                String[] parts = line.split("\\s+");
                if (parts.length < 5) {
                    writer.append(lineBuffer.toString().trim());
                    writer.append("\n");
                    lineBuffer = new StringBuffer();
                    continue;
                }
                String token = parts[1];
                String pos = parts[3];
                if (!parts[4].equals(parts[3])) {
                    switch (parts[4]) {
                    case "AUX":
                        pos += "-AUX";
                        break;
                    case "PNOUN":
                        pos = parts[4];
                        break;
                    default:
                        LOGGER.error("Error in POS: {}", parts[4]);
                    }
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
            }

            writer.append(lineBuffer.toString().trim());
            writer.append("\n");

            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
