package eu.fbk.dh.tint.runner;

import com.google.common.base.Throwables;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;

/**
 * Created by alessio on 03/08/16.
 */

public class TintRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TintRunner.class);

    public static enum OutputFormat {
        READABLE, JSON, XML, CONLL, NAF, TEXTPRO
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("tint")
                    .withHeader("Run the Tint pipeline.")
                    .withOption("c", "config-file", "Configuration file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("i", "input-file", "Input text file (default stdin)", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("o", "output-file", "Output processed file (default stdout)", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("f", "output-format",
                            "Output format: textpro, json, xml, conll, readable (default conll)",
                            "FORMAT",
                            CommandLine.Type.STRING, true, false, false)
                    .withOption(null, "properties", "Additional properties for Stanford CoreNLP", "PROPS",
                            CommandLine.Type.STRING, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("i", File.class);
            final File outputPath = cmd.getOptionValue("o", File.class);
            final File configPath = cmd.getOptionValue("c", File.class);

            List<String> addProperties = cmd.getOptionValues("properties", String.class);
            Properties additionalProps = new Properties();
            for (String property : addProperties) {
                try {
                    additionalProps.load(new StringReader(property));
                } catch (Exception e) {
                    // continue
                }
            }

            Console console = System.console();
            if (console == null) {
                final String loggerClassName = LOGGER.getClass().getName();
                if (loggerClassName.equals("ch.qos.logback.classic.Logger")) {
                    final Class<?> levelClass = Class.forName("ch.qos.logback.classic.Level");
                    final Object level = call(levelClass, "valueOf", "OFF");
                    call(LOGGER, "setLevel", level);
                } else if (loggerClassName.equals("org.apache.log4j.Logger")) {
                    final Class<?> levelClass = Class.forName("org.apache.log4j.Level");
                    final Object level = call(levelClass, "valueOf", "OFF");
                    call(LOGGER, "setLevel", level);
                } else if (loggerClassName.equals("org.apache.logging.slf4j.Log4jLogger")) {

                    // todo: check
                    final Class<?> managerClass = Class
                            .forName("org.apache.logging.log4j.LogManager");
                    final Object ctx = call(managerClass, "getContext", false);
                    final Object config = call(ctx, "getConfiguration");
                    final Object logConfig = call(config, "getLoggerConfig",
                            LOGGER.getName());
                    final Class<?> levelClass = Class
                            .forName("org.apache.logging.log4j.Level");
                    final Object level = call(levelClass, "valueOf", "OFF");
                    call(logConfig, "setLevel", level);
                    call(ctx, "updateLoggers");
                }

//            if (outputPath == null) {
                // todo: disable logging at all
//                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.OFF);
//                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("eu.fbk")).setLevel(Level.OFF);
            }

            final String formatString = cmd.getOptionValue("f", String.class);
            OutputFormat format = getOutputFormat(formatString, OutputFormat.JSON);

            // Input

            InputStream inputStream;
//            Reader reader;

            if (inputPath != null) {
                inputStream = new FileInputStream(inputPath);
            } else {
                inputStream = System.in;
            }

            // Text annotation

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.loadPropertiesFromFile(configPath);
            pipeline.addProperties(additionalProps);
            pipeline.load();
            System.err.println("Tint is ready");

            // Output

            OutputStream outputStream = System.out;
            if (outputPath != null) {
                outputStream = new FileOutputStream(outputPath);
            }

            pipeline.run(inputStream, outputStream, format);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    public static OutputFormat getOutputFormat(String formatString, OutputFormat outputFormat) {
        OutputFormat format = outputFormat;
        try {
            format = OutputFormat.valueOf(formatString.toUpperCase());
        } catch (Exception e) {
            LOGGER.error("Unrecognized format {}, using default ({})", formatString, outputFormat.toString());
            // continue
        }
        return format;
    }

    private static Object call(final Object object, final String methodName,
            final Object... args) {
        final boolean isStatic = object instanceof Class<?>;
        final Class<?> clazz = isStatic ? (Class<?>) object : object.getClass();
        for (final Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)
                    && isStatic == Modifier.isStatic(method.getModifiers())
                    && method.getParameterTypes().length == args.length) {
                try {
                    return method.invoke(isStatic ? null : object, args);
                } catch (final InvocationTargetException ex) {
                    Throwables.propagate(ex.getCause());
                } catch (final IllegalAccessException ex) {
                    throw new IllegalArgumentException("Cannot invoke " + method, ex);
                }
            }
        }
        throw new IllegalArgumentException("Cannot invoke " + methodName);
    }

}
