package eu.fbk.dh.tint.runner;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dkm.utils.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Properties;

import static com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT.value;

/**
 * Created by alessio on 03/08/16.
 */

public class TintRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TintRunner.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("tint")
                    .withHeader("Run the Tint pipeline.")
                    .withOption("c", "config-file", "Configuration file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("i", "input-file", "Input text file (default stdin)", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("o", "output-file", "Output processed file (default stdout)", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("i", File.class);
            final File outputPath = cmd.getOptionValue("o", File.class);
            final File configPath = cmd.getOptionValue("c", File.class);

            InputStream stream = null;
            Properties stanfordConfig = new Properties();

            stream = TintRunner.class.getResourceAsStream("/default-config.properties");
            stanfordConfig.load(stream);

            if (configPath != null) {
                stream = new FileInputStream(configPath);
                stanfordConfig.load(stream);
            }

            StringBuilder inputText = new StringBuilder();

            if (inputPath != null) {
                BufferedReader reader = new BufferedReader(new FileReader(inputPath));
                int i;
                while ((i = reader.read()) != -1) {
                    inputText.append((char) i);
                }
                reader.close();
            } else {
                InputStreamReader reader = new InputStreamReader(System.in);
                int i;
                while ((i = reader.read()) != -1) {
                    inputText.append((char) i);
                }
                reader.close();
            }

            System.out.println(stanfordConfig);
            StanfordCoreNLP pipeline = new StanfordCoreNLP(stanfordConfig);

            System.out.println(inputText.toString());

//            boolean cond = true;
//            int i = 0;
//            while (cond) {
//                if (cadena.isEmpty()) {
//                    cond = false;
//                }
//            }

//            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//
//            while (true) {

//                System.out.print("Enter something : ");
//                String input = br.readLine();
//
//                if ("q".equals(input)) {
//                    System.out.println("Exit!");
//                    System.exit(0);
//                }
//
//                System.out.println(input);
//                System.out.println("input : " + input);
//                System.out.println("-----------\n");
//            }

//            br.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
