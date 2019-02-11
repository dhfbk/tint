package eu.fbk.dh.tint.readability.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class CalculateParameters {
    public static Set<String> values = new HashSet<>();

    static {
        values.add("ttrValue");
        values.add("deepAvg");
        values.add("deepMax");
        values.add("density");
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./calculate-values")
                    .withHeader("Calculate values for online demo")
                    .withOption("e", "input-easy", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("d", "input-difficult", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputEasyFolder = cmd.getOptionValue("input-easy", File.class);
            File inputDiffFolder = cmd.getOptionValue("input-difficult", File.class);

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, depparse, readability");
            properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, readability");
            properties.setProperty("customAnnotatorClass.readability", "eu.fbk.dh.tint.readability.ReadabilityAnnotator");
            properties.setProperty("readability.language", "en");
            properties.setProperty("readability.ttrLimit", "100");
            StanfordCoreNLP coreNLP = new StanfordCoreNLP(properties);

            Map<String, Double> sums = new HashMap<>();
            for (String value : values) {
                sums.putIfAbsent(value, 0.0d);
            }
            sums.putIfAbsent("main", 0.0d);

            int count = 0;

            Map<String, File> list = new HashMap<>();
            list.put("easy", inputEasyFolder);
            list.put("difficult", inputDiffFolder);

            int limit = 0;

            for (Map.Entry<String, File> entry : list.entrySet()) {
                int fileCount = 0;
                for (File file : entry.getValue().listFiles()) {
                    if (file.isDirectory()) {
                        continue;
                    }
                    if (limit > 0 && ++fileCount > limit) {
                        continue;
                    }
                    System.out.println(file.getAbsolutePath());

                    Annotation annotation = new Annotation(Files.toString(file, Charsets.UTF_8));
                    coreNLP.annotate(annotation);
                    Readability readability = annotation.get(ReadabilityAnnotations.ReadabilityAnnotation.class);
//                    int tokenCount = annotation.get(CoreAnnotations.TokensAnnotation.class).size();
                    int tokenCount = 1;

                    Double v;

                    v = sums.get("ttrValue");
                    v += readability.getTtrValue() * tokenCount;
                    sums.put("ttrValue", v);

//                    v = sums.get("deepAvg");
//                    v += readability.getDeepAvg() * tokenCount;
//                    sums.put("deepAvg", v);
//
//                    v = sums.get("deepMax");
//                    v += readability.getDeepMax() * tokenCount;
//                    sums.put("deepMax", v);

                    v = sums.get("density");
                    v += readability.getDensity() * tokenCount;
                    sums.put("density", v);

                    count += tokenCount;
                }

                System.out.println(entry.getKey());
                System.out.println(count);
                for (String key : sums.keySet()) {
                    System.out.println(key);
                    System.out.println(sums.get(key));
                    System.out.println(sums.get(key) / (count * 1.0));
                }
                System.out.println();


            }


        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
