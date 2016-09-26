package eu.fbk.dh.tint.readability;

import edu.stanford.nlp.ling.CoreAnnotation;
import eu.fbk.dh.tint.json.JSONableString;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class ReadabilityAnnotations {

    public static class ReadabilityAnnotation implements CoreAnnotation<Readability> {

        public Class<Readability> getType() {
            return Readability.class;
        }
    }

    public static class HyphenationAnnotation implements CoreAnnotation<JSONableString> {

        public Class<JSONableString> getType() {
            return JSONableString.class;
        }
    }

//    public static class MorphoCompAnnotation implements CoreAnnotation<List<String>> {
//
//        public Class<List<String>> getType() {
//            return ErasureUtils.<Class<List<String>>>uncheckedCast(List.class);
//        }
//    }

}
