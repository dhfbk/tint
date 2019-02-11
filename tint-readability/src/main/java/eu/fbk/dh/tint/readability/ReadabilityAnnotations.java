package eu.fbk.dh.tint.readability;

import edu.stanford.nlp.ling.CoreAnnotation;
import eu.fbk.utils.gson.JSONLabel;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class ReadabilityAnnotations {

    @JSONLabel("contentWord")
    public static class ContentWord implements CoreAnnotation<Boolean> {

        public Class<Boolean> getType() {
            return Boolean.class;
        }
    }

    @JSONLabel("easyWord")
    public static class EasyWord implements CoreAnnotation<Boolean> {

        public Class<Boolean> getType() {
            return Boolean.class;
        }
    }

    @JSONLabel("literalWord")
    public static class LiteralWord implements CoreAnnotation<Boolean> {

        public Class<Boolean> getType() {
            return Boolean.class;
        }
    }

    @JSONLabel("depth")
    public static class SentenceDepthAnnotation implements CoreAnnotation<Integer> {

        public Class<Integer> getType() {
            return Integer.class;
        }
    }

    @JSONLabel("subordinateRatio")
    public static class SubordinateRatioAnnotation implements CoreAnnotation<Double> {

        public Class<Double> getType() {
            return Double.class;
        }
    }

    @JSONLabel("density")
    public static class DensityAnnotation implements CoreAnnotation<Double> {

        public Class<Double> getType() {
            return Double.class;
        }
    }

    @JSONLabel("readability")
    public static class ReadabilityAnnotation implements CoreAnnotation<Readability> {

        public Class<Readability> getType() {
            return Readability.class;
        }
    }

    @JSONLabel("hyphenation")
    public static class HyphenationAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

    @JSONLabel("difficultyLevel")
    public static class DifficultyLevelAnnotation implements CoreAnnotation<Integer> {

        public Class<Integer> getType() {
            return Integer.class;
        }
    }

    @JSONLabel("contentWords")
    public static class ContentWordsAnnotation implements CoreAnnotation<Integer> {

        public Class<Integer> getType() {
            return Integer.class;
        }
    }

    @JSONLabel("literalWords")
    public static class LiteralWordsAnnotation implements CoreAnnotation<Integer> {

        public Class<Integer> getType() {
            return Integer.class;
        }
    }

}
