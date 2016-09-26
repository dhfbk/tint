package eu.fbk.dh.tint.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Created by alessio on 23/09/16.
 */
public interface JSONable {

    String getName();

    default JsonElement getJson(Gson gson) {
        return gson.toJsonTree(this);
    }
}
