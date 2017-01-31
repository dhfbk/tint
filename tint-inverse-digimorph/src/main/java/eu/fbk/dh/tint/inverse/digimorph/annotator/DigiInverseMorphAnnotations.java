package eu.fbk.dh.tint.inverse.digimorph.annotator;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotator;
import eu.fbk.utils.gson.JSONLabel;

/**
 * Created by giovannimoretti on 31/01/17.
 */

public class DigiInverseMorphAnnotations {

    public static final String DH_INVERSE_MORPHOLOGY = "inverse-morphology";
    public static final Annotator.Requirement DH_INVERSE_MORPHOLOGY_REQUIREMENT = new Annotator.Requirement(DH_INVERSE_MORPHOLOGY);

    @JSONLabel("inverse_full_morpho")
    public static class InverseMorphoAnnotation implements CoreAnnotation<String> {
        public Class<String> getType() {
            return String.class;
        }
    }



}
