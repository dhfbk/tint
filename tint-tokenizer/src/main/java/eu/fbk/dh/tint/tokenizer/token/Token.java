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
    private boolean preceedBySpace = false;

    private int spaceOffset;

    public boolean isPreceedBySpace() {
        return preceedBySpace;
    }

    public void setPreceedBySpace(boolean preceedBySpace) {
        this.preceedBySpace = preceedBySpace;
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
//        this.form = token.getForm();
//        this.normForm = token.getNormForm();
//        this.afterNewLine = token.isAfterNewLine();
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
                + isPreceedBySpace() + "\t"
                + super.toString();
    }

}
