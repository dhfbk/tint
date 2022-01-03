package eu.fbk.dh.tint.resources.ner;

import eu.fbk.utils.core.FrequencyHashSet;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitTrainTest {

    static Pattern fictionPattern = Pattern.compile("(.*)-[0-9]{3}[.-]");
    static Set<String> removeFromFilename = new HashSet<>();
    static Set<String> fictionTest = new HashSet<>();

    static {
        removeFromFilename.add("_TERESA");
        removeFromFilename.add("narrativa-teresa-");
        fictionTest.add("fabiani");
        fictionTest.add("pavese");
    }

    public static void main(String[] args) {
        String inputFolder = "/Users/alessio/Downloads/annotazioni/out-20211221/final-nosplit";
        double testRatio = 0.2;

        Random rand = new Random();
        File inputFolderFile = new File(inputFolder);

        File outputFolder = new File(inputFolder + File.separator + "_out");
        outputFolder.mkdirs();

        FrequencyHashSet<String> fictionTotals = new FrequencyHashSet<>();
        final AtomicInteger fictionTotal = new AtomicInteger();

        for (File file : inputFolderFile.listFiles((dir, name) -> !name.startsWith("_") && !name.startsWith("."))) {
            System.out.println(file.getAbsolutePath());

            List<String> testList = new ArrayList<>();
            List<String> trainList = new ArrayList<>();

            File outputTestFile = new File(inputFolder + File.separator + "_out" + File.separator + file.getName() + "_test.tsv");
            File outputTrainFile = new File(inputFolder + File.separator + "_out" + File.separator + file.getName() + "_train.tsv");

            File outputListTestFile = new File(inputFolder + File.separator + "_out" + File.separator + file.getName() + "_testlist.txt");
            File outputListTrainFile = new File(inputFolder + File.separator + "_out" + File.separator + file.getName() + "_trainlist.txt");

            try {
                BufferedWriter testWriter = new BufferedWriter(new FileWriter(outputTestFile));
                BufferedWriter trainWriter = new BufferedWriter(new FileWriter(outputTrainFile));

                if (outputListTestFile.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(outputListTestFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        testList.add(line);
                    }
                    reader.close();
                }
                Files.walk(file.toPath())
                        .filter(Objects::nonNull)
                        .filter(Files::isRegularFile)
                        .filter(c -> c.getFileName().toString().substring(c.getFileName().toString().length() - 4).contains(".tsv"))
                        .forEach(x -> {
                            String fileName = x.toFile().getName();

                            try {
                                if (file.getName().equals("fiction")) {
                                    String fileNameForAuthor = fileName;
                                    for (String r : removeFromFilename) {
                                        fileNameForAuthor = fileNameForAuthor.replace(r, "");
                                    }

                                    Matcher matcher = fictionPattern.matcher(fileNameForAuthor);
                                    if (matcher.find()) {
                                        String author = matcher.group(1);

                                        if (fictionTest.contains(author)) {
                                            testList.add(fileName);
                                            add(testWriter, x);
                                        } else {
                                            trainList.add(fileName);
                                            add(trainWriter, x);
                                        }
                                        BufferedReader reader = new BufferedReader(new FileReader(x.toFile()));
                                        String line;
                                        while ((line = reader.readLine()) != null) {
                                            if (line.trim().length() > 0) {
                                                fictionTotals.add(author);
                                                fictionTotal.incrementAndGet();
                                            }
                                        }
                                        reader.close();
                                    } else {
                                        System.out.println("Error: " + fileName);
                                    }
                                } else {
                                    if (outputListTestFile.exists()) {
                                        if (!testList.contains(fileName)) {
                                            trainList.add(fileName);
                                            add(trainWriter, x);
                                        }
                                        else {
                                            add(testWriter, x);
                                        }
                                    } else {
                                        if (rand.nextDouble() < testRatio) {
                                            testList.add(fileName);
                                            add(testWriter, x);
                                        } else {
                                            trainList.add(fileName);
                                            add(trainWriter, x);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                if (file.getName().equals("fiction")) {
                    // Statistics
                    System.out.println(fictionTotals);
                    System.out.println(fictionTotal);
                }

                BufferedWriter writer;
                writer = new BufferedWriter(new FileWriter(outputListTestFile));
                for (String testFile : testList) {
                    writer.append(testFile).append("\n");
                }
                writer.close();
                writer = new BufferedWriter(new FileWriter(outputListTrainFile));
                for (String trainFile : trainList) {
                    writer.append(trainFile).append("\n");
                }
                writer.close();

                trainWriter.close();
                testWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void add(BufferedWriter writer, Path x) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(x.toFile()));
        String line;
        while ((line = reader.readLine()) != null) {
            writer.append(line.trim()).append("\n");
        }
        reader.close();
    }
}
