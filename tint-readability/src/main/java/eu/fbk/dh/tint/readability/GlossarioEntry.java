package eu.fbk.dh.tint.readability;

import java.util.Arrays;

/**
 * Created by alessio on 31/05/16.
 */

public class GlossarioEntry {

    private String[] forms;
    private String description;
    private String pos;

    public GlossarioEntry(String[] forms, String description) {
        this.forms = forms;
        this.description = description;
    }

    public GlossarioEntry(String form, String description) {
        this.forms = new String[] { form };
        this.description = description;
    }

    public String[] getForms() {
        return forms;
    }

    public void setForms(String[] forms) {
        this.forms = forms;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override public String toString() {
        return "GlossarioEntry{" +
                "forms=" + Arrays.toString(forms) +
                ", description='" + description + '\'' +
                ", pos='" + pos + '\'' +
                '}';
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }
}
