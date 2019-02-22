package eu.fbk.dh.tint.digimorph.annotator;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import eu.fbk.dh.tint.digimorph.DigiMorph;
import eu.fbk.utils.core.FrequencyHashSet;
import org.mapdb.SortedTableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GuessModel {

    class Token {
        String form;
        String lemma;
        String feats;

        @Override
        public String toString() {
            return "Token{" +
                    "form='" + form + '\'' +
                    ", lemma='" + lemma + '\'' +
                    ", feats='" + feats + '\'' +
                    '}';
        }

        public Token(String form, String lemma, String feats) {
            this.form = form;
            this.lemma = lemma;
            this.feats = feats;
        }
    }

    private static Set<String> absAdvs = new HashSet<>();

    static {
        absAdvs.add("ottimamente");
        absAdvs.add("pessimamente");
        absAdvs.add("massimamente");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GuessModel.class);
    private HashSet<String> allowedTags = new HashSet<>();
    private Map<String, RadixTree<LinkedList<String>>> trees = new HashMap<>();
    private Map<String, String> featMappings = new HashMap<>();

    public String getMorphoFeatsForContentWords(String featString) {
        String subToken = featString.replaceAll("^[^~]*~", "");
        subToken = subToken.replaceAll("^[^+]*\\+", "");
        return featMappings.get(subToken);
    }

    public void addSexMorpho(Set<String> set, String sex) {
        if (sex.equals("m")) {
            set.add("Gender=Masc");
        } else if (sex.equals("f")) {
            set.add("Gender=Fem");
        }
    }

    public void addNumMorpho(Set<String> set, String num) {
        if (num.equals("sing")) {
            set.add("Number=Sing");
        } else if (num.equals("plur")) {
            set.add("Number=Plur");
        }
    }

    public void addPersMorpho(Set<String> set, String pers) {
        if (pers.equals("1") || pers.equals("2") || pers.equals("3")) {
            set.add("Person=" + pers);
        }
    }

    public void addTypeMorpho(Set<String> set, String type) {
        if (type.equals("sup")) {
            set.add("Degree=Abs");
        }
        if (type.equals("cmp")) {
            set.add("Degree=Cmp");
        }
    }

    public String getMorphoFeats(String featString, String pos) {
        Set<String> featureSet = new TreeSet<>();
        String[] parts = featString.split("\\+");
        if (parts.length > 1) {
            switch (parts[1]) {
                case "adj":
                    addSexMorpho(featureSet, parts[2]);
                    addNumMorpho(featureSet, parts[3]);
                    if (parts.length > 4) {
                        addTypeMorpho(featureSet, parts[4]);
                    }
                    break;
                case "art":
                    addSexMorpho(featureSet, parts[2]);
                    addNumMorpho(featureSet, parts[3]);
                    break;
                case "adv":
                    // todo: add Cmp for adverbs?
                    // It seems that it is not used in UD, but maybe it should be.
                    // see https://it.wikipedia.org/wiki/Gradi_e_alterazioni_degli_avverbi
                    if (parts[0].endsWith("issimo")) {
                        featureSet.add("Degree=Abs");
                    }
                    if (absAdvs.contains(parts[0].toLowerCase())) {
                        featureSet.add("Degree=Abs");
                    }
                    break;
                case "pron":
                    addSexMorpho(featureSet, parts[3]);
                    addPersMorpho(featureSet, parts[4]);
                    addNumMorpho(featureSet, parts[5]);
                    break;
            }
        }
        switch (pos) {
            case "A":
            case "V":
            case "VA":
            case "VM":
            case "S":
                return getMorphoFeatsForContentWords(featString);
            case "AP":
                featureSet.add("Poss=Yes");
                featureSet.add("PronType=Prs");
                break;
            case "BN":
                featureSet.add("PronType=Neg");
                break;
            case "DD":
                featureSet.add("PronType=Dem");
                break;
            case "DE":
                featureSet.add("PronType=Exc");
                break;
            case "DI":
                featureSet.add("PronType=Ind");
                break;
            case "DQ":
                featureSet.add("PronType=Int");
                break;
            case "DR":
                featureSet.add("PronType=Rel");
                break;
            case "I":
                switch (parts[0].toLowerCase()) {
                    case "si":
                    case "sì":
                    case "si'":
                        featureSet.add("Polarity=Pos");
                        break;
                    case "no":
                        featureSet.add("Polarity=Neg");
                        break;
                }
                break;
            case "N":
                featureSet.add("NumType=Card");
                break;
            case "NO":
                featureSet.add("NumType=Ord");
                break;
            case "PC":
                featureSet.add("Clitic=Yes");
                featureSet.add("PronType=Prs");
                break;
            case "PD":
                featureSet.add("PronType=Dem");
                break;
            case "PE":
            case "PP":
                featureSet.add("PronType=Prs");
                break;
            case "PI":
                featureSet.add("PronType=Ind");
                break;
            case "PQ":
                featureSet.add("PronType=Int");
                break;
            case "PR":
                featureSet.add("PronType=Rel");
                break;
            case "RD":
                featureSet.add("Definite=Def");
                featureSet.add("PronType=Art");
                break;
            case "RI":
                featureSet.add("Definite=Ind");
                featureSet.add("PronType=Art");
                break;
            case "SW":
                featureSet.add("Foreign=Yes");
                break;
            case "T":
                featureSet.add("PronType=Tot");
                break;
        }

        StringBuffer buffer = new StringBuffer();
        int i = 0;
        for (String s : featureSet) {
            buffer.append(s);
            if (++i < featureSet.size()) {
                buffer.append("|");
            }
        }
        if (buffer.length() == 0) {
            buffer.append("_");
        }

        return buffer.toString();
    }

    public GuessModel() {
        this(null);
    }

    public GuessModel(String guessModelPath) {

        HashMap<String, String> uMap = new HashMap<>();
        uMap.put("v", "VERB");
        uMap.put("adv", "ADV");
        uMap.put("adj", "ADJ");
        uMap.put("n", "NOUN");
        allowedTags.add("VERB");
        allowedTags.add("NOUN");
        allowedTags.add("ADJ");
        allowedTags.add("ADV");

        List<String> lines;
        try {
            if (guessModelPath == null) {
                URL adjResource = Resources.getResource("feat-mappings.txt");
                lines = Resources.readLines(adjResource, Charsets.UTF_8);
            } else {
                lines = Files.readLines(new File(guessModelPath), Charsets.UTF_8);
            }
            for (String line : lines) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    continue;
                }
                featMappings.put(parts[0], parts[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String allowedTag : allowedTags) {
            trees.put(allowedTag, new ConcurrentRadixTree<>(new DefaultCharArrayNodeFactory()));
        }

        DigiMorph digiMorph = new DigiMorph();
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
                String feats = getMorphoFeatsForContentWords(word);
                if (feats == null) {
                    continue;
                }

                String token = key.toLowerCase();
                String lemma = parts[0].toLowerCase();
                String reverse_token = new StringBuilder(token).reverse().toString();

                LinkedList<String> features = new LinkedList<>();
                features.add(token);
                features.add(lemma);
                features.add(feats);

                trees.get(uMap.get(ePos)).put(reverse_token, features);
            }
        }
    }

    public Token guess(String token, String pos) {
        String reverse_sample_query = new StringBuilder(token).reverse().toString();
        FrequencyHashSet<String> values = new FrequencyHashSet<>();
        Iterable<LinkedList<String>> closestForms = trees.get(pos).getValuesForClosestKeys(reverse_sample_query);
        for (LinkedList<String> s : closestForms) {
            values.add(s.get(2));
        }

        String guess = values.mostFrequent();
        String guessed_lemma = token;
        for (LinkedList<String> closestForm : closestForms) {
            String feat = closestForm.get(2);
            if (feat.equals(guess)) {
                String lemma = closestForm.get(1).toLowerCase();
                String form = closestForm.get(0).toLowerCase();

                int min = Math.min(form.length(), lemma.length());

                for (int i = 0; i < min; i++) {
                    char charForm = form.charAt(i);
                    char charLemma = lemma.charAt(i);
                    if (charForm != charLemma || i == min - 1) {
                        String postfix = lemma.substring(i);
                        int length = token.length() - form.length();
                        if (length < 0) {
                            break;
                        }
                        String prefix = token.substring(0, i + length);
                        guessed_lemma = prefix + postfix;
                        break;
                    }
                }
                break;
            }
        }

        return new Token(token, guessed_lemma, guess);
    }

    public static void main(String[] args) {
        GuessModel model = new GuessModel();
//        System.out.println(model.guess("smerdazzi", "NOUN"));
        System.out.println(model.guess("sparacchio", "VERB"));
    }
}
