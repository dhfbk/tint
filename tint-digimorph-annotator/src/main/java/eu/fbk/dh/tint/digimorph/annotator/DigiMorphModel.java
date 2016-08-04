package eu.fbk.dh.tint.digimorph.annotator;

import eu.fbk.dh.tint.digimorph.DigiMorph;

/**
 * Created by giovannimoretti on 10/06/16.
 */
public class DigiMorphModel {

    private static DigiMorph digiMorph;

    public static DigiMorph getInstance(String model_path) {
        if (digiMorph == null) {
            digiMorph = new DigiMorph(model_path);
        }

        return digiMorph;
    }

}
