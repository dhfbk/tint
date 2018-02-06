package eu.fbk.dh.tint.derived;

import edu.stanford.nlp.ling.CoreAnnotation;
import eu.fbk.utils.gson.JSONLabel;

public class DerivationAnnotations {

    @JSONLabel("derivation")
    public static class DerivationAnnotation implements CoreAnnotation<Derivation> {

        public Class<Derivation> getType() {
            return Derivation.class;
        }
    }

}
