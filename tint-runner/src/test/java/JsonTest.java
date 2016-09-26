import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alessio on 23/09/16.
 */

public class JsonTest {

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().create();

        Map<Integer, String> map = new HashMap<>();
        map.put(1, "Ciao");
        map.put(2, "Buonasera");

        System.out.println(gson.toJson(map).toString());
    }

}
