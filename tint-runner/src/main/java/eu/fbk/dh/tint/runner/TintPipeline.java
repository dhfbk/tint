package eu.fbk.dh.tint.runner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import eu.fbk.dkm.pikes.tintop.AnnotationPipeline;
import eu.fbk.dkm.pikes.tintop.server.AbstractHandler;
import ixa.kaflib.KAFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by alessio on 15/08/16.
 */

public class TintPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(TintPipeline.class);
    StanfordCoreNLP pipeline = null;
    String documentDate = null;
    Properties props = new Properties();

    public void load() {
        if (pipeline == null) {
            pipeline = new StanfordCoreNLP(props);
        }
    }

    public void loadDefaultProperties() throws IOException {
        InputStream configStream = TintRunner.class.getResourceAsStream("/default-config.properties");
        if (configStream != null) {
            props.load(configStream);
        }
    }

    public void loadPropertiesFromStream(InputStream stream) throws IOException {
        props.load(stream);
    }

    public void loadPropertiesFromFile(File propsFile) throws IOException {
        if (propsFile != null) {
            InputStream configStream = new FileInputStream(propsFile);
            loadPropertiesFromStream(configStream);
        }
    }

    public void addProperties(Properties properties) {
        if (properties != null) {
            props.putAll(properties);
        }
    }

    public String getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(String documentDate) {
        this.documentDate = documentDate;
    }

    public Annotation runRaw(String text) {
        load();

        Annotation annotation = new Annotation(text);
        LOGGER.debug("Text: {}", text);
        if (documentDate != null) {
            annotation.set(CoreAnnotations.DocDateAnnotation.class, documentDate);
        }
        pipeline.annotate(annotation);

        return annotation;
    }

    public Annotation run(InputStream inputStream, OutputStream outputStream, TintRunner.OutputFormat format)
            throws IOException {

        Reader reader = new InputStreamReader(inputStream);
        StringBuilder inputText = new StringBuilder();
        int i;
        while ((i = reader.read()) != -1) {
            inputText.append((char) i);
        }
        reader.close();
        String text = inputText.toString();

        Annotation annotation = runRaw(text);

        switch (format) {
        case CONLL:
            CoNLLUOutputter.conllUPrint(annotation, outputStream, pipeline);
            break;
        case READABLE:
            TextOutputter.prettyPrint(annotation, outputStream, pipeline);
            break;
        case XML:
            XMLOutputter.xmlPrint(annotation, outputStream, pipeline);
            break;
        case JSON:
            JSONOutputter.jsonPrint(annotation, outputStream, pipeline);
            break;
        case TEXTPRO:
            TextProOutputter.tpPrint(annotation, outputStream, pipeline);
            break;
        case NAF:
            KAFDocument doc = AbstractHandler.text2naf(text, new HashMap<>());
            AnnotationPipeline pikesPipeline = new AnnotationPipeline(null, null);
            pikesPipeline.addToNerMap("PER", "PERSON");
            pikesPipeline.addToNerMap("ORG", "ORGANIZATION");
            pikesPipeline.addToNerMap("LOC", "LOCATION");
            pikesPipeline.annotateStanford(new Properties(), annotation, doc);
            outputStream.write(doc.toString().getBytes());
            outputStream.flush();
        }

        return annotation;
    }
}
