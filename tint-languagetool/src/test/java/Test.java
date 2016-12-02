import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;

import java.util.Properties;

/**
 * Created by alessio on 02/12/16.
 */

public class Test {

    public static void main(String[] args) {
        String text = "Inserite qui il vostro testo... oppure controlate direttamente questo ed avrete un assaggio di quali errori possono essere identificati con LanguageTool.";

        try {
            TintPipeline pipeline = new TintPipeline();

            Properties properties = new Properties();
            properties.setProperty("annotators", "languagetool");
            properties.setProperty("languagetool.server", "gardner.fbk.eu");
            properties.setProperty("languagetool.port", "50007");
            properties.setProperty("customAnnotatorClass.languagetool",
                    "eu.fbk.dh.tint.languagetool.LanguageToolAnnotator");
            pipeline.loadDefaultProperties();
            pipeline.addProperties(properties);

            pipeline.run(text, System.out, TintRunner.OutputFormat.JSON);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
