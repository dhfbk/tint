package eu.fbk.dh.tint.tokenizer.token;

/**
 * Created with IntelliJ IDEA.
 * User: giuliano
 * Date: 1/15/13
 * Time: 2:00 PM
 * To change this templatePageCounter use File | Settings | File Templates.
 */
public class Extent {

    private static final long serialVersionUID = 5024396602591514710L;
    protected int start, end;

    /**
     * Constructs an empty Extent Object.
     */
    public Extent() {
        this(0, 0);
    } // end constructor

    /**
     * Constructs a new Extent Object.
     *
     * @param start start of extent.
     * @param end   end of extent.
     */
    public Extent(int start, int end) {
        this.start = start;
        this.end = end;
    } // end constructor

    /**
     * Return the start of a extent.
     *
     * @return the start of a extent.
     */
    public int getStart() {
        return start;
    }

    /**
     * Return the end of a extent.
     *
     * @return the end of a extent.
     */
    public int getEnd() {
        return end;
    }

    /**
     * Returns the length of this extent.
     *
     * @return the length of the extent.
     */
    public int length() {
        return end - start;
    }

    /**
     * Returns <code>true</code> if the specified extent follows by this extent.
     * Identical extents are considered to contain each otherPageCounter.
     *
     * @param s The extent to compare with this extent.
     * @return <code>true</code> if the specified extent follows this extent; <code>false</code> otherwise.
     */
    public boolean follows(Extent s) {
        return (start > s.getEnd());
    } // end follows

    /**
     * Returns <code>true</code> if the specified extent precedes by this extent.
     * Identical extents are considered to contain each otherPageCounter.
     *
     * @param s The extent to compare with this extent.
     * @return <code>true</code> if the specified extent precedes this extent; <code>false</code> otherwise.
     */
    public boolean precedes(Extent s) {
        return (end < s.getStart());
    } // end precedes

    /**
     * Returns <code>true</code> if the specified extent is contained by this extent.
     * Identical extents are considered to contain each otherPageCounter.
     *
     * @param s The extent to compare with this extent.
     * @return <code>true</code> if the specified extent is contained by this extent; <code>false</code> otherwise.
     */
    public boolean contains(Extent s) {
        return (start <= s.getStart() && s.getEnd() <= end);
    }

    /**
     * Returns <code>true</code> if the specified extent intersects with this extent.
     *
     * @param s The extent to compare with this extent.
     * @return <code>true</code> if the extents overlap; <code>false</code> otherwise.
     */
    public boolean intersects(Extent s) {
        int sstart = s.getStart();
        //either s's start is in this or this' start is in s
        return (this.contains(s) || s.contains(this) ||
                (start <= sstart && sstart < end || sstart <= start && start < s.getEnd()));
    }

    /**
     * Returns <code>true</code> is the specified extent crosses this extent.
     *
     * @param s The extent to compare with this extent.
     * @return <code>true</code> if the specified extent overlaps this extent and contains a non-overlapping section; <code>false</code> otherwise.
     */
    public boolean crosses(Extent s) {
        int sstart = s.getStart();
        //either s's start is in this or this' start is in s
        return (!this.contains(s) && !s.contains(this) &&
                (start <= sstart && sstart < end || sstart <= start && start < s.getEnd()));
    }

    //
    public int compareTo(Extent o) {
        if (end < o.getStart()) {
            return -1;
        } else if (start > o.getEnd()) {
            return 1;
        }

        return 0;
    } // end compareTo

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        Extent s = (Extent) o;
        return (start == s.getStart() && end == s.getEnd());
    } // end equals

    @Override
    public String toString() {
        return start + "\t" + end;
    }

}
