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

        request.setCharacterEncoding("UTF-8");
        String text = request.getParameter("text");
        String outputFormat = request.getParameter("format");

        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        TintRunner.OutputFormat format = TintRunner.getOutputFormat(outputFormat, TintRunner.OutputFormat.JSON);
        pipeline.run(inputStream, outputStream, format);

        LOGGER.debug("Text: {}", text);

        String output = outputStream.toString();

        writeOutput(response, contentTypes.get(format), output);
    }
}
