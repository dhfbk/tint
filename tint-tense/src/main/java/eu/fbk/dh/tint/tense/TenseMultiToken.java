package eu.fbk.dh.tint.tense;

import edu.stanford.nlp.ling.CoreLabel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alessio on 08/03/17.
 */

public class TenseMultiToken {

    List<CoreLabel> tokens = new ArrayList<>();

    public List<CoreLabel> getTokens() {
        return tokens;
    }

    public void setTokens(List<CoreLabel> tokens) {
        this.tokens = tokens;
    }

    public void addToken(CoreLabel token) {
        this.tokens.add(token);
    }

    @Override public String toString() {
        return "TenseMultiToken{" +
                "tokens=" + tokens +
                '}';
    }
}
