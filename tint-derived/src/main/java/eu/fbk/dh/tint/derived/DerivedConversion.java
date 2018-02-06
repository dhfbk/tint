package eu.fbk.dh.tint.derived;

public class DerivedConversion extends DerivedPhase {

    private String convertionType;

    public DerivedConversion(String convertionType) {
        this.convertionType = convertionType;
        this.type = "conversion";
    }

    public String getConvertionType() {
        return convertionType;
    }

    @Override
    public String toString() {
        return "DerivedConversion{" +
                "convertionType='" + convertionType + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
