package eu.fbk.dh.tint.runner.wrappers;

import de.unihd.dbs.heideltime.standalone.Config;
import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by alessio on 25/05/15.
 */

public class HeidelTimeModel {

    private static HeidelTimeModel instance;
    private HeidelTimeStandalone tagger;
    private static final Logger LOGGER = LoggerFactory.getLogger(HeidelTimeModel.class);

    // /Users/alessio/Documents/scripts/simpatico-dashboard/config.props

    private HeidelTimeModel(String configFile, DocumentType documentType) {
        LOGGER.trace("Loading tagger for HeidelTime");
        tagger = new HeidelTimeStandalone(Language.ITALIAN, documentType, OutputType.XMI, configFile);
    }

    private HeidelTimeModel(Properties properties, DocumentType documentType) {
        LOGGER.trace("Loading tagger for HeidelTime");

        Config.setProps(properties);
        tagger = new HeidelTimeStandalone(Language.ITALIAN, documentType, OutputType.XMI);
    }

    public static HeidelTimeModel getInstance(String configFile, DocumentType documentType) {
        if (instance == null) {
            instance = new HeidelTimeModel(configFile, documentType);
        }

        return instance;
    }

    public static HeidelTimeModel getInstance(Properties properties, DocumentType documentType) {
        if (instance == null) {
            instance = new HeidelTimeModel(properties, documentType);
        }

        return instance;
    }

    public HeidelTimeStandalone getTagger() {
        return tagger;
    }
}
