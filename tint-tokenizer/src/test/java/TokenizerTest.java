import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreLabel;
import eu.fbk.dh.tint.tokenizer.ItalianTokenizer;
import eu.fbk.utils.core.CommandLine;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * Created by alessio on 04/08/16.
 */

public class TokenizerTest {

    private static String getSentence(List<List<CoreLabel>> sentences) {
        StringBuffer sentenceText = new StringBuffer();
        for (List<CoreLabel> sentence : sentences) {
            sentenceText.append(">");
            for (CoreLabel token : sentence) {
                sentenceText.append(token.toString()).append("|");
            }
            sentenceText.append("\n");
        }
        return sentenceText.toString();
    }

    @Test
    public void Tokenization() {
        ItalianTokenizer tokenizer = new ItalianTokenizer();

        try {
            URL url = Resources.getResource("test-tokenizer.txt");
            String text = Resources.toString(url, Charsets.UTF_8);

            List<List<CoreLabel>> sentences;

            sentences = tokenizer.parse(text, ItalianTokenizer.NewLineType.SINGLE, false, false);
            assertEquals(sentences.size(), 7);
            assertEquals(sentences.get(0).size(), 6);
            assertEquals(sentences.get(0).get(5).originalText(), "...");

            sentences = tokenizer.parse(text, ItalianTokenizer.NewLineType.NOBR, false, false);
            assertEquals(sentences.size(), 5);
            assertEquals(sentences.get(2).size(), 16);
            assertEquals(sentences.get(2).get(7).originalText(), "G.");

            sentences = tokenizer.parse(text, ItalianTokenizer.NewLineType.SINGLE, true, true);
            assertEquals(sentences.get(4).size(), 11);
            assertEquals(sentences.get(0).size(), 5);

            sentences = tokenizer.parse(text, ItalianTokenizer.NewLineType.DOUBLE, false, true);
            assertEquals(sentences.size(), 3);

            sentences = tokenizer.parse(text, ItalianTokenizer.NewLineType.DOUBLE, false, false);
            assertEquals(sentences.size(), 5);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    public static void main(String[] args) {
        ItalianTokenizer tokenizer = new ItalianTokenizer(new File("/Users/alessio/Dropbox/relation-extraction/token-settings-wemapp.xml"));
        List<List<CoreLabel>> sentences = tokenizer.parse("Prova con due acapo\n\n\n \n\n Ciao\n\nBella lì\nEccoci", ItalianTokenizer.NewLineType.DOUBLE, false, false);
        System.out.println(getSentence(sentences));
    }
}
