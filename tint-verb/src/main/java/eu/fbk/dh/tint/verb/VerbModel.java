package eu.fbk.dh.tint.verb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alessio on 26/09/16.
 */
public class VerbModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerbModel.class);
    private static VerbModel ourInstance = null;
    Set<String> transitiveVerbs;

    public static VerbModel getInstance() {
        return getInstance(null);
    }

    public static VerbModel getInstance(String fileName) {
        Set<String> transitiveVerbs = new HashSet<>();
        if (ourInstance == null) {

            try {
                InputStream configStream;

                if (fileName == null) {
                    configStream = VerbModel.class.getResourceAsStream("/transitivi.txt");
                } else {
                    configStream = new FileInputStream(fileName);
                }
                if (configStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(configStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() == 0) {
                            continue;
                        }
                        if (line.startsWith("#")) {
                            continue;
                        }
                        transitiveVerbs.add(line);
                    }
                    reader.close();
                }
                ourInstance = new VerbModel(transitiveVerbs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.info("Readability model already loaded");
        }
        return ourInstance;
    }

    private VerbModel(Set<String> transitiveVerbs) {
        this.transitiveVerbs = transitiveVerbs;
    }

    public Set<String> getTransitiveVerbs() {
        return transitiveVerbs;
    }

}
