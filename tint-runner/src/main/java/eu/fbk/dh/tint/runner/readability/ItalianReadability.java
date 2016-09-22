package eu.fbk.dh.tint.runner.readability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by alessio on 21/09/16.
 */

abstract class ItalianReadability extends Readability {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItalianReadability.class);

    public ItalianReadability() {
        super("it");
    }

    @Override Map<String, Object> json() {
        return null;
    }
}
