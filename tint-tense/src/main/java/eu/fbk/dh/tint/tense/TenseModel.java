package eu.fbk.dh.tint.tense;

import eu.fbk.dh.tint.digimorph.DigiMorph;

import java.util.Set;

/**
 * Created by giovannimoretti on 10/06/16.
 */
public class TenseModel {

    private static Set<String> transitiveVerbs = null;

    public static Set<String> getInstance(String transitiveVerbsPath) {
        if (transitiveVerbs == null) {
            // load TenseModel
        }

        return transitiveVerbs;
    }

}
