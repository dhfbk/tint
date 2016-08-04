package eu.fbk.dh.tint.tokenizer.models;

import eu.fbk.dh.tint.tokenizer.token.ItalianTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by alessio on 25/05/15.
 */

public class ItalianTokenizerModel {

    private static ItalianTokenizerModel instance;
    private ItalianTokenizer tokenizer;
    private static final Logger LOGGER = LoggerFactory.getLogger(ItalianTokenizerModel.class);

    private ItalianTokenizerModel(File configuration) {
        LOGGER.trace("Loading model for Italian tokenizer");
        tokenizer = new ItalianTokenizer(configuration);
    }

    public static ItalianTokenizerModel getInstance(File posModel) {
        if (instance == null) {
            instance = new ItalianTokenizerModel(posModel);
        }

        return instance;
    }

    public ItalianTokenizer getTokenizer() {
        return tokenizer;
    }
}
