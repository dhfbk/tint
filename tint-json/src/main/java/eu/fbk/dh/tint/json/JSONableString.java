package eu.fbk.dh.tint.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Created by alessio on 25/09/16.
 */

public class JSONableString implements JSONable {

    private String support;

    public JSONableString(String support) {
        this.support = support;
    }

    public String getSupport() {
        return support;
    }

    @Override public String getName() {
        return "hyphenation";
    }

    @Override public JsonElement getJson(Gson gson) {
        return gson.toJsonTree(support);
    }
}
