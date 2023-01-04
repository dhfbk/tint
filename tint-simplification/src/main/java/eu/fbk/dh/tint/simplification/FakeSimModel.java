package eu.fbk.dh.tint.simplification;

import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import eu.fbk.dh.tint.readability.GlossarioEntry;
import eu.fbk.dh.tint.readability.it.ItalianReadability;
import eu.fbk.dh.tint.readability.it.ItalianReadabilityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Created by alessio on 26/09/16.
 */
public class FakeSimModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItalianReadabilityModel.class);
    private static FakeSimModel ourInstance = null;
    private HashMap<String, GlossarioEntry> glossario = new HashMap<>();
    Map<String, Integer> frequencies = new HashMap<>();
    private HashMultimap<String, String> ff = HashMultimap.create();
//    File inputFile = new File("/Users/alessio/Documents/out-sinonimicontrari-lemmatized-noinvert.txt");

    public static FakeSimModel getInstance(Properties globalProperties, Properties localProperties, SkipModel skipModel) {
        if (ourInstance == null) {
            HashMap<String, GlossarioEntry> glossario = new HashMap<>();
            HashMultimap<String, String> ff = HashMultimap.create();
            Map<String, Integer> frequencies = new HashMap<>();

            try {
                HashMap<String, LinkedTreeMap> glossarioTmp;

                InputStream stream;
                BufferedReader reader;
                String line;

                stream = loadStream(localProperties.getProperty("lemmi"), "/Lemmi_inverso_tab.txt");
                reader = new BufferedReader(new InputStreamReader(stream));
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    String[] parts = line.split("\t");
                    frequencies.put(parts[0], Integer.parseInt(parts[1]));
                }

                // Load list of simplifications
                stream = loadStream(localProperties.getProperty("list"), "/out-sinonimicontrari-lemmatized-noinvert.txt");

                Gson gson = new GsonBuilder().create();
                glossarioTmp = gson.fromJson(new InputStreamReader(stream), HashMap.class);

                List<String> glossarioKeys = new ArrayList<>(glossarioTmp.keySet());
                Collections.sort(glossarioKeys, new ItalianReadability.StringLenComparator());

                for (String form : glossarioKeys) {
                    LinkedTreeMap linkedTreeMap = glossarioTmp.get(form);

                    ArrayList<String> arrayList = (ArrayList<String>) linkedTreeMap.get("forms");
                    String[] strings = new String[arrayList.size()];
                    strings = arrayList.toArray(strings);
                    String description = (String) linkedTreeMap.get("description");
                    GlossarioEntry entry = new GlossarioEntry(strings, description);

                    if (skipModel.getSkipList().contains(form)) {
                        continue;
                    }
                    glossario.put(form, entry);
                }

                // Load list of false friends
                stream = loadStream(localProperties.getProperty("ff"), "/ff.txt");
                reader = new BufferedReader(new InputStreamReader(stream));
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split("\t");
                    if (parts.length < 2) {
                        continue;
                    }

                    ff.put(parts[0], parts[1]);
                }
                reader.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            ourInstance = new FakeSimModel(glossario, ff, frequencies);
        }
        return ourInstance;
    }

    protected static InputStream loadStream(String property, String resourceName) {
        InputStream stream = null;
        if (property != null) {
            try {
                stream = new FileInputStream(property);
            } catch (FileNotFoundException e) {
                // continue
            }
        }
        if (stream == null) {
            stream = FakeSimModel.class.getResourceAsStream(resourceName);
        }
        return stream;
    }

    private FakeSimModel(
            HashMap<String, GlossarioEntry> glossario, HashMultimap<String, String> ff, Map<String, Integer> frequencies) {
        this.glossario = glossario;
        this.ff = ff;
        this.frequencies = frequencies;
    }

    public HashMap<String, GlossarioEntry> getGlossario() {
        return glossario;
    }

    public HashMultimap<String, String> getFf() {
        return ff;
    }

    public Map<String, Integer> getFrequencies() {
        return frequencies;
    }
}
