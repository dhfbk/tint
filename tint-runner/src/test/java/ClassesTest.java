import com.google.common.reflect.ClassPath;
import com.google.gson.JsonSerializer;
import eu.fbk.dh.tint.json.JSONLabel;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Created by alessio on 04/10/16.
 */

public class ClassesTest {

    public static void main(String[] args) {
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
                                        if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType()
                                                .equals(JsonSerializer.class)) {
                                            System.out.println(((ParameterizedType) type).getActualTypeArguments()[0]);
                                        }

                                    }
                                }
                            }

                        }
                    } catch (Throwable e) {
//                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
