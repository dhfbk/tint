package eu.fbk.dh.tint.simplification;

/**
 * Created by alessio on 14/07/17.
 */

public class RawSimplification {

    int start;
    int end;
    String simplification;
    String originalValue;

    public RawSimplification(int start, int end, String simplification) {
        this.start = start;
        this.end = end;
        this.simplification = simplification;
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

    public String getSimplification() {
        return simplification;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public void setSimplification(String simplification) {
        this.simplification = simplification;
    }
}