import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreLabel;
import eu.fbk.dh.tint.tokenizer.ItalianTokenizer;
import eu.fbk.utils.core.CommandLine;
import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URL;
import java.util.List;

/**
 * Created by alessio on 04/08/16.
 */

public class TokenizerTest {

    private String getSentence(List<List<CoreLabel>> sentences) {
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

            sentences = tokenizer.parse(text, true, false, false);
//            System.out.println(getSentence(sentences));
            assertEquals(sentences.size(), 7);
            assertEquals(sentences.get(0).size(), 6);
            assertEquals(sentences.get(0).get(5).originalText(), "...");

            sentences = tokenizer.parse(text, false, false, false);
//            System.out.println(getSentence(sentences));
            assertEquals(sentences.size(), 5);
            assertEquals(sentences.get(2).size(), 16);
            assertEquals(sentences.get(2).get(7).originalText(), "G.");

            sentences = tokenizer.parse(text, true, true, true);
            System.out.println(getSentence(sentences));
        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
