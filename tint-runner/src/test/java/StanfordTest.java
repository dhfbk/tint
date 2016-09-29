import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by alessio on 25/09/16.
 */

public class StanfordTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StanfordTest.class);

    public static void main(String[] args) throws IOException {
        String text = "International prosecutors investigating the downing of flight MH17 over eastern Ukraine in 2014 say the Buk missile that hit the plane was from Russia.\n"
                + "They also narrowed down the area it was fired from to a field in territory controlled by Russian-backed rebels.\n"
                + "All 298 people on board the Boeing 777 died when it broke apart in mid-air flying from Amsterdam to Kuala Lumpur.\n"
                + "Russia says it cannot accept the findings as the final truth, saying no Russian weapons were taken to Ukraine.\n"
                + "\"Based on the criminal investigation, we have concluded that flight MH17 was downed by a Buk missile of the series 9M83 that came from the territory of the Russian Federation,\" chief Dutch police investigator Wilbert Paulissen told a news conference on Thursday.\n"
                + "The missile had been taken from Russia to rebel-held Ukraine in the morning 17 of July, when the plane was shot down, and the launcher was taken back to Russia afterwards, he said.";
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, readability");
        props.setProperty("readability.language", "en");
        props.setProperty("customAnnotatorClass.readability", "eu.fbk.dh.tint.readability.ReadabilityAnnotator");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        System.out.println(JSONOutputter.jsonPrint(document));
    }
}
