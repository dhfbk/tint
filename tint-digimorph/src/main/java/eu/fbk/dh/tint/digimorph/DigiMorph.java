package eu.fbk.dh.tint.digimorph;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Giovanni Moretti at Digital Humanities group at FBK.
 * @version 0.4a
 */
public class DigiMorph {

    String model_path = "";
    ExecutorService executor = null;
    List<Future<List<String>>> futures = null;

    Set<Callable<List<String>>> callables = new HashSet<Callable<List<String>>>();
    Volume volume = null;
    SortedTableMap<String, String> map = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(DigiMorph.class);


    public static String getVersion() {
        return DigiMorph.class.getPackage().getImplementationTitle() + "\n"
                + DigiMorph.class.getPackage().getSpecificationVendor() + " - "
                + DigiMorph.class.getPackage().getImplementationVendor() + "\n"
                + "Version: " + DigiMorph.class.getPackage().getSpecificationVersion();
    }

    public DigiMorph() {
        this(null);
    }

    public DigiMorph(String model_path) {
        if (model_path == null) {
            try {
                File file = File.createTempFile("mapdb", "mapdb");
                file.deleteOnExit();
                byte[] bytes = Resources.toByteArray(Resources.getResource("italian.db"));
                Files.write(file.toPath(), bytes);
                model_path = file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.model_path = model_path;
        volume = MappedFileVol.FACTORY.makeVolume(model_path, true);
        this.map = SortedTableMap.open(volume, Serializer.STRING, Serializer.STRING);

    }

    public SortedTableMap<String, String> getMap() {
        return map;
    }

    /**
     * @param token_list list of string containing words.
     * @return list of string containing the results of the Morphological analyzer.
     * @author Giovanni Moretti
     * @version 0.42a
     */

    synchronized public List<String> getMorphology(List<String> token_list) {
        List<String> results = new LinkedList<>();
        int threadsNumber = Runtime.getRuntime().availableProcessors();
        List<List<String>> parts = Lists.partition(token_list, (token_list.size() / threadsNumber) + 1);

        if (token_list.size() > 0) {
            executor = Executors.newFixedThreadPool(parts.size());
        } else {
            LOGGER.warn("No tokens to the morphological analyzer");
            return results;
        }

        callables = new LinkedHashSet<>();

        for (int pts = 0; pts < parts.size(); pts++) {
            callables.add(new DigiMorph_Analizer(parts.get(pts), map));
        }

        try {

            futures = executor.invokeAll(callables);

            executor.shutdown();
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

            executor.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (Future<List<String>> future : futures) {
                List<String> stringList = future.get();
                results.addAll(stringList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        for (int pts = 0; pts < parts.size(); pts++) {
//            parts.get(pts).clear();
//        }

        return results;
    }

//    Map<String, String> mapcodgram = new HashMap<String, String>();
//    Map<String, String> mapcodfless = new HashMap<String, String>();

    /**
     * This method creates or re-creates the db file with the morphology forms used by the analyzer
     *
     * @param csv_path - String contains the tsv file path
     */

    public static void re_train(File csv_path, File output, boolean include_lemma) {
        if (output.exists()) {
            output.delete();
        }
        Volume volume = MappedFileVol.FACTORY.makeVolume(output.getAbsolutePath(), false);
        SortedTableMap.Sink<String, String> sink =
                SortedTableMap.create(
                        volume,
                        Serializer.STRING, // key serializer
                        Serializer.STRING   // value serializer
                )
                        .pageSize(64 * 1024)
                        .nodeSize(8)
                        .createFromSink();

        SortedMap<String, String> map = new TreeMap<String, String>();
        try {
            Reader in = new FileReader(csv_path);
            Iterable<CSVRecord> records = CSVFormat.TDF.withIgnoreEmptyLines().withQuote('≥').parse(in);
            for (CSVRecord record : records) {

                String feature = record.get(2);
                String lemma = record.get(1);
                String forma = record.get(0).toLowerCase();
                if (!map.containsKey(forma)) {
                    map.put(forma, "");
                }
                if (lemma == null) {
                    lemma = "";
                }

                if (include_lemma) {
                    map.put(forma, map.get(forma) + " " + lemma + "+" + feature);
                } else {
                    map.put(forma, map.get(forma) + " " + feature);
                }
            }

            // put imp in last position
            // todo: we can do it better
            for (Map.Entry<String, String> e : map.entrySet()) {
                if (e.getValue().contains("+imp+")) {
                    String newMorph = "";
                    String[] morphItems = e.getValue().split(" ");
                    String imp = "";
                    for (String m : morphItems) {
                        if (m.contains("+imp+")) {
                            imp = m;
                        } else {
                            newMorph += m + " ";
                        }
                    }
                    newMorph += imp;
                    e.setValue(newMorph);
                }
            }

            for (Map.Entry<String, String> e : map.entrySet()) {
                sink.put(e.getKey(), e.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        SortedTableMap<String, String> stmap = sink.create();
        //volume.close();

        System.out.println("done");

    }
}
