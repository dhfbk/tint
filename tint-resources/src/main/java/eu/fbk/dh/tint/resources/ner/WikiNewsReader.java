package eu.fbk.dh.tint.resources.ner;

import eu.fbk.twm.utils.ExtractorParameters;
import eu.fbk.twm.wiki.xmldump.AbstractWikipediaExtractor;

import java.util.Locale;

/**
 * Created by alessio on 26/01/17.
 */

public class WikiNewsReader extends AbstractWikipediaExtractor {

    public WikiNewsReader(int numThreads, int numPages, Locale locale, String configurationFolder) {
        super(numThreads, numPages, locale, configurationFolder);
    }

    @Override public void start(ExtractorParameters extractorParameters) {

    }

    @Override public void disambiguationPage(String text, String title, long wikiID) {

    }

    @Override public void categoryPage(String text, String title, long wikiID) {

    }

    @Override public void templatePage(String text, String title, long wikiID) {

    }

    @Override public void redirectPage(String text, String title, long wikiID) {

    }

    @Override public void contentPage(String text, String title, long wikiID) {
        if (title.startsWith("Wikinotizie:")) {
            return;
        }
        
        System.out.println(title);
        System.out.println(text);
        System.out.println("--------");
    }

    @Override public void portalPage(String text, String title, long wikiID) {

    }

    @Override public void projectPage(String text, String title, long wikiID) {

    }

    @Override public void filePage(String text, String title, long wikiID) {

    }

    public static void main(String[] args) {
        WikiNewsReader wikiNewsReader = new WikiNewsReader(8, Integer.MAX_VALUE, new Locale("it"), "/Users/alessio/Documents/scripts/twm-lib/configuration");
        wikiNewsReader.startProcess("/Users/alessio/Desktop/itwikinews-20170120-pages-articles.xml");
    }
}
