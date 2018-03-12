package eu.fbk.dh.tint.derived;

import java.util.ArrayList;
import java.util.List;

public class Derivation {

    private String baseLemma = null;
    private String baseType = null;
    private List<DerivedPhase> phases = new ArrayList<>();

    public Derivation(String baseLemma, String baseType) {
        this.baseLemma = baseLemma;
        this.baseType = baseType;
    }

    public void addPhase(DerivedPhase phase) {
        phases.add(phase);
    }

    public String getBaseLemma() {
        return baseLemma;
    }

    public String getBaseType() {
        return baseType;
    }

    public List<DerivedPhase> getPhases() {
        return phases;
    }
}
