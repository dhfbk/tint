package eu.fbk.dh.tint.resources.ner;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import org.fbk.cit.hlt.thewikimachine.index.FormPageSearcher;
import org.fbk.cit.hlt.thewikimachine.index.PageFormSearcher;
import org.fbk.cit.hlt.thewikimachine.index.util.FreqSetSearcher;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;

/**
 * Created by alessio on 12/05/16.
 */

public class LoadWikipedia {

    private static final double MIN_PF_FREQ = 0.02;
    private static final double MIN_FREQ = 0.8;

    public static void main(String[] args) {
        final CommandLine cmd = CommandLine
                .parser()
                .withName("ner-extractor")
                .withHeader("Extractor for PER/ORG/LOC")
                .withOption("f", "form-page-path", "Form-page path from Airpedia", "DIR", CommandLine.Type.DIRECTORY_EXISTING, true,
                        false, true)
                .withOption("p", "page-form-path", "Page-form path from Airpedia", "DIR", CommandLine.Type.DIRECTORY_EXISTING, true,
                        false, true)
                .withOption("l", "page-list", "Page-list from Airpedia", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                .withOption(null, "label", "Label (PER, ORG, LOC, ...)", "LABEL", CommandLine.Type.STRING, true, false, true)
                .withLogger(LoggerFactory.getLogger("eu.fbk.fssa")).parse(args);

        final File formPagePath = cmd.getOptionValue("f", File.class);
        final File pageFormPath = cmd.getOptionValue("p", File.class);
        final File listPath = cmd.getOptionValue("l", File.class);
        final File outputPath = cmd.getOptionValue("o", File.class);
        final String label = cmd.getOptionValue("label", String.class);

        try {
            FormPageSearcher formPageSearcher = new FormPageSearcher(formPagePath.getAbsolutePath());
            PageFormSearcher pageFormSearcher = new PageFormSearcher(pageFormPath.getAbsolutePath());

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

            List<String> pages = Files.readLines(listPath, Charsets.UTF_8);
            HashSet<String> pageSet = new HashSet<>();
            pageSet.addAll(pages);

            for (String page : pages) {
                page = page.trim();
                if (page.length() == 0) {
                    continue;
                }

                FreqSetSearcher.Entry[] entries = pageFormSearcher.search(page);

                for (FreqSetSearcher.Entry entry : entries) {
                    if (entry.getFreq() < MIN_PF_FREQ) {
                        continue;
                    }

                    String form = entry.getValue();
                    FreqSetSearcher.Entry[] pEntries = formPageSearcher.search(form);

                    double isThis = 0;

                    for (FreqSetSearcher.Entry pEntry : pEntries) {
                        if (pageSet.contains(pEntry.getValue())) {
                            isThis += pEntry.getFreq();
                        }
                    }

                    if (isThis < MIN_FREQ) {
                        continue;
                    }

                    writer.append(label).append(" ").append(form).append("\n");
                }
            }

            formPageSearcher.close();
            pageFormSearcher.close();
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
