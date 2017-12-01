package eu.fbk.dh.tint.digimorph.annotator;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import eu.fbk.dh.tint.digimorph.DigiMorph;
import eu.fbk.utils.core.FrequencyHashSet;
import org.mapdb.SortedTableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(GuessModel.class);
    private HashSet<String> allowedTags = new HashSet<>();
    private Map<String, RadixTree<LinkedList<String>>> trees = new HashMap<>();
    private Map<String, String> featMappings = new HashMap<>();

    public GuessModel() {

        HashMap<String, String> uMap = new HashMap<>();
        uMap.put("v", "VERB");
        uMap.put("adv", "ADV");
        uMap.put("adj", "ADJ");
        uMap.put("n", "NOUN");
        allowedTags.add("VERB");
        allowedTags.add("NOUN");
        allowedTags.add("ADJ");
        allowedTags.add("ADV");

        URL adjResource = Resources.getResource("feat-mappings.txt");
        try {
            for (String line : Resources.readLines(adjResource, Charsets.UTF_8)) {
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
                String subToken = word.replaceAll("^[^~]*~", "");
                subToken = subToken.replaceAll("^[^+]*\\+", "");
                String feats = featMappings.get(subToken);
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
