package eu.fbk.dh.tint.verb;

import com.google.common.collect.Iterables;
import edu.stanford.nlp.ling.CoreLabel;
import eu.fbk.fcw.udpipe.api.UDPipeAnnotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by alessio on 08/03/17.
 */

public class VerbMultiToken {

    List<CoreLabel> tokens = new ArrayList<>();
    boolean isPassive = false;
    String tense;
    String mood;
    Integer person = null;
    String gender = null;

    public List<CoreLabel> getTokens() {
        return tokens;
    }

    public void setTokens(List<CoreLabel> tokens) {
        this.tokens = tokens;
    }

    public void addToken(CoreLabel token) {
        addToken(token, false);
    }

    public void addToken(CoreLabel token, boolean last) {
        this.tokens.add(token);
        Map<String, Collection<String>> features = token.get(UDPipeAnnotations.FeaturesAnnotation.class);
        System.out.println(features);
        try {
            gender = Iterables.getFirst(features.get("Gender"), null);
        } catch (NullPointerException e) {
            gender = null;
        }
        if (last) {
            switch (this.tokens.size()) {
            case 1:
                setTense(this.tokens.get(0));
                isPassive = false;
                break;
            case 2:
                // check transitivity
                setTense(this.tokens.get(0));
                if (!isPassive) {
                    try {
                        addStep();
                    } catch (NullPointerException e) {
                        // ignored
                    }
                }
                break;
            default:
                isPassive = true;
            }
        }
    }

    private void addStep() throws NullPointerException {
        switch (mood) {
        case "Ind":
        case "Conj":
            switch (tense) {
            case "Pres":
                tense = "PrPast";
                break;
            case "Imp":
                tense = "TrPast";
                break;
            case "Past":
                tense = "RemPast";
                break;
            case "Fut":
                tense = "AntFut";
                break;
            }
        default:
            switch (tense) {
            case "Pres":
                tense = "Past";
            }
        }
    }

    private void setTense(CoreLabel coreLabel) {
        Map<String, Collection<String>> features = coreLabel.get(UDPipeAnnotations.FeaturesAnnotation.class);
        try {
            tense = Iterables.getFirst(features.get("Tense"), null);
        } catch (NullPointerException e) {
            tense = null;
        }
        try {
            mood = Iterables.getFirst(features.get("Mood"), null);
        } catch (NullPointerException e) {
            mood = null;
        }
        if (mood == null) {
            mood = Iterables.getFirst(features.get("VerbForm"), null);
        }
        if (mood != null && mood.equals("Sub")) {
            mood = "Conj";
        }
        String txtPerson = null;
        try {
            txtPerson = Iterables.getFirst(features.get("Person"), null);
        } catch (NullPointerException e) {
            // ignore
        }
        if (txtPerson != null) {
            person = Integer.parseInt(txtPerson);
        }
    }

    @Override public String toString() {
        return "VerbMultiToken{" +
                "tokens=" + tokens +
                ", isPassive=" + isPassive +
                ", tense='" + tense + '\'' +
                ", mood='" + mood + '\'' +
                ", person=" + person +
                ", gender='" + gender + '\'' +
                '}';
    }
}
