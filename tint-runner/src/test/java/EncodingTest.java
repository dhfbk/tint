import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

import java.nio.charset.StandardCharsets;

public class EncodingTest {
    public static void main(String[] args) {
        try {
            String string = "This 😈 is a devil emoji";
            String[] parts = string.split("\\s+");
            for (String part : parts) {
                System.out.printf("String: %s%n", part);
                System.out.printf("Length in UTF-8: %d%n", part.length());
                byte[] converttoBytes = string.getBytes(StandardCharsets.UTF_16);
                System.out.printf("Length in UTF-16: %d%n", converttoBytes.length);
            }

            TintPipeline pipeline = new TintPipeline();
            pipeline.setProperty("annotators", "ita_toksent");
            pipeline.setProperty("ita_toksent.model", "/Users/alessio/Dropbox/relation-extraction/token-settings-wemapp.xml");
            pipeline.setProperty("ita_toksent.newlineIsSentenceBreak", "two");
            Annotation annotation = pipeline.runRaw(string);
            String json = JSONOutputter.jsonPrint(annotation);
            System.out.println(json);

//            string = new String(converttoBytes, StandardCharsets.UTF_16);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
