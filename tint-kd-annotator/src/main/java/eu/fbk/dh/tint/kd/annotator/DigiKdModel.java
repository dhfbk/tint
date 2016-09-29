package eu.fbk.dh.tint.kd.annotator;

import eu.fbk.dh.kd.lib.KD_core;

/**
 * Created by giovannimoretti on 14/09/16.
 */
public class DigiKdModel {

    private static KD_core kd;

    public static KD_core getInstance(KD_core.Threads t) {
        if (kd == null) {
            kd = new KD_core(t);
        }

        return kd;
    }

}
