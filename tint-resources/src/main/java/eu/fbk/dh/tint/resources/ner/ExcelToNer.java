package eu.fbk.dh.tint.resources.ner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelToNer {

    public static final Logger LOGGER = LogManager.getLogger(ExcelToNer.class);
    public static Pattern nerPattern = Pattern.compile("^([IB]-)?(PER|ORG|LOC)$");

    public static void main(String[] args) {
        String inputFolder = "/Users/alessio/Downloads/annotazioni/wiki-download";
        String outputFolder = "/Users/alessio/Downloads/annotazioni/wiki-download-ner";

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

        try {
            Properties properties = new Properties();
            properties.setProperty("annotators", "ita_toksent, pos, ita_upos, ita_splitter");
            properties.setProperty("ita_toksent.newlineIsSentenceBreak", "1");
            properties.setProperty("ita_toksent.tokenizeOnlyOnSpace", "1");
            properties.setProperty("ita_toksent.ssplitOnlyOnNewLine", "1");
            TintPipeline pipeline = new TintPipeline();
            pipeline.addProperties(properties);
            pipeline.load();

            final AtomicInteger tokenCount = new AtomicInteger(0);

            Files.walk(Paths.get(inputFolder))
                    .filter(Files::isRegularFile)
                    .filter(c -> c.getFileName().toString().substring(c.getFileName().toString().length() - 5).contains(".xlsx"))
                    .forEach(x -> {
                        try {

                            String fileName = x.getFileName().toString();
                            if (fileName.startsWith("~")) {
                                return;
                            }

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

                            for (int r = 0; r < datatypeSheet.getPhysicalNumberOfRows(); r++) {
                                Row myrow = datatypeSheet.getRow(r);

                                Boolean isEmpty = false;
                                Cell tokenCell = null;
                                String ner = null;

                                if (myrow == null || myrow.getLastCellNum() < 3) {
                                    isEmpty = true;
                                } else {
                                    if (myrow.getCell(0).toString().startsWith("#")) {
                                        continue;
                                    }

                                    tokenCell = myrow.getCell(1);

                                    if (tokenCell.toString().trim().equals("")) {
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
                                        StringBuilder buffer = new StringBuilder();
                                        for (String token : tokens) {
                                            buffer.append(token).append(" ");
                                        }
                                        String text = buffer.toString().trim();
                                        Annotation annotation = pipeline.runRaw(text);

                                        int i = -1;
                                        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
                                            if (token.isMWTFirst() || !token.isMWT()) {
                                                i++;
                                            }
                                            writer.append(token.word()).append("\t").append(ners.get(i)).append("\n");
                                        }

                                        writer.append("\n");

                                        tokenCount.addAndGet(tokens.size());

                                        tokens = new ArrayList<>();
                                        ners = new ArrayList<>();
                                    }
                                } else {

                                    String dataFormatString = tokenCell.getCellStyle().getDataFormatString();
                                    String token;
                                    if (tokenCell.getCellType() == CellType.NUMERIC) {
                                        if (dataFormatString.equals("General")) {
                                            token = Double.toString(tokenCell.getNumericCellValue());
                                        } else {
                                            DataFormatter df = new DataFormatter();
                                            dataFormatString = dataFormatString.replace('h', 'H');
                                            df.addFormat(dataFormatString, new SimpleDateFormat(dataFormatString));
                                            token = df.formatCellValue(tokenCell, evaluator);
                                        }
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

                            writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    });

//            LOGGER.info("Token count: {}", tokenCount);
            System.out.println(tokenCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
