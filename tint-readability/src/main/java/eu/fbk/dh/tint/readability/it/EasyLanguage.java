package eu.fbk.dh.tint.readability.it;

import com.google.gson.annotations.SerializedName;

/**
 * Created by alessio on 01/06/16.
 */

public class EasyLanguage {

    public class EasySingle {

        public String[] n, v, a, r;

        public EasySingle() {
            n = new String[0];
            v = new String[0];
            a = new String[0];
            r = new String[0];
        }
    }

    public EasyLanguage() {
        level1 = new EasySingle();
        level2 = new EasySingle();
        level3 = new EasySingle();
    }

    @SerializedName("level-1") public EasySingle level1;
    @SerializedName("level-2") public EasySingle level2;
    @SerializedName("level-3") public EasySingle level3;
}
