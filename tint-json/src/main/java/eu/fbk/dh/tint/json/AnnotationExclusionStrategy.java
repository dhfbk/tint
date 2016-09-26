package eu.fbk.dh.tint.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Created by alessio on 25/09/16.
 */

public class AnnotationExclusionStrategy implements ExclusionStrategy {

    @Override public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        return fieldAttributes.getAnnotation(JSONExclude.class) != null;
    }

    @Override public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
