package eu.fbk.dh.tint.simplification;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static eu.fbk.dh.tint.simplification.FakeSimModel.loadStream;

/**
 * Created by alessio on 25/05/15.
 */

public class SkipModel {

    private static SkipModel instance;
    //    private Map<String, String> lemmaList;
    private Map<String, String> replaceList;
    private Set<String> skipList;
    private static final Logger LOGGER = LoggerFactory.getLogger(SkipModel.class);

    private SkipModel(String listFile) {
        LOGGER.trace("Loading model for Skipping");
//        lemmaList = new HashMap<>();
        replaceList = new HashMap<>();
        skipList = new HashSet<>();

        InputStream stream;
        BufferedReader reader;
        String line;

        stream = loadStream(listFile, "/skipList-replace.txt");
        reader = new BufferedReader(new InputStreamReader(stream));
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.length() == 0) {
                    continue;
                }

                String[] parts = line.split("\t+");

                switch (parts.length) {
                    case 1:
                        skipList.add(parts[0]);
//                        lemmaList.put(parts[0], null);
                        break;
                    case 2:
                        replaceList.put(parts[0], parts[1]);
//                        lemmaList.put(parts[0], parts[1]);
                        break;
                    default:
                        LOGGER.warn("The line '{}' has too many tabs", line);
                }

//                    System.out.println(line);
//                    System.out.println(parts.length);
//                    System.out.println();

//                    lemmaList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SkipModel getInstance(String listFile) {
        if (instance == null) {
            instance = new SkipModel(listFile);
        }

        return instance;
    }

    public Map<String, String> getReplaceList() {
        return replaceList;
    }

    public Set<String> getSkipList() {
        return skipList;
    }
}
