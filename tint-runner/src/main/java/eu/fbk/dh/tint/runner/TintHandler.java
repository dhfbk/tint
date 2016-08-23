package eu.fbk.dh.tint.runner;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: alessio
 * Date: 21/07/14
 * Time: 15:30
 * To change this template use File | Settings | File Templates.
 */

public class TintHandler extends HttpHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TintHandler.class);
    private TintPipeline pipeline;

    public static Map<TintRunner.OutputFormat, String> contentTypes = new HashMap<>();
    static {
        contentTypes.put(TintRunner.OutputFormat.CONLL, "text/plain");
        contentTypes.put(TintRunner.OutputFormat.XML, "text/xml");
        contentTypes.put(TintRunner.OutputFormat.NAF, "text/xml");
        contentTypes.put(TintRunner.OutputFormat.JSON, "text/json");
        contentTypes.put(TintRunner.OutputFormat.TEXTPRO, "text/plain");
        contentTypes.put(TintRunner.OutputFormat.READABLE, "text/plain");
    }

    public TintHandler(TintPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void writeOutput(Response response, String contentType, String output) throws IOException {
        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write(output);
    }

    @Override
    public void service(Request request, Response response) throws Exception {

        String host = request.getHeader("x-forwarded-for");

//		String referer = request.getHeader("referer");
//		String okReferer = pipeline.getDefaultConfig().getProperty("back_referer");

//		boolean backLink = false;
//		if (okReferer != null || referer != null) {
//			backLink = true;
//		}

        request.setCharacterEncoding("UTF-8");
        String text = request.getParameter("text");
        String outputFormat = request.getParameter("format");

        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        TintRunner.OutputFormat format = TintRunner.getOutputFormat(outputFormat, TintRunner.OutputFormat.CONLL);
        pipeline.run(inputStream, outputStream, format);

        // Log for stats
        LOGGER.info("[SENTENCE]");
        LOGGER.info("Host: {}", host);
        LOGGER.info("Text: {}", text);

        String output = outputStream.toString();

//		KAFDocument doc = text2naf(text, meta);

//		doc = pipeline.parseFromString(doc.toString());

//        String viewString;
//        viewString = doc.toString();
//        try {
//
//            HashMap<String, Object> demoProperties = new HashMap<>();
//            demoProperties.put("renderer.template.title", "PIKES demo");
//            if (backLink) {
//                demoProperties.put("renderer.template.backlink", "javascript:history.back();");
//            } else {
//                demoProperties
//                        .put("renderer.template.backlink", pipeline.getDefaultConfig().getProperty("back_alt_link"));
//                demoProperties
//                        .put("renderer.template.backlabel", pipeline.getDefaultConfig().getProperty("back_alt_text"));
//            }
//
//            boolean fusion = request.getParameter("rdf_fusion") != null;
//            boolean normalization = request.getParameter("rdf_compaction") != null;
//
//            demoProperties.put("generator.fusion", fusion);
//            demoProperties.put("generator.normalization", normalization);
//
//            NAFFilter filter = NAFFilter.builder().withProperties(pipeline.getDefaultConfig(), "filter").build();
//            RDFGenerator generator = RDFGenerator.builder().withProperties(demoProperties, "generator").build();
//            Renderer renderer = Renderer.builder().withProperties(demoProperties, "renderer").build();
//
//            filter.filter(doc);
//            final Model model = generator.generate(doc, null);
//            StringWriter writer = new StringWriter();
//            renderer.renderAll(writer, doc, model, null, null);
//            viewString = writer.toString();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            viewString = "Unable to show graph. <br /><br />\n<pre>" + doc.toString().replace("<", "&lt;")
//                    .replace(">", "&gt;") + "</pre>";
//        }

        writeOutput(response, contentTypes.get(format), output);
    }
}
