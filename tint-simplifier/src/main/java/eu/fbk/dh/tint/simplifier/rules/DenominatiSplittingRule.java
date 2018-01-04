package eu.fbk.dh.tint.simplifier.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alessio on 15/02/17.
 */

public class DenominatiSplittingRule extends SimpleSplittingRule{

    public DenominatiSplittingRule() {
        List<String> words = new ArrayList<>();
        Map<Integer, String> replacements = new HashMap<>();
        words.add(",");
        words.add("denominat(e|i)");
        words.add("rispettivamente");
        words.add(":");
        replacements.put(0, "");
        replacements.put(1, "Ess$1 sono denominat$1");
        setWords(words);
        setReplacements(replacements);
        setHead(1);
        setUseRegex(true);
    }
}
