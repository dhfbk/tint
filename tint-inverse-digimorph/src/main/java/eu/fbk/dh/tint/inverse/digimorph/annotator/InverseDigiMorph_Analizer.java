package eu.fbk.dh.tint.inverse.digimorph.annotator;

import org.mapdb.SortedTableMap;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by giovannimoretti on 31/01/17.
 */

public class InverseDigiMorph_Analizer implements Callable<List<String>> {

    // Volume volume = null;
    private SortedTableMap<String, String> map = null;

    List<String> morphos;

    public InverseDigiMorph_Analizer(List<String> morphos, SortedTableMap<String, String> map) {
        this.morphos = morphos;
        this.map = map;
    }

    public List<String> call() {
        List<String> results = new LinkedList<String>();
        for (String s : this.morphos) {
            results.add(getInverseMorphology(s));
        }
        return results;
    }

    public String getInverseMorphology(String morpho) {


        String output = "";
        String form = map.get((morpho));
        output = form != null ? form : morpho.split("\\+")[0];
        return output.trim();


    }



}