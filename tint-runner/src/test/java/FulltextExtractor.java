import eu.fbk.utils.core.CommandLine;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.TreeMap;

/**
 * Created by alessio on 15/06/17.
 */

public class FulltextExtractor {

    public static void main(String[] args) {
        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr;
            NodeList nl;

            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-fn-ft")
                    .withHeader("Extract fulltexts from FrameNet")
                    .withOption("i", "input", "Input folder", "FOLDER",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FOLDER",
                            CommandLine.Type.DIRECTORY, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inFolder = cmd.getOptionValue("input", File.class);
            File outFolder = cmd.getOptionValue("output", File.class);

            if (!outFolder.exists()) {
                outFolder.mkdirs();
            }

            for (File file : inFolder.listFiles()) {
                String name = file.getName();
                if (!name.endsWith(".xml")) {
                    continue;
                }

                System.out.println("Processing file " + name);

                File outputFile = new File(outFolder + File.separator + name + ".txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                Document doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();

                expr = xpath.compile("/fullTextAnnotation/sentence");
                nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                for (int i = 0; i < nl.getLength(); i++) {
                    Node item = nl.item(i);

                    NodeList internalNl;
                    String text = "";

                    expr = xpath.compile("text");
                    internalNl = (NodeList) expr.evaluate(item, XPathConstants.NODESET);
                    for (int j = 0; j < internalNl.getLength(); j++) {
                        Node internalItem = internalNl.item(j);
                        Element internalElement = (Element) internalItem;
                        text = internalElement.getTextContent();
                    }

                    if (text.length() == 0) {
                        System.out.println("ERROR!");
                        continue;
                    }

                    StringBuffer buffer = new StringBuffer();
                    int lastIndex = text.length();

                    expr = xpath.compile("annotationSet/layer[@name='Target']/label");
                    internalNl = (NodeList) expr.evaluate(item, XPathConstants.NODESET);
                    TreeMap<Integer, Integer> map = new TreeMap<>();
                    for (int j = internalNl.getLength() - 1; j >= 0; j--) {
                        Node internalItem = internalNl.item(j);
                        Element internalElement = (Element) internalItem;
                        Integer start = Integer.parseInt(internalElement.getAttribute("start"));
                        Integer end = Integer.parseInt(internalElement.getAttribute("end")) + 1;

                        map.put(start, end);
                    }

                    for (Integer start : map.descendingKeySet()) {
                        int end = map.get(start);

                        if (lastIndex < end) {
                            System.out.println("lastIndex is lesser than end");
                            continue;
                        }

                        buffer.insert(0, "}" + text.substring(end, lastIndex));
                        lastIndex = end;
                        buffer.insert(0, "{" + text.substring(start, lastIndex));
                        lastIndex = start;
                    }

                    buffer.insert(0, text.substring(0, lastIndex));

                    writer.append(buffer.toString()).append("\n");
                }

                writer.close();
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
