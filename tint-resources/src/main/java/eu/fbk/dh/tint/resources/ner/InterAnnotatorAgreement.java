package eu.fbk.dh.tint.resources.ner;

import eu.fbk.utils.core.FrequencyHashSet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterAnnotatorAgreement {
    public static void main(String[] args) {
        String inputFolder = "/Users/alessio/Downloads/annotazioni/out-20211220/tutti";
        String outputFolder = "/Users/alessio/Downloads/annotazioni/out-20211220/tutti-out";
        Pattern pattern = Pattern.compile("^.*-([0-9]+)_([a-zA-Z0-9]+).tsv$");

        HashMap<String, Integer> conversionMap = new HashMap<>();
        conversionMap.put("O", 0);
        conversionMap.put("PER", 1);
        conversionMap.put("LOC", 2);
        conversionMap.put("ORG", 3);

        HashMap<String, HashMap<String, List<String>>> texts = new HashMap<>();
        Map<String, HashMap<String, ArrayList<Integer>>> results = new HashMap<>();

        try {
            Files.walk(Paths.get(inputFolder))
                    .filter(Files::isRegularFile)
                    .filter(c -> c.getFileName().toString().substring(c.getFileName().toString().length() - 4).contains(".tsv"))
                    .parallel()
                    .forEach(x -> {
                        Matcher m = pattern.matcher(x.toString());
                        if (m.find()) {
                            String idFile = m.group(1);
                            String annotator = m.group(2);
                            texts.putIfAbsent(idFile, new HashMap<>());
                            texts.get(idFile).putIfAbsent(annotator, new ArrayList<>());

                            ArrayList<Integer> theseResults = new ArrayList<>();

                            try {
                                BufferedReader reader = new BufferedReader(new FileReader(x.toString()));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    texts.get(idFile).get(annotator).add(line.trim());
                                    String[] parts = line.split("\t");
                                    if (parts.length < 2) {
                                        continue;
                                    }
                                    theseResults.add(conversionMap.get(parts[1]));
                                }
                                reader.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            results.putIfAbsent(idFile, new HashMap<>());
                            results.get(idFile).putIfAbsent(annotator, theseResults);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<Integer> newList = new ArrayList<>(conversionMap.values());
        ArrayList<int[]> resList = new ArrayList<>();

        for (String key : results.keySet()) {
            ArrayList<FrequencyHashSet<Integer>> theseResults = new ArrayList<>();

            for (String annotator : results.get(key).keySet()) {
                ArrayList<Integer> get = results.get(key).get(annotator);
                for (int i = 0; i < get.size(); i++) {
                    if (theseResults.size() < i + 1) {
                        theseResults.add(new FrequencyHashSet<>());
                    }
                    Integer value = get.get(i);
                    theseResults.get(i).add(value);
                }
            }
            for (FrequencyHashSet<Integer> rowResult : theseResults) {
                int[] r = new int[newList.size()];
                for (Integer val : newList) {
                    r[val] = rowResult.getZero(val);
                }
                resList.add(r);
            }
        }

        int[][] mat = new int[resList.size()][newList.size()];
        for (int i = 0; i < resList.size(); i++) {
            int[] ints = resList.get(i);
            mat[i] = ints;
        }

        float kappa = FleissKappa.computeKappa(mat);

        File outputFolderFile = new File(outputFolder);
        if (!outputFolderFile.exists()) {
            outputFolderFile.mkdirs();
        }
        for (String idFile : texts.keySet()) {
            Path outputFile = Paths.get(outputFolder, idFile + ".tsv");
            Set<String> annotators = texts.get(idFile).keySet();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.toFile()));
                int size = 0;
                for (String annotator : annotators) {
                    size = texts.get(idFile).get(annotator).size();
                    break;
                }
                for (int i = 0; i < size; i++) {
                    FrequencyHashSet<String> thisSet = new FrequencyHashSet<>();
                    for (String annotator : annotators) {
                        thisSet.add(texts.get(idFile).get(annotator).get(i));
                    }
                    writer.append(thisSet.getSorted().first().getKey()).append("\n");
                }
                writer.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
