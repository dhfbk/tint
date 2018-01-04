package eu.fbk.dh.tint.verb;

import com.google.common.collect.Iterables;
import edu.stanford.nlp.ling.CoreLabel;
import eu.fbk.utils.corenlp.CustomAnnotations;

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

    public boolean isPassive() {
        return isPassive;
    }

    public String getTense() {
        return tense;
    }

    public String getMood() {
        return mood;
    }

    public Integer getPerson() {
        return person;
    }

    public String getGender() {
        return gender;
    }

    public List<CoreLabel> getTokens() {
        return tokens;
    }

    public void setTokens(List<CoreLabel> tokens) {
        this.tokens = tokens;
    }

    public void addToken(VerbModel model, CoreLabel token) {
        addToken(model, token, false);
    }

    public void addToken(VerbModel model, CoreLabel token, boolean last) {
        this.tokens.add(token);
        Map<String, Collection<String>> features = token.get(CustomAnnotations.FeaturesAnnotation.class);
//        System.out.println(features);
        try {
            gender = Iterables.getFirst(features.get("Gender"), null);
        } catch (NullPointerException e) {
            gender = null;
        }
        if (last) {
            String lemma = this.tokens.get(this.tokens.size() - 1).lemma();
            boolean isTransitive = false;
            if (model.getTransitiveVerbs().contains(lemma)) {
                isTransitive = true;
            }

            switch (this.tokens.size()) {
            case 1:
                setTense(this.tokens.get(0));
                isPassive = false;

                if (isTransitive && mood.equals("Part") && tense.equals("Past")) {
                    isPassive = true;
                }
                break;
            case 2:
                String auxLemma = this.tokens.get(0).lemma();
                if (auxLemma.equals("avere")) {
                    isPassive = false;
                } else {
                    if (isTransitive) {
                        isPassive = true;
                    } else {
                        isPassive = false;
                    }
                }

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
                setTense(this.tokens.get(0));
                addStep();
            }
        }
    }

    private void addStep() throws NullPointerException {
        if (tense == null) {
            return;
        }
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
        Map<String, Collection<String>> features = coreLabel.get(CustomAnnotations.FeaturesAnnotation.class);
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
