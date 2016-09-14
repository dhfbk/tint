import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

/**
 * Created by alessio on 02/09/16.
 */

public class DeGasperi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeGasperi.class);

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-degasperi")
                    .withHeader("Extract De Gasperi corpus for embeddings")
                    .withOption("i", "input", "Input file", "FILE",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withOption("s", "stopwords", "Output file for tokens", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

//            String folder = "/Volumes/LEXAR/Resources/ita/degasperi";
//            String stopWordsFile = "/Volumes/LEXAR/Resources/ita/it.stop";
//            String outputFile = "/Volumes/LEXAR/Resources/ita/degasperi.all.txt";

            File folder = cmd.getOptionValue("input", File.class);
            File stopWordsFile = cmd.getOptionValue("stopwords", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.load();

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            HashSet<String> stopWords = new HashSet<>();
            if (stopWordsFile != null) {
                List<String> lines = Files.readLines(stopWordsFile, Charsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    if (line.length() > 0) {
                        stopWords.add(line.toLowerCase());
                    }
                }

                stopWords.add("l'");
                stopWords.add("dall'");
                stopWords.add("dell'");
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            java.nio.file.Files.walk(folder.toPath()).forEach(filePath -> {
                try {
                    if (java.nio.file.Files.isRegularFile(filePath)) {
                        LOGGER.info("FILE: {}", filePath.getFileName());
                        InputStream stream = new FileInputStream(filePath.toFile());

                        Document doc = dBuilder.parse(stream);
                        doc.getDocumentElement().normalize();

                        XPathExpression expr;
                        NodeList nl;

                        expr = xpath.compile("/xml/file/content");
                        nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                        for (int i = 0; i < nl.getLength(); i++) {
                            Node item = nl.item(i);
                            Element element = (Element) item;
                            String content = element.getTextContent();
                            Annotation annotation = pipeline.runRaw(content);

                            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
                            for (CoreMap sentence : sentences) {
                                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                                StringBuilder sentenceString = new StringBuilder();
                                for (CoreLabel token : tokens) {
                                    String lemma = token.lemma();
                                    if (stopWords.contains(lemma.toLowerCase())) {
                                        continue;
                                    }
                                    if (lemma.equals("[PUNCT]")) {
                                        continue;
                                    }
                                    sentenceString.append(lemma).append(" ");
                                }
                                writer.append(sentenceString.toString().trim());
                                writer.append("\n");
                            }

//                            pipeline.run(inputStream, new WriterOutputStream(writer), TintRunner.OutputFormat.TEXTPRO);
//                            writer.append(content).append("\n");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
