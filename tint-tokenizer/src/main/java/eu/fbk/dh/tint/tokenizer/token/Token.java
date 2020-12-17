package eu.fbk.dh.tint.tokenizer.token;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 1/15/13
 * Time: 2:01 PM
 * To change this templatePageCounter use File | Settings | File Templates.
 */
public class Token extends Extent {

    private static final long serialVersionUID = 1024396602591514749L;

    private String form, normForm;
    private boolean hasSpaceBefore = false;
    private boolean hasSpaceAfter = true;

    private int spaceOffset;

    public boolean hasSpaceBefore() {
        return hasSpaceBefore;
    }

    public boolean hasSpaceAfter() {
        return hasSpaceAfter;
    }

    public void setHasSpaceBefore(boolean hasSpaceBefore) {
        this.hasSpaceBefore = hasSpaceBefore;
    }

    public void setHasSpaceAfter(boolean hasSpaceAfter) {
        this.hasSpaceAfter = hasSpaceAfter;
    }

    public int getSpaceOffset() {
        return spaceOffset;
    }

    public void setSpaceOffset(int spaceOffset) {
        this.spaceOffset = spaceOffset;
    }

    public void updateByToken(Token token) {
        this.start = token.getStart();
        this.end = token.getEnd();
        this.spaceOffset = token.getSpaceOffset();
        this.hasSpaceBefore = token.hasSpaceBefore();
        this.hasSpaceAfter = token.hasSpaceAfter();
    }

    public Token(int start, int end, String form, String normForm) {
        super(start, end);
        this.form = form;
        this.normForm = normForm;
    }

    public Token(int start, int end, String form) {
        this(start, end, form, form);
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getForm() {
        return form;
    }

    public String getNormForm() {
        return normForm;
    }

    public void setNormForm(String normForm) {
        this.normForm = normForm;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Token) {
            return equals((Token) obj);
        }

        return false;
    }

    @Override
    public String toString() {
        return form + "\t"
                + spaceOffset + "\t"
                + (spaceOffset + super.getStart()) + "\t"
                + hasSpaceBefore() + "\t"
                + super.toString();
    }

}
