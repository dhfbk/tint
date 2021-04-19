package eu.fbk.dh.tint.resources.ner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class ExcelToNer {

    public static final Logger LOGGER = LogManager.getLogger(ExcelToNer.class);
    public static Pattern nerPattern = Pattern.compile("^([IB]-)?(PER|ORG|LOC)$");
    public static Pattern annotatorPattern = Pattern.compile("^[0-9]+_(.*)$");

    public static void main(String[] args) {
        String inputFolder = "/Users/alessio/Downloads/annotazioni/annotazioni/wiki";
        String outputFolder = "/Users/alessio/Downloads/annotazioni/annotazioni/wiki-out";
        boolean includeSubFolder = true;
        String nerListFile = "/Users/alessio/Downloads/annotazioni/annotazioni/all_ners.txt";
        String removeListFile = "/Users/alessio/Downloads/annotazioni/annotazioni/remove.txt";

        Properties props = new Properties();
        props.put("log4j.rootLogger", "INFO, stdlog");
        props.put("log4j.appender.stdlog", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.stdlog.target", "System.out");
        props.put("log4j.appender.stdlog.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.stdlog.layout.ConversionPattern", "%d{HH:mm:ss} %-5p %-25c :: %m%n");

        props.put("log4j.logger.edu.stanford.nlp.pipeline.StanfordCoreNLP", "ERROR");
        props.put("log4j.logger.eu.fbk.dh.tint.tokenizer.ItalianTokenizer", "ERROR");
        props.put("log4j.logger.edu.stanford.nlp.tagger.maxent.MaxentTagger", "ERROR");

        PropertyConfigurator.configure(props);

        File outputFolderFile = new File(outputFolder);
        if (!outputFolderFile.exists()) {
            outputFolderFile.mkdirs();
        }

        File removeFile = new File(removeListFile);
        Map<String[], String[]> removeMap = new HashMap<>();
        if (removeFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(removeFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split("\\s");
                    String[] results = new String[parts.length];
                    Arrays.fill(results, "O");
                    removeMap.put(parts, results);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Properties properties = new Properties();
            properties.setProperty("annotators", "ita_toksent, pos, ita_upos, ita_splitter");
//            properties.setProperty("annotators", "ita_toksent, pos, ita_upos");
            properties.setProperty("ita_toksent.newlineIsSentenceBreak", "1");
            properties.setProperty("ita_toksent.tokenizeOnlyOnSpace", "1");
            properties.setProperty("ita_toksent.ssplitOnlyOnNewLine", "1");
            TintPipeline pipeline = new TintPipeline();
            pipeline.addProperties(properties);
            pipeline.load();

            final AtomicInteger tokenCount = new AtomicInteger(0);

            HashMap<String, HashSet<String>> allNers = new HashMap<>();
            Files.walk(Paths.get(inputFolder))
                    .filter(Objects::nonNull)
                    .filter(Files::isRegularFile)
                    .filter(c -> c.getFileName().toString().substring(c.getFileName().toString().length() - 5).contains(".xlsx"))
                    .forEach(x -> {
                        try {

                            String fileName = x.getFileName().toString();

                            if (fileName.startsWith("~")) {
                                return;
                            }

                            String annotator = null;
                            if (includeSubFolder) {
                                String folder = x.getParent().getFileName().toString();
                                fileName = folder + "-" + fileName;
                                Matcher matcher = annotatorPattern.matcher(folder);
                                if (matcher.find()) {
                                    annotator = matcher.group(1);
                                }
                            } else {
                                fileName = "wiki-" + fileName;
                            }

                            if (annotator == null) {
                                System.err.println("No annotator for " + fileName);
                                return;
                            }

//                            System.out.println(x.toFile().getAbsolutePath());

                            fileName = fileName.substring(0, fileName.length() - 5);
                            fileName += ".tsv";

                            Path outputFile = Paths.get(outputFolder, fileName);

                            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile.toFile()));

                            FileInputStream excelFile = new FileInputStream(x.toFile());
                            Workbook workbook = new XSSFWorkbook(excelFile);
                            Sheet datatypeSheet = workbook.getSheetAt(0);

                            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

                            List<String> tokens = new ArrayList<>();
                            List<String> ners = new ArrayList<>();

                            for (int r = 0; r <= datatypeSheet.getLastRowNum(); r++) {
                                Row myrow = datatypeSheet.getRow(r);

                                boolean isEmpty = false;
                                Cell tokenCell = null;
                                String ner = null;

                                if (myrow == null || myrow.getLastCellNum() < 3) {
                                    isEmpty = true;
                                } else {
                                    if (myrow.getCell(0) == null || myrow.getCell(0).toString().startsWith("#")) {
                                        continue;
                                    }

                                    tokenCell = myrow.getCell(1);

                                    if (tokenCell == null || tokenCell.toString().trim().equals("")) {
                                        isEmpty = true;
                                    }

                                    try {
                                        ner = myrow.getCell(2).getStringCellValue();
                                        ner = ner.trim();
                                        if (ner.equals("")) {
                                            isEmpty = true;
                                        }
                                    } catch (NullPointerException e) {
                                        LOGGER.error("{} in {} - Row: {}", e.getMessage(), fileName, myrow.toString());
                                    }
                                }

                                if (isEmpty) {
                                    if (tokens.size() > 0) {
                                        manageTokens(tokens, ners, pipeline, tokenCount, writer, allNers, removeMap);
                                        tokens = new ArrayList<>();
                                        ners = new ArrayList<>();
                                    }
                                } else {

                                    String dataFormatString = tokenCell.getCellStyle().getDataFormatString();
                                    String token;
                                    if (tokenCell.getCellType() == CellType.NUMERIC) {
                                        if (dataFormatString.equals("General")) {
                                            double number = tokenCell.getNumericCellValue();
                                            token = Double.toString(number);
                                            if (number == Math.ceil(number)) {
                                                token = Long.toString(Math.round(number));
                                            }
                                        } else {
                                            DataFormatter df = new DataFormatter();
                                            dataFormatString = dataFormatString.replace('h', 'H');
                                            df.addFormat(dataFormatString, new SimpleDateFormat(dataFormatString));
                                            token = df.formatCellValue(tokenCell, evaluator);
                                        }
                                    } else if (tokenCell.getCellType() == CellType.BOOLEAN) {
                                        token = Boolean.toString(tokenCell.getBooleanCellValue());
                                    } else {
                                        token = tokenCell.getStringCellValue();
                                    }
                                    if (!ner.equals("O")) {
                                        Matcher matcher = nerPattern.matcher(ner);
                                        if (matcher.matches()) {
                                            ner = matcher.group(2);
                                        } else {
                                            LOGGER.error("Invalid NER groups in {} - Row: {}", fileName, myrow.toString());
                                        }
                                    }

                                    token = token.replaceAll("\\s+", "");

                                    tokens.add(token);
                                    ners.add(ner);
                                }
                            }

                            if (tokens.size() > 0) {
                                manageTokens(tokens, ners, pipeline, tokenCount, writer, allNers, removeMap);
                            }

                            writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    });

//            LOGGER.info("Token count: {}", tokenCount);
            System.out.println(tokenCount);
//            System.out.println(allNers);

            BufferedWriter listWriter = new BufferedWriter(new FileWriter(nerListFile));
            for (String key : allNers.keySet()) {
                for (String ner : allNers.get(key)) {
                    listWriter.append(key).append("\t").append(ner).append("\n");
                }
            }
            listWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void manageTokens(List<String> tokens, List<String> ners, TintPipeline pipeline, AtomicInteger tokenCount, BufferedWriter writer, HashMap<String, HashSet<String>> allNers, Map<String[], String[]> removeMap) throws IOException {
        StringBuilder buffer = new StringBuilder();

        // Clean from jpg labels
        List<String> aTokens = new ArrayList<>(tokens);
        OptionalInt firstJpg = IntStream.range(0, tokens.size()).filter(i -> aTokens.get(i).equalsIgnoreCase("jpg")).findFirst();
        OptionalInt lastPipe = IntStream.range(0, tokens.size()).filter(i -> aTokens.get(i).equals("|")).reduce((first, second) -> second);
        if (lastPipe.isPresent()) {
            int start = firstJpg.orElse(0);
            int end = lastPipe.getAsInt();
            tokens.subList(start, end + 1).clear();
            ners.subList(start, end + 1).clear();
        }

        String previousNer = "O";
        StringBuilder thisString = new StringBuilder();
        for (int i = 0, nersSize = ners.size(); i < nersSize; i++) {
            String ner = ners.get(i);
            if (!ner.equals(previousNer)) {
                if (thisString.length() > 0) {
                    if (!previousNer.equals("O")) {
                        allNers.putIfAbsent(previousNer, new HashSet<>());
                        allNers.get(previousNer).add(thisString.toString().trim());
                    }
                    thisString = new StringBuilder();
                }
            }
            thisString.append(tokens.get(i)).append(" ");
            previousNer = ner;
        }
        if (thisString.length() > 0) {
            if (!previousNer.equals("O")) {
                allNers.putIfAbsent(previousNer, new HashSet<>());
                allNers.get(previousNer).add(thisString.toString().trim());
            }
        }

        ners = updateNers(tokens, ners, removeMap, true);

        for (String token : tokens) {
            buffer.append(token).append(" ");
        }
        String text = buffer.toString().trim();
        Annotation annotation = pipeline.runRaw(text);

        int i = -1;
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            if (token.isMWTFirst() == null || token.isMWTFirst() || !token.isMWT()) {
                i++;
            }
            writer.append(token.word()).append("\t").append(ners.get(i)).append("\n");
        }

        writer.append("\n");

        tokenCount.addAndGet(tokens.size());
    }

    private static List<String> updateNers(List<String> tokens, List<String> ners, Map<String[], String[]> myMap, boolean caseSensitive) {
        for (int i = 0, tokensSize = tokens.size(); i < tokensSize; i++) {
            String token = tokens.get(i);
            for (String[] replaceTokens : myMap.keySet()) {
                int length = replaceTokens.length;
                if (token.equals(replaceTokens[0]) && tokens.size() > i + length) {
                    String[] sequence = new String[length];
                    for (int j = 0; j < length; j++) {
                        sequence[j] = tokens.get(i + j);
                    }
                    if (!equals(sequence, replaceTokens, caseSensitive)) {
                        continue;
                    }
                    Set<String> theseNers = new HashSet<>(ners.subList(i, i + length));
                    if (theseNers.size() != 1) {
                        continue;
                    }
                    if (i != 0 && ners.get(i - 1).equals(ners.get(i))) {
                        continue;
                    }
                    if (ners.size() > i + length && ners.get(i + length).equals(ners.get(i))) {
                        continue;
                    }
//                    System.out.println(tokens);
//                    System.out.println(Arrays.toString(replaceTokens));
                    for (int j = 0; j < length; j++) {
                        ners.set(i + j, myMap.get(replaceTokens)[j]);
                    }
                }
            }
        }
        return ners;
    }

    public static boolean equals(String[] array1, String[] array2, boolean caseSensitive) {
        if (array1 == array2) {
            return true;
        }
        if (array1.length != array2.length) {
            return false;
        }

        for (int i = 0; i < array1.length; i++) {
            if (caseSensitive) {
                if (!array1[i].equals(array2[i])) {
                    return false;
                }
            }
            else {
                if (!array1[i].equalsIgnoreCase(array2[i])) {
                    return false;
                }
            }
        }

        return true;
    }

}
