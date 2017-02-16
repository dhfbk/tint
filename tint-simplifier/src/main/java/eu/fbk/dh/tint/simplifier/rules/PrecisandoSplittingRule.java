package eu.fbk.dh.tint.simplifier.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alessio on 15/02/17.
 */

public class PrecisandoSplittingRule extends SimpleSplittingRule{

    public PrecisandoSplittingRule() {
        List<String> words = new ArrayList<>();
        Map<Integer, String> replacements = new HashMap<>();
        words.add(",");
        words.add("precisando");
        words.add("che");
        replacements.put(0, "");
        replacements.put(1, "Si precisa");
        setWords(words);
        setReplacements(replacements);
        setHead(1);
    }
}
