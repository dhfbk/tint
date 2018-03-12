package eu.fbk.dh.tint.derived;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class DerivedModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(DerivedModel.class);
    private static DerivedModel ourInstance = null;
    private Map<String, Derivation> derivations;

    public static DerivedModel getInstance() {
        if (ourInstance == null) {
            ourInstance = new DerivedModel();
        }

        return ourInstance;
    }

    private DerivedModel() {
        LOGGER.info("Loading derivatario");
        InputStream derivatarioStream = this.getClass().getResourceAsStream("/derivatario.csv");
        Reader in = new InputStreamReader(derivatarioStream);
        Iterable<CSVRecord> records = null;
        derivations = new HashMap<>();

        try {
            records = CSVFormat.DEFAULT.parse(in);
        } catch (IOException e) {
            return;
        }

        for (CSVRecord record : records) {

            String lemma = record.get(1).trim().toLowerCase();
            String[] baseParts = record.get(2).trim().split(":");

            String baseLemma = null;
            String baseType = null;
            if (!baseParts[0].toLowerCase().equals("baseless")) {
                baseLemma = baseParts[0].toLowerCase();
                baseType = baseParts[1].toLowerCase();
            }

            Derivation derivation = new Derivation(baseLemma, baseType);

            for (int i = 3; i < record.size(); i++) {
                String value = record.get(i).trim();
                if (value.length() == 0) {
                    break;
                }

                String[] parts = value.split(":");
                DerivedPhase phase;
                if (parts[0].toLowerCase().equals("conversion")) {
                    phase = new DerivedConversion(parts[1].toLowerCase());
                } else {
                    try {
                        phase = new DerivedAffixation(
                                parts[0].toLowerCase(),
                                parts[1].toLowerCase(),
                                parts[2].toLowerCase(),
                                parts[3].toLowerCase()
                        );
                    } catch (Exception e) {
                        LOGGER.error("Error in {}", value);
                        continue;
                    }
                }

                derivation.addPhase(phase);
            }

//            if (derivations.containsKey(lemma)) {
//                System.out.println("Lemma already exists: " + lemma);
//            }

            derivations.put(lemma, derivation);
        }
    }

    public Map<String, Derivation> getDerivations() {
        return derivations;
    }

    public static void main(String[] args) {
        DerivedModel instance = DerivedModel.getInstance();
    }
}