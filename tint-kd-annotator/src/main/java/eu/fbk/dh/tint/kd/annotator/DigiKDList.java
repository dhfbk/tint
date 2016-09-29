package eu.fbk.dh.tint.kd.annotator;

import eu.fbk.dh.tint.json.JSONable;

import java.util.ArrayList;

/**
 * Created by alessio on 27/09/16.
 */

public class DigiKDList<T> extends ArrayList<T> implements JSONable {

    @Override public String getName() {
        return "keywords";
    }

}
