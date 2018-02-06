package eu.fbk.dh.tint.derived;

public class DerivedAffixation extends DerivedPhase {

    private String affix;
    private String allomorph;
    private String mt;
    private String ms;

    public DerivedAffixation(String affix, String allomorph, String mt, String ms) {
        this.affix = affix;
        this.allomorph = allomorph;
        this.mt = mt;
        this.ms = ms;
        this.type = "affixation";
    }

    public String getAffix() {
        return affix;
    }

    public String getAllomorph() {
        return allomorph;
    }

    public String getMt() {
        return mt;
    }

    public String getMs() {
        return ms;
    }

    @Override
    public String toString() {
        return "DerivedAffixation{" +
                "affix='" + affix + '\'' +
                ", allomorph='" + allomorph + '\'' +
                ", mt='" + mt + '\'' +
                ", ms='" + ms + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
