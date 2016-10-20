package eu.fbk.dh.tint.kd.annotator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import eu.fbk.dh.tint.json.JSONExclude;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovannimoretti on 24/05/16.
 */
public class DigiKdResult {
    String keyphrase;
    Integer frequency;
    Double score;
    ArrayList<String> lemmas = new ArrayList<>();
    ArrayList<String> tokens = new ArrayList<>();
    List<String> posList = new ArrayList<>();

//    class ResultSerializer implements JsonSerializer<DigiKdResult> {
//
//        @Override public JsonElement serialize(DigiKdResult digiKdResult, Type type,
//                JsonSerializationContext jsonSerializationContext) {
//            JsonObject element = new JsonObject();
//            element.addProperty("keyphrase", digiKdResult.getKeyphrase());
//            element.addProperty("frequency", digiKdResult.getFrequency());
//            element.addProperty("score", digiKdResult.getScore());
//            return element;
//        }
//    }

    public DigiKdResult(String keyphrase, Integer frequency, Double score, ArrayList<String> lemmas, ArrayList<String> tokens,List<String> posList) {
        this.keyphrase = keyphrase;
        this.frequency = frequency;
        this.score = score;
        this.lemmas = lemmas;
        this.tokens = tokens;
        this.posList = posList;
    }

    public void setPosList(List<String> posList) {
        this.posList = posList;
    }

    public List<String> getPosList() {

        return posList;
    }

    public String getKeyphrase() {
        return keyphrase;
    }

    public void setKeyphrase(String keyphrase) {
        this.keyphrase = keyphrase;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }


    public void setLemmas(ArrayList<String> lemmas) {
        this.lemmas = lemmas;
    }

    public void setTokens(ArrayList<String> tokens) {
        this.tokens = tokens;
    }

    public ArrayList<String> getLemmas() {
        return lemmas;
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public String toString(){
        return keyphrase+"\t"+"<"+frequency+","+score+">";
    }
}
