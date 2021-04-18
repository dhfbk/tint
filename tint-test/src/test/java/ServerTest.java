import eu.fbk.dh.tint.runner.TintServer;

import java.util.Properties;

public class ServerTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("annotators", "ita_toksent");
        TintServer server = new TintServer("0.0.0.0", 8012, null, properties);
    }
}
