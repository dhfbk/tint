package eu.fbk.dh.tint.digimorph;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

import java.io.*;
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
    }

    /**
     * @param token_list list of string containing words.
     * @return list of string containing the results of the Morphological analyzer.
     * @author Giovanni Moretti
     * @version 0.4a
     */

    public List<String> getMorphology(List token_list) {
        Volume volume = null;
        volume = MappedFileVol.FACTORY.makeVolume(model_path, true);

        SortedTableMap<String, String> map = SortedTableMap.open(volume, Serializer.STRING, Serializer.STRING);

        List<String> results = new LinkedList<String>();

        int threadsNumber = Runtime.getRuntime().availableProcessors();
        List<List<String>> parts;

        parts = Lists.partition(token_list, (token_list.size() / threadsNumber) + 1);

        try {
            executor = Executors.newFixedThreadPool(parts.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        callables = new LinkedHashSet<Callable<List<String>>>();

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
            for (int i = 0; i < futures.size(); i++) {
                results.addAll(futures.get(i).get());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int pts = 0; pts < parts.size(); pts++) {
            parts.get(pts).clear();
        }

        volume.close();
        return results;
    }

    Map<String, String> mapcodgram = new HashMap<String, String>();
    Map<String, String> mapcodfless = new HashMap<String, String>();

    /**
     * This method creates or re-creates the db file with the morphology forms used by the analyzer
     *
     * @param csv_path - String contains the tsv file path
     */

    public void re_train(String csv_path, boolean include_lemma) {
        File dbf = new File(model_path);
        if (dbf.exists()) {
            dbf.delete();
        }
        fill_codgram();
        fill_codfless();
        Volume volume = MappedFileVol.FACTORY.makeVolume(model_path, false);
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

            for (Map.Entry<String, String> e : map.entrySet()) {
                sink.put(e.getKey(), e.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        SortedTableMap<String, String> stmap = sink.create();
        volume.close();

        System.out.println("done");

    }

    private void fill_codfless() {

    }

    private void fill_codgram() {

    }

}
