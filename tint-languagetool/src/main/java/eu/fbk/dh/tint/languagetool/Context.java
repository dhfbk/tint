package eu.fbk.dh.tint.languagetool;


/**
 * Created by alessio on 02/12/16.
 */

public class Context {
    String text;
    int offset;
    int length;

    public Context(String text, int offset, int length) {
        this.text = text;
        this.offset = offset;
        this.length = length;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
}
