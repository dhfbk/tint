package eu.fbk.dh.tint.inverse.digimorph.annotator;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by giovannimoretti on 31/01/17.
 */

public class InverseDigiMorph {

    String model_path = "";
    ExecutorService executor = null;
    List<Future<List<String>>> futures = null;

    Set<Callable<List<String>>> callables = new HashSet<Callable<List<String>>>();
    Volume volume = null;
    SortedTableMap<String, String> map = null;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(eu.fbk.dh.tint.inverse.digimorph.annotator.InverseDigiMorph.class);

    public InverseDigiMorph() {
        this(null);
    }

    public InverseDigiMorph(String model_path) {
        if (model_path == null) {
            try {
                File file = File.createTempFile("mapdb", "mapdb");
                file.deleteOnExit();
                byte[] bytes = Resources.toByteArray(Resources.getResource("inverse-italian.db"));
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

    /**
     * @param morphology string containing morphologies in EAGLE format.
     * @return result of the Inverse Morphological analyzer.
     * @author Giovanni Moretti
     * @version 0.42a
     */

    synchronized public String getInverseMorphology(String morphology) {
        List token_list = new ArrayList();
        token_list.add(morphology);
        List<String> inverseMorphology = getInverseMorphology(token_list);
        return inverseMorphology.get(0);
    }

    /**
     * @param morpho_list list of string containing morphologies in EAGLE format.
     * @return list of string containing the results of the Inverse Morphological analyzer.
     * @author Giovanni Moretti
     * @version 0.42a
     */

    synchronized public List<String> getInverseMorphology(List morpho_list) {
        List<String> results = new LinkedList<String>();
        List<List<String>> parts;
        int threadsNumber = Runtime.getRuntime().availableProcessors();
        //int threadsNumber = 1;
        parts = Lists.partition(morpho_list, (morpho_list.size() / threadsNumber) + 1);

        if (morpho_list.size() > 0) {
            executor = Executors.newFixedThreadPool(parts.size());
        } else {
            LOGGER.warn("No tokens to the morphological analyzer");
            return results;
        }

        callables = new LinkedHashSet<Callable<List<String>>>();

        for (int pts = 0; pts < parts.size(); pts++) {
            callables.add(new InverseDigiMorph_Analizer(parts.get(pts), map));
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
                List<String> stringList = futures.get(i).get();
                results.addAll(stringList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int pts = 0; pts < parts.size(); pts++) {
            parts.get(pts).clear();
        }

        return results;
    }

}
