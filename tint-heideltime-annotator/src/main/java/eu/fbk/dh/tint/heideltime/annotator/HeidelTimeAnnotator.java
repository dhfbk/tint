package eu.fbk.dh.tint.heideltime.annotator;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import eu.fbk.utils.core.PropertiesUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by alessio on 10/08/16.
 */

public class HeidelTimeAnnotator implements Annotator {

    HeidelTimeStandalone tagger;
    static DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    class TimexObject {

        private int start;
        private int end;
        private String timexType;
        private String timexValue;

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getTimexType() {
            return timexType;
        }

        public void setTimexType(String timexType) {
            this.timexType = timexType;
        }

        public String getTimexValue() {
            return timexValue;
        }

        public void setTimexValue(String timexValue) {
            this.timexValue = timexValue;
        }

        public TimexObject(int start, int end, String timexType, String timexValue) {
            this.start = start;
            this.end = end;
            this.timexType = timexType;
            this.timexValue = timexValue;
        }
    }

    public HeidelTimeAnnotator(String annotatorName, Properties props) {

        // Todo: load an instance for each type
        // Todo: add document creation datetime

        String configFile = props.getProperty(annotatorName + ".config", null);
        String dtString = props.getProperty(annotatorName + ".type", "news");

        DocumentType documentType;
        try {
            documentType = DocumentType.valueOf(dtString.toUpperCase());
        } catch (Exception e) {
            documentType = DocumentType.NEWS;
        }

        if (configFile == null) {
            Properties convertedProperties = PropertiesUtils.dotConvertedProperties(props, annotatorName);
            tagger = HeidelTimeModel.getInstance(convertedProperties, documentType).getTagger();
        } else {
            tagger = HeidelTimeModel.getInstance(configFile, documentType).getTagger();
        }
    }

    /**
     * Given an Annotation, perform a task on this Annotation.
     *
     * @param annotation
     */
    @Override public void annotate(Annotation annotation) {
        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        if (text != null) {

            try {
                Date documentDate = new Date();

                try {
                    String creationDate = annotation.get(CoreAnnotations.DocDateAnnotation.class);
                    documentDate = format.parse(creationDate);
                } catch (Exception e) {
                    // ignored
                }
                String process = tagger.process(text, documentDate);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                InputStream textStream = new ByteArrayInputStream(process.getBytes());

                Document doc = dBuilder.parse(textStream);
                doc.getDocumentElement().normalize();

                Map<Integer, TimexObject> timexes = new HashMap<>();
                NodeList entries = doc.getElementsByTagName("*");

                for (int i = 1; i < entries.getLength(); i++) {
                    Element element = (Element) entries.item(i);
                    if (element.getNodeName().equals("heideltime:Timex3")) {
                        int begin = Integer.parseInt(element.getAttribute("begin"));
                        int end = Integer.parseInt(element.getAttribute("end"));

                        String timexType = element.getAttribute("timexType");
                        String timexValue = element.getAttribute("timexValue");

                        TimexObject timexObject = new TimexObject(begin, end, timexType, timexValue);
                        timexes.put(begin, timexObject);
                    }
                }

                List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
                TimexObject timexObject = null;

                for (CoreLabel token : tokens) {
                    int begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                    if (timexObject != null && end > timexObject.getEnd()) {
                        timexObject = null;
                    }
                    if (timexes.containsKey(begin)) {
                        timexObject = timexes.get(begin);
                    }

                    if (timexObject != null) {
                        token.set(CoreAnnotations.NamedEntityTagAnnotation.class, timexObject.getTimexType());
                        token.set(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class,
                                timexObject.getTimexValue());
                        token.set(CoreAnnotations.ValueAnnotation.class,
                                text.substring(timexObject.getStart(), timexObject.getEnd()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Returns a set of requirements for which tasks this annotator can
     * provide.  For example, the POS annotator will return "pos".
     */
    @Override public Set<Requirement> requirementsSatisfied() {
        return Collections.emptySet();
    }

    /**
     * Returns the set of tasks which this annotator requires in order
     * to perform.  For example, the POS annotator will return
     * "tokenize", "ssplit".
     */
    @Override public Set<Requirement> requires() {
        return Collections.singleton(Annotator.TOKENIZE_REQUIREMENT);
    }
}
