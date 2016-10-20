package eu.fbk.dh.tint.geoloc.annotator;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by alessio on 29/09/16.
 */

public class GeocodResult {

//    public static class GeocodResultSerializer implements JsonSerializer<GeocodResult> {
//
//        @Override public JsonElement serialize(GeocodResult geocodResult, Type type,
//                JsonSerializationContext jsonSerializationContext) {
//            return jsonSerializationContext.serialize(geocodResult.getText());
//        }
//    }

    double longitude, latitude;
    int start, end;
    String text;
    JsonElement raw;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setRaw(JsonElement raw) {
        this.raw = raw;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public JsonElement getRaw() {
        return raw;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public GeocodResult(double longitude, double latitude, int start, int end) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.start = start;
        this.end = end;
    }
}
