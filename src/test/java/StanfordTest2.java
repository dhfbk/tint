import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Created by alessio on 26/02/15.
 */

public class StanfordTest2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(StanfordTest2.class);

    public static void main(String[] args) throws IOException {

        byte[] file = Files.readAllBytes((new File("/Volumes/LEXAR/Resources/postag-ita/it-ud-train.text")).toPath());
        String ITAtext = new String(file);

        Properties props;
        Annotation annotation;

        props = new Properties();
        props.setProperty("annotators", "ita_toksent");

        props.setProperty("customAnnotatorClass.ita_toksent", "eu.fbk.dkm.pikes.tintop.ita.annotators.ItalianTokenizerAnnotator");

        StanfordCoreNLP ITApipeline = new StanfordCoreNLP(props);
        annotation = new Annotation(ITAtext);
        ITApipeline.annotate(annotation);
        System.out.println(ITApipeline.timingInformation());


    }
}
