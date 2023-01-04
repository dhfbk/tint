package eu.fbk.dh.tint.simplification;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;
import java.util.Map;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class SimpaticoAnnotations {

    @JSONLabel("simplifications")
    public static class SimplificationsAnnotation implements CoreAnnotation<List<RawSimplification>> {

        public Class<List<RawSimplification>> getType() {
            return ErasureUtils.<Class<List<RawSimplification>>>uncheckedCast(List.class);
        }
    }

    @JSONLabel("ffs")
    public static class FfsAnnotation implements CoreAnnotation<Map<String, List<RawSimplification>>> {
        public Class<Map<String, List<RawSimplification>>> getType() {
            return ErasureUtils.uncheckedCast(Map.class);
        }
    }

    @JSONLabel("ff")
    public static class FfAnnotation implements CoreAnnotation<List<String>> {
        public Class<List<String>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }

    @JSONLabel("simplifiedVersion")
    public static class SimplifiedAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

    @JSONLabel("syntSimplifiedVersion")
    public static class SyntSimplifiedAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

    @JSONLabel("isSyntSimplified")
    public static class IsSyntSimplifiedAnnotation implements CoreAnnotation<Boolean> {

        public Class<Boolean> getType() {
            return Boolean.class;
        }
    }

}
