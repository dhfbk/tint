package eu.fbk.dh.tint.readability;

import com.google.common.collect.HashMultimap;
import com.google.gson.*;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;


public class ParseDeMauro {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseDeMauro.class);

    static class HashMultimapSerializer implements JsonSerializer<HashMultimap> {
        @Override
        public JsonElement serialize(HashMultimap hashMultimap, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject object = new JsonObject();
            for (Object key : hashMultimap.keySet()) {
                object.add(key.toString(), jsonSerializationContext.serialize(hashMultimap.get(key)));
            }

            return object;
        }
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./parse-demauro")
                    .withHeader(
                            "Parse HTML file extracted from De Mauro")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            HashMultimap<String, String> level1 = HashMultimap.create();
            HashMultimap<String, String> level2 = HashMultimap.create();
            HashMultimap<String, String> level3 = HashMultimap.create();

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("Grassetto")) {
                    addWordToLevel(level1, line.replaceAll("</?span[^>]*>", ""));
                } else if (line.contains("Corsivo")) {
                    addWordToLevel(level3, line.replaceAll("</?span[^>]*>", ""));
                } else {
                    addWordToLevel(level2, line.replaceAll("</?span[^>]*>", ""));
                }

            }
            reader.close();

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gsonBuilder.registerTypeAdapter(HashMultimap.class, new ParseDeMauro.HashMultimapSerializer());
            Gson gson = gsonBuilder.create();

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("level-1", gson.toJsonTree(level1));
            jsonObject.add("level-2", gson.toJsonTree(level2));
            jsonObject.add("level-3", gson.toJsonTree(level3));

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(gson.toJson(jsonObject));
            writer.close();

//            System.out.println(level1);
//            System.out.println(level2);
//            System.out.println(level3);

            // bleah

//            Set<String> l1 = new HashSet<>();
//            l1.addAll(level1.values());
//            Set<String> l2 = new HashSet<>();
//            l2.addAll(level2.values());
//            Set<String> l3 = new HashSet<>();
//            l3.addAll(level3.values());
//
//            String dataFile = "/Volumes/Dati/Dropbox/simplification/dataset/demauro-stats.txt";
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile));
//
//            Set<String> files = new HashSet<>();
//            files.add("/Volumes/Dati/Dropbox/simplification/dataset/sara-raw/clic.tsv");
//            files.add("/Volumes/Dati/Dropbox/simplification/dataset/sara-raw/simpitiki.tsv");
//            files.add("/Volumes/Dati/Dropbox/simplification/dataset/sara-raw/teacher.tsv");
//            files.add("/Volumes/Dati/Dropbox/simplification/dataset/sara-raw/terence-t0.5.tsv");
//
//            TintPipeline pipeline = new TintPipeline();
//            pipeline.loadDefaultProperties();
//            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma");
//            pipeline.load();
//
//            for (String file : files) {
//                File f = new File(file);
//
//                int c1o = 0, c2o = 0, c3o = 0;
//                int c1s = 0, c2s = 0, c3s = 0;
//
//                BufferedReader fr = new BufferedReader(new FileReader(f));
//                while ((line = fr.readLine()) != null) {
//                    line = line.trim();
//                    String[] parts = line.split("\t");
//                    if (parts.length < 2) {
//                        continue;
//                    }
//
//                    Annotation origAnn = pipeline.runRaw(parts[0]);
//                    Annotation simpAnn = pipeline.runRaw(parts[1]);
//
//                    for (CoreLabel token : origAnn.get(CoreAnnotations.TokensAnnotation.class)) {
//                        if (l1.contains(token.lemma().toLowerCase())) {
//                            c1o++;
//                        }
//                        if (l2.contains(token.lemma().toLowerCase())) {
//                            c2o++;
//                        }
//                        if (l3.contains(token.lemma().toLowerCase())) {
//                            c3o++;
//                        }
//                    }
//                    for (CoreLabel token : simpAnn.get(CoreAnnotations.TokensAnnotation.class)) {
//                        if (l1.contains(token.lemma().toLowerCase())) {
//                            c1s++;
//                        }
//                        if (l2.contains(token.lemma().toLowerCase())) {
//                            c2s++;
//                        }
//                        if (l3.contains(token.lemma().toLowerCase())) {
//                            c3s++;
//                        }
//                    }
//                }
//
////                System.out.println(file);
////                System.out.printf("%10d %10d %10d\n", c1o, c2o, c3o);
////                System.out.printf("%10d %10d %10d\n", c1s, c2s, c3s);
////                System.out.println();
//
//                writer.append(file).append("\n");
//                writer.append(String.format("%10d %10d %10d", c1o, c2o, c3o)).append("\n");
//                writer.append(String.format("%10d %10d %10d", c1s, c2s, c3s)).append("\n");
//                writer.append("\n");
//
//                fr.close();
//            }
//
//            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    private static void addWordToLevel(HashMultimap<String, String> level, String s) {
        String firstPart = s.replaceAll("\\s.*", "");
        String secondPart = s.replaceAll("^[^\\s]*\\s", "");
        if (firstPart.length() < 3) {
            return;
        }

        if (secondPart.contains(" s.") || secondPart.startsWith("s.")) {
            level.put("n", firstPart);
        }
        if (secondPart.contains(" avv.") || secondPart.startsWith("avv.")) {
            level.put("r", firstPart);
        }
        if (secondPart.contains(" agg.") || secondPart.startsWith("agg.")) {
            level.put("a", firstPart);
        }
        if (secondPart.contains(" v.") || secondPart.startsWith("v.")) {
            level.put("v", firstPart);
        }
    }
}
