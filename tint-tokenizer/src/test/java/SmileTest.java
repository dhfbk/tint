import edu.stanford.nlp.ling.CoreLabel;
import eu.fbk.dh.tint.tokenizer.ItalianTokenizer;

import java.util.List;

public class SmileTest {
    public static void main(String[] args) {
        String test = "I am a sdf👹😀 devil";
        ItalianTokenizer tokenizer = new ItalianTokenizer();
        List<List<CoreLabel>> result = tokenizer.parse(test);
        System.out.println(result);
        System.out.println(Character.UnicodeBlock.of("😁".codePointAt(0)));
//        test.codePoints().forEach(i -> {
//            System.out.println(i);
//        });
//        System.out.println(test.codePoints().count());
//        System.out.println(test.length());
//        for (int i = 0; i < test.length(); i++) {
//            System.out.println(test.codePointAt(i));
//        }
    }
}
