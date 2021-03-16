package eu.fbk.dh.tint.runner;

import com.google.gson.GsonBuilder;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import eu.fbk.utils.corenlp.outputters.TextProOutputter;
import eu.fbk.utils.corenlp.outputters.TokenOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.time.Instant;
import java.util.Properties;

//import eu.fbk.utils.corenlp.outputters.SerializerCollector;

/**
 * Created by alessio on 15/08/16.
 */

public class TintPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(TintPipeline.class);
    private String documentDate = null;
    private Properties props = new Properties();

//    private boolean DEFAULT_LOAD_SERIALIZER = false;
//    SerializerCollector serializerCollector = null;

//    public void loadSerializers() {
//        serializerCollector = new SerializerCollector();
//    }

    public TintPipeline(Properties props) {
        this.props = props;
    }

    public TintPipeline() {
        this(true);
    }

    public TintPipeline(boolean loadDefaultProperties) {
        if (loadDefaultProperties) {
            try {
                loadDefaultProperties();
            } catch (IOException e) {
                LOGGER.error("Unable to load default configuration");
            }
        }
    }

    public void load() {
//        if (pipeline == null) {
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//        if (DEFAULT_LOAD_SERIALIZER) {
//            loadSerializers();
//        }
//        }
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

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public String getDocumentDate() {
        return documentDate;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public void setDocumentDate(String documentDate) {
        this.documentDate = documentDate;
    }

    public Annotation runRaw(String text) {
        return runRaw(text, null);
    }

    public Annotation runRaw(String text, @Nullable StanfordCoreNLP pipeline) {
        load();

        Annotation annotation = new Annotation(text);

        //todo: fix this using a property
        LOGGER.debug("Text: {}", text);
        if (documentDate == null) {
            documentDate = Instant.now().toString().substring(0, 10);
        }
        annotation.set(CoreAnnotations.DocDateAnnotation.class, documentDate);
        if (pipeline == null) {
            pipeline = new StanfordCoreNLP(props);
        }

        pipeline.annotate(annotation);
        annotation.set(TimingAnnotations.TimingAnnotation.class, pipeline.timingInformation());

        return annotation;
    }

    public Annotation run(String text, OutputStream outputStream, TintRunner.OutputFormat format)
            throws IOException {
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = runRaw(text, pipeline);

        switch (format) {
            case CONLLU:
                CoNLLUOutputter.conllUPrint(annotation, outputStream, pipeline);
                break;
            case CONLL:
                CoNLLOutputter.conllPrint(annotation, outputStream, pipeline);
                break;
            case READABLE:
                TextOutputter.prettyPrint(annotation, outputStream, pipeline);
                break;
            case XML:
                XMLOutputter.xmlPrint(annotation, outputStream, pipeline);
                break;
            case JSON:
                GsonBuilder gsonBuilder;
                gsonBuilder = new GsonBuilder();
//            if (serializerCollector != null) {
//                gsonBuilder = serializerCollector.getGsonBuilder();
//            } else {
//                gsonBuilder = new GsonBuilder();
//            }
                JSONOutputter.jsonPrint(gsonBuilder, annotation, outputStream, pipeline);
                break;
            case TEXTPRO:
                TextProOutputter.tpPrint(annotation, outputStream, pipeline);
                break;
            case TOKEN:
                TokenOutputter.tpPrint(annotation, outputStream, pipeline);
                break;
//        case NAF:
//            KAFDocument doc = AbstractHandler.text2naf(text, new HashMap<>());
//            AnnotationPipeline pikesPipeline = new AnnotationPipeline(null, null);
//            pikesPipeline.addToNerMap("PER", "PERSON");
//            pikesPipeline.addToNerMap("ORG", "ORGANIZATION");
//            pikesPipeline.addToNerMap("LOC", "LOCATION");
//            pikesPipeline.annotateStanford(new Properties(), annotation, doc);
//            outputStream.write(doc.toString().getBytes());
//            outputStream.flush();
        }

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

        return run(text, outputStream, format);

    }
}
