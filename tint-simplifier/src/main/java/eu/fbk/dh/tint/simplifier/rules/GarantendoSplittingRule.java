package eu.fbk.dh.tint.simplifier.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alessio on 15/02/17.
 */

public class GarantendoSplittingRule extends SimpleSplittingRule{

    public GarantendoSplittingRule() {
        List<String> words = new ArrayList<>();
        Map<Integer, String> replacements = new HashMap<>();
        words.add(",");
        words.add("garantendo");
        words.add("così");
        replacements.put(0, "");
        replacements.put(1, "Si garantisce");
        setWords(words);
        setReplacements(replacements);
        setHead(1);
    }
}
