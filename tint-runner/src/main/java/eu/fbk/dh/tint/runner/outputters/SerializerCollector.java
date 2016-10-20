package eu.fbk.dh.tint.runner.outputters;

import com.google.common.reflect.ClassPath;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import eu.fbk.dh.tint.json.AnnotationExclusionStrategy;
import eu.fbk.dh.tint.json.JSONLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by alessio on 04/10/16.
 */

public class SerializerCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializerCollector.class);
    private GsonBuilder gsonBuilder;

    public GsonBuilder getGsonBuilder() {
        return gsonBuilder;
    }

    public SerializerCollector() {
        gsonBuilder = new GsonBuilder();

        LOGGER.info("Loading serializers");
        final ClassLoader loader = Thread.currentThread()
                .getContextClassLoader();
        try {

            ClassPath classpath = ClassPath.from(loader); // scans the class path used by classloader
            for (ClassPath.ClassInfo classInfo : classpath.getAllClasses()) {
                if (!classInfo.getSimpleName().endsWith("_")) {
                    try {
                        Class<?> myClass = classInfo.load();
                        if (myClass.isAnnotationPresent(JSONLabel.class)) {
                            JSONLabel JsonAnnotation = myClass.getAnnotation(JSONLabel.class);
                            Class<?>[] serializerClasses = JsonAnnotation.serializer();
                            for (Class<?> serializerClass : serializerClasses) {
                                if (JsonSerializer.class.isAssignableFrom(serializerClass)) {
                                    for (Type type : serializerClass.getGenericInterfaces()) {
                                        if (type instanceof ParameterizedType && ((ParameterizedType) type)
                                                .getRawType()
                                                .equals(JsonSerializer.class)) {
                                            try {
                                                Type thisType = ((ParameterizedType) type).getActualTypeArguments()[0];
                                                LOGGER.info("Loading serializer for {}", thisType);
                                                gsonBuilder
                                                        .registerTypeAdapter(thisType, serializerClass.newInstance());
                                            } catch (InstantiationException e) {
                                                e.printStackTrace();
                                            } catch (IllegalAccessException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                }
                            }

                        }
                    } catch (Throwable e) {
                        // ignored
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        gsonBuilder.setExclusionStrategies(new AnnotationExclusionStrategy());
    }

}
