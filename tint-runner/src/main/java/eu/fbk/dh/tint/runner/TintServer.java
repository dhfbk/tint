package eu.fbk.dh.tint.runner;

import eu.fbk.utils.core.CommandLine;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: alessio
 * Date: 21/07/14
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */

public class TintServer {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TintServer.class);

    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final Integer DEFAULT_PORT = 8012;

    public TintServer(String host, Integer port) {
        this(host, port, null, null);
    }

    public TintServer(String host, Integer port, @Nullable File configFile) {
        this(host, port, configFile, null);
    }

    public TintServer(String host, Integer port, @Nullable File configFile,
            @Nullable Properties additionalProperties) {
        LOGGER.info("starting " + host + "\t" + port + " (" + new Date() + ")...");

        int timeoutInSeconds = -1;

        try {
            // Load the pipeline
            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.loadPropertiesFromFile(configFile);
            pipeline.addProperties(additionalProperties);
            pipeline.load();

            LOGGER.info("Pipeline loaded");

            final HttpServer httpServer = new HttpServer();
            NetworkListener nl = new NetworkListener("tint-server", host, port);
            httpServer.addListener(nl);

            TintHandler tintHandler = new TintHandler(pipeline);
            tintHandler.setRequestURIEncoding(Charset.forName("UTF-8"));

            httpServer.getServerConfiguration().setSessionTimeoutSeconds(timeoutInSeconds);
            httpServer.getServerConfiguration().setMaxPostSize(4194304);
            httpServer.getServerConfiguration().addHttpHandler(tintHandler, "/tint");

            httpServer.start();
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("error running " + host + ":" + port);
        }
    }

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./tintop-server")
                    .withHeader("Run the Tintop Server")
                    .withOption("c", "config", "Configuration file", "FILE", CommandLine.Type.FILE_EXISTING, true,
                            false, false)
                    .withOption("p", "port", String.format("Host port (default %d)", DEFAULT_PORT), "NUM",
                            CommandLine.Type.INTEGER, true, false, false)
                    .withOption("h", "host", String.format("Host address (default %s)", DEFAULT_HOST), "NUM",
                            CommandLine.Type.STRING, true, false, false)
                    .withOption(null, "properties", "Additional properties", "PROPS", CommandLine.Type.STRING, true,
                            true, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            String host = cmd.getOptionValue("host", String.class, DEFAULT_HOST);
            Integer port = cmd.getOptionValue("port", Integer.class, DEFAULT_PORT);
            File configFile = cmd.getOptionValue("config", File.class);

            List<String> addProperties = cmd.getOptionValues("properties", String.class);

            Properties additionalProps = new Properties();
            for (String property : addProperties) {
                try {
                    additionalProps.load(new StringReader(property));
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
            }

            TintServer server = new TintServer(host, port, configFile, additionalProps);

        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
