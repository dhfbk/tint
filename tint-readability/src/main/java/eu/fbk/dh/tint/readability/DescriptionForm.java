package eu.fbk.dh.tint.readability;

/**
 * Created by alessio on 03/06/16.
 */

public class DescriptionForm {

    private int start, end;
    private GlossarioEntry description;

    public DescriptionForm(int start, int end, GlossarioEntry description) {
        this.start = start;
        this.end = end;
        this.description = description;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public GlossarioEntry getDescription() {
        return description;
    }

    public void setDescription(GlossarioEntry description) {
        this.description = description;
    }

    @Override public String toString() {
        return "DescriptionForm{" +
                "start=" + start +
                ", end=" + end +
                ", description=" + description.getDescription() +
                '}';
    }
}
