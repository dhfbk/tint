import eu.fbk.dh.tint.inverse.digimorph.annotator.InverseDigiMorph;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by giovannimoretti on 31/01/17.
 */
public class InverseDigiTest {
    public static void main(String[] args) {

        List<String> text = new LinkedList<String>();
        Scanner scanner = new Scanner(System.in);
        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.compareToIgnoreCase("go()") == 0) {
                break;
            }
            text.add(line);

        }
        InverseDigiMorph dm = new InverseDigiMorph();

        for (String s : dm.getInverseMorphology(text)) {
            System.out.println(s);
        }

    }
}
