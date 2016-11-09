package eu.fbk.dh.tint.digimorph.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class DigiMorphAnnotations {

    public static final String DH_MORPHOLOGY = "morphology";
    public static final Annotator.Requirement DH_MORPHOLOGY_REQUIREMENT = new Annotator.Requirement(DH_MORPHOLOGY);

    @JSONLabel("full_morpho")
    public static class MorphoAnnotation implements CoreAnnotation<String> {

        public Class<String> getType() {
            return String.class;
        }
    }

    public static class MorphoCompAnnotation implements CoreAnnotation<List<String>> {

        public Class<List<String>> getType() {
            return ErasureUtils.<Class<List<String>>>uncheckedCast(List.class);
        }
    }

}
