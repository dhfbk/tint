package eu.fbk.dh.tint.digimorph.annotator;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import eu.fbk.dh.tint.digimorph.DigiMorph;
import eu.fbk.utils.core.FrequencyHashSet;
import org.mapdb.SortedTableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;


public class ModelHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelHelper.class);

    public static void main(String[] args) {

        File conllFile = new File("/Volumes/Dati/Resources/ud-treebanks-v2.1/UD_Italian/it-ud-train.conllu");

        Set<String> detAdj = new HashSet<>();
        URL adjResource = Resources.getResource("det-adj.txt");
        try {
            for (String line : Resources.readLines(adjResource, Charsets.UTF_8)) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                detAdj.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        DigiMorph digiMorph = new DigiMorph();
        Map<String, FrequencyHashSet<String>> formToFeats = new HashMap<>();
        Map<String, Map<String, FrequencyHashSet<String>>> formToForms = new HashMap<>();
//        HashMultimap<String, String> formToForms = HashMultimap.create();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(conllFile));

            List<String> forms = new ArrayList<>();
            List<String> feats = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                if (parts.length < 10) {
                    continue;
                }

                forms.add(parts[1]);
                feats.add(parts[5]);
            }

            List<String> morpho = digiMorph.getMorphology(forms);

            for (int i = 0; i < morpho.size(); i++) {
                String mor = morpho.get(i);
                String fea = feats.get(i);
                String form = forms.get(i);

                String[] words = mor.split("[\\s/]+");
                if (words.length > 2) {
                    continue;
                }
                for (String word : words) {
                    String[] parts = word.split("\\+");

                    if (parts.length < 2) {
                        continue;
                    }

                    if (detAdj.contains(parts[0])) {
                        continue;
                    }

                    String subToken = word.replaceAll("^[^~]*~", "");
                    subToken = subToken.replaceAll("^[^+]*\\+", "");

                    formToFeats.putIfAbsent(subToken, new FrequencyHashSet<>());
                    formToFeats.get(subToken).add(fea);
                    formToForms.putIfAbsent(subToken, new HashMap<>());
                    formToForms.get(subToken).putIfAbsent(form, new FrequencyHashSet<>());
                    formToForms.get(subToken).get(form).add(fea);
                }
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        HashMap<String, String> uMap = new HashMap<>();
        uMap.put("v", "VERB");
        uMap.put("adv", "ADV");
        uMap.put("adj", "ADJ");
        uMap.put("n", "NOUN");

        Set<String> featsList = new HashSet<>();
//                Map<String, String> featMap = new HashMap<>();

        SortedTableMap<String, String> map = digiMorph.getMap();
        Iterator<String> gmIterator = map.keyIterator();
        while (gmIterator.hasNext()) {
            String key = gmIterator.next();
            String value = map.get(key).trim();
            String[] words = value.split("[\\s/]+");
            for (String word : words) {
                String[] parts = word.split("\\+");

                if (parts.length < 2) {
                    continue;
                }
                String ePos = parts[1];
                if (ePos.length() == 0) {
                    continue;
                }
                if (!uMap.keySet().contains(ePos)) {
                    continue;
                }

                // Feats
                String subToken = word.replaceAll("^[^~]*~", "");
                subToken = subToken.replaceAll("^[^+]*\\+", "");
                featsList.add(subToken);

//                String token = key.toLowerCase();
//                String lemma = parts[0].toLowerCase();
//                String reverse_token = new StringBuilder(token).reverse().toString();
//                LinkedList<String> features = new LinkedList<>();
//                features.add(lemma);
//                features.add(token);
            }
        }

        for (String feats : featsList) {
            System.out.println(feats);
            if (formToFeats.get(feats) != null) {
                String mostFrequent = formToFeats.get(feats).mostFrequent();
                System.out.println("Most frequent: " + mostFrequent);
                System.out.println("Frequency: " + formToFeats.get(feats).get(mostFrequent) + "/" + formToFeats.get(feats).sum());
                System.out.println(formToFeats.get(feats));
                System.out.println(formToForms.get(feats));
            }
            System.out.println();
        }

//        System.out.println(featsList);
//        System.out.println(featsList.size());
    }
}
