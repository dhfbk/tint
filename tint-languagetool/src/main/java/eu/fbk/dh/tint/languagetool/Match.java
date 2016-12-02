package eu.fbk.dh.tint.languagetool;

import java.util.List;

/**
 * Created by alessio on 02/12/16.
 */

public class Match {

    private String message;
    private String shortMessage;
    private transient List<Replacement> replacements;
    private int offset;
    private int length;
    private Context context;
    private Rule rule;

    public Match(String message, String shortMessage, List<Replacement> replacements, int offset, int length,
            Context context, Rule rule) {
        this.message = message;
        this.shortMessage = shortMessage;
        this.replacements = replacements;
        this.offset = offset;
        this.length = length;
        this.context = context;
        this.rule = rule;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShortMessage() {
        return shortMessage;
    }

    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    public List<Replacement> getReplacements() {
        return replacements;
    }

    public void setReplacements(List<Replacement> replacements) {
        this.replacements = replacements;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
