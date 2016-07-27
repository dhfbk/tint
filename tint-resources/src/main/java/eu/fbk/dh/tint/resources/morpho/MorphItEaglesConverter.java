package eu.fbk.dh.tint.resources.morpho;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.io.Files;
import eu.fbk.dh.tint.textpro.FstanRunner;
import eu.fbk.dkm.utils.CommandLine;
import eu.fbk.dkm.utils.FrequencyHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 18/05/16.
 */

public class MorphItEaglesConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MorphItEaglesConverter.class);
    private static Pattern morphoType = Pattern.compile("^([A-Z0-9-]+):?");
    private static Pattern fstanPattern = Pattern.compile("([^~]+~)?[^+]+(\\+.*)");

    static HashMap<String, String> noLemmaTypes = new HashMap<>();
    static HashMultimap<String, String> manuallyMapped = HashMultimap.create();
    static HashSet<String> skipTypes = new HashSet<>();

    static {
        noLemmaTypes.put("PON", "[PUNCT]");
        noLemmaTypes.put("SENT", "[PUNCT]");
        noLemmaTypes.put("SMI", "[SMILE]");
        noLemmaTypes.put("SYM", "[SYMBOL]");
        noLemmaTypes.put("ABL", "[ABL]");

        skipTypes.add("AUX");
        skipTypes.add("CAU");
        skipTypes.add("MOD");
        skipTypes.add("TALE");
        skipTypes.add("ASP");
        skipTypes.add("DET-WH");
        skipTypes.add("CE");
        skipTypes.add("CI");
        skipTypes.add("SI");
        skipTypes.add("NPR");
        skipTypes.add("WH-CHE");
        skipTypes.add("ART-M");
        skipTypes.add("ART-F");

        manuallyMapped.put("PRO-DEMO-F-P", "+pron+_+m+3+plur+dim");
        manuallyMapped.put("PRO-PERS-1-M-S", "+pron+nom+_+1+sing+strong");
        manuallyMapped.put("PRO-PERS-1-M-P", "+pron+nom+_+1+plur+strong");
        manuallyMapped.put("PRO-PERS-1-F-S", "+pron+nom+_+1+sing+strong");
        manuallyMapped.put("PRO-PERS-1-F-P", "+pron+nom+_+1+plur+strong");
        manuallyMapped.put("PRO-PERS-2-F-P", "+pron+nom+_+2+plur+strong");
        manuallyMapped.put("PRO-PERS-2-M-P", "+pron+nom+_+2+plur+strong");
        manuallyMapped.put("PRO-PERS-2-F-S", "+pron+nom+_+2+sing+strong");
        manuallyMapped.put("PRO-PERS-2-M-S", "+pron+nom+_+2+sing+strong");
        manuallyMapped.put("PRO-PERS-CLI-3-F-P", "+pron+acc+f+3+plur+clit");
        manuallyMapped.put("PRO-PERS-CLI-3-M-S", "+pron+acc+m+3+sing+clit");
        manuallyMapped.put("PRO-PERS-CLI-2-F-P", "+pron+acc+f+2+plur+clit");
        manuallyMapped.put("PRO-PERS-CLI-2-F-S", "+pron+acc+f+2+sing+clit");
        manuallyMapped.put("PRO-PERS-CLI-1-F-S", "+pron+acc+f+1+sing+clit");
        manuallyMapped.put("PRO-PERS-CLI-1-F-P", "+pron+acc+f+1+plur+clit");
        manuallyMapped.put("PRO-PERS-CLI-1-M-P", "+pron+acc+m+1+plur+clit");
        manuallyMapped.put("PRO-WH-F-P", "+pron+_+_+3+plur+int");
        manuallyMapped.put("PRO-WH-M-S", "+pron+_+_+3+sing+int");
        manuallyMapped.put("PRO-WH-F-S", "+pron+_+_+3+sing+int");
        manuallyMapped.put("PRO-WH-M-P", "+pron+_+_+3+plur+int");
        manuallyMapped.put("PRO-INDEF-M-P", "+pron+_+m+3+sing+ind");
        manuallyMapped.put("PRO-POSS-F-P", "+pron+f+plur+pst+poss");
        manuallyMapped.put("PRO-POSS-F-S", "+pron+f+sing+pst+poss");
        manuallyMapped.put("PRO-POSS-M-P", "+pron+m+plur+pst+poss");
        manuallyMapped.put("PRO-POSS-M-S", "+pron+m+sing+pst+poss");
        manuallyMapped.put("PRO-INDEF-F-P", "+pron+_+f+3+plur+ind");
        manuallyMapped.put("PRO-INDEF-M-P", "+pron+_+m+3+plur+ind");
        manuallyMapped.put("PRO-INDEF-F-S", "+pron+_+f+3+sing+ind");
        manuallyMapped.put("PRO-INDEF-M-S", "+pron+_+m+3+sing+ind");

        manuallyMapped.put("VER:part+past+p+f+gli", "+v+part+pass+f+nil+plur/gli~pro+pron+dat+_+3+_");
        manuallyMapped.put("VER:impr+pres+2+p", "+v+imp+pres+nil+2+plur");
        manuallyMapped.put("VER:impr+pres+1+p", "+v+imp+pres+nil+1+plur");
        manuallyMapped.put("VER:sub+pres+1+s", "+v+cong+pres+nil+1+sing");
        manuallyMapped.put("VER:sub+pres+1+p", "+v+cong+pres+nil+1+plur");
        manuallyMapped.put("VER:sub+pres+3+s", "+v+cong+pres+nil+3+sing");
        manuallyMapped.put("VER:impr+pres+2+p+veli",
                "+v+imp+pres+nil+2+plur/vi~voi+pron+dat+_+2+plur/li~pro+pron+acc+m+3+plur");
        manuallyMapped.put("VER:impr+pres+2+s+celo",
                "+v+imp+pres+nil+2+sing/ci~pro+pron+dat+_+1+plur/lo~pro+pron+acc+m+3+sing");
        manuallyMapped
                .put("VER:impr+pres+2+s+celo", "+v+imp+pres+nil+2+sing/ci~loc+pron+loc+_+3+_/lo~pro+pron+acc+m+3+sing");
        manuallyMapped
                .put("VER:impr+pres+2+s+celi", "+v+imp+pres+nil+2+sing/ci~loc+pron+loc+_+3+_/lo~pro+pron+acc+m+3+plur");
        manuallyMapped
                .put("VER:impr+pres+2+p+celi", "+v+imp+pres+nil+2+plur/ci~loc+pron+loc+_+3+_/lo~pro+pron+acc+m+3+plur");
        manuallyMapped.put("VER:impr+pres+2+s+mele",
                "+v+imp+pres+nil+2+sing/mi~io+pron+dat+_+1+sing/le~pro+pron+acc+f+3+plur");
        manuallyMapped.put("VER:part+pres+s+m", "+v+part+pres+nil+nil+sing");
        manuallyMapped.put("VER:part+past+s+f+ne", "+v+part+pass+f+nil+sing/ne~part+pron+gen+_+3+_");
        manuallyMapped.put("VER:part+past+s+f+gli", "+v+part+pass+f+nil+sing/gli~pro+pron+dat+_+3+_");
        manuallyMapped.put("VER:ger+pres+vene",
                "+v+gerundio+pres+nil+nil+nil/vi~voi+pron+dat+_+2+plur/ne~part+pron+gen+_+3+_");
        manuallyMapped.put("VER:ger+pres+celo",
                "+v+gerundio+pres+nil+nil+nil/ci~pro+pron+dat+_+1+plur/lo~pro+pron+acc+m+3+sing");
        manuallyMapped.put("VER:ger+pres+celi",
                "+v+gerundio+pres+nil+nil+nil/ci~pro+pron+dat+_+1+plur/lo~pro+pron+acc+m+3+plur");
        manuallyMapped.put("VER:ger+pres+cela",
                "+v+gerundio+pres+nil+nil+nil/ci~pro+pron+dat+_+1+plur/lo~pro+pron+acc+f+3+sing");
        manuallyMapped.put("VER:part+pres+p+f", "+v+part+pres+nil+nil+plur");
        manuallyMapped.put("VER:sub+impf+2+s", "+v+cong+imperf+nil+2+sing");
        manuallyMapped.put("VER:inf+pres+vele",
                "+v+gerundio+pres+nil+nil+nil/vi~voi+pron+dat+_+2+plur/le~pro+pron+acc+f+3+plur");

        manuallyMapped.put("ADJ:comp+f+p", "+adj+_+plur+pst");
        manuallyMapped.put("ADJ:comp+f+s", "+adj+_+sing+pst");
        manuallyMapped.put("ADJ:comp+m+s", "+adj+_+sing+pst");
        manuallyMapped.put("ADJ:comp+m+p", "+adj+_+plur+pst");

        manuallyMapped.put("DET-NUM-CARD", "+adj+_+_+pst+num");
        manuallyMapped.put("DET-POSS:m+s", "+adj+m+sing+pst+poss");
        manuallyMapped.put("DET-POSS:m+p", "+adj+m+plur+pst+poss");
    }

    public static void main(String[] args) {
        final CommandLine cmd = CommandLine
                .parser()
                .withName("morphit-converter")
                .withHeader("Convert Morph-It dataset to be compliant with fstan")
                .withOption("i", "input", "input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                .withOption("o", "output", "output file", "FILE", CommandLine.Type.FILE, true, false, true)
                .withOption("f", "fstan-command", "fstan TextPro command", "COMMAND", CommandLine.Type.FILE_EXISTING,
                        true, false, true)
                .withOption("m", "fstan-model", "fstan TextPro model", "MODEL FILE", CommandLine.Type.FILE_EXISTING,
                        true, false, true)
                .withOption(null, "no-fstan", "Do not use fstan for initial population")
                .withOption(null, "use-spaces", "Use spaces (instead of tabs) as separator")
                .withLogger(LoggerFactory.getLogger("eu.fbk.dh")).parse(args);

        final File inputPath = cmd.getOptionValue("i", File.class);
        final File outputPath = cmd.getOptionValue("o", File.class);

        final String fstanCommand = cmd.getOptionValue("f", String.class);
        final String fstanModel = cmd.getOptionValue("m", String.class);

        boolean useFstan = !cmd.hasOption("no-fstan");
        boolean useSpaces = cmd.hasOption("use-spaces");

        char separator = '\t';
        if (useSpaces) {
            separator = ' ';
        }

        FstanRunner runner = new FstanRunner(fstanCommand, fstanModel);
        HashSet<String> buffer = new HashSet<>();

        HashSet<String> allForms = new HashSet<>();

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
            HashSet<String> types = new HashSet<>();

            HashMultimap<String, String> typeToForm = HashMultimap.create();
            FrequencyHashSet<String> formMeanings = new FrequencyHashSet<>();

            List<String> lines = Files.readLines(inputPath, Charsets.ISO_8859_1);
            for (String line : lines) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    LOGGER.error("Invalid line: {}", line);
                    continue;
                }

                String form = parts[0];
                String lemma = parts[1];
                String morpho = parts[2];

                Matcher matcher = morphoType.matcher(morpho);
                if (!matcher.find()) {
                    LOGGER.warn("Invalid pattern: {}", morpho);
                    continue;
                }

                String type = matcher.group(1);
                if (noLemmaTypes.containsKey(type)) {
                    continue;
                }

                allForms.add(form);

                if (skipTypes.contains(type)) {
                    continue;
                }
                if (manuallyMapped.keys().contains(morpho)) {
                    continue;
                }

                types.add(morpho);
                typeToForm.put(morpho, form);
                formMeanings.add(form);
            }

            for (String form : formMeanings.keySet()) {
                if (formMeanings.get(form) == 1) {
                    buffer.add(form);
                }
            }

            HashMap<String, String> mappings = new HashMap<>();

            for (String type : typeToForm.keySet()) {

                HashSet<String> nonAmbiguous = new HashSet<>();
                for (String form : typeToForm.get(type)) {
                    if (buffer.contains(form)) {
                        nonAmbiguous.add(form);
                    }

                    if (nonAmbiguous.size() > 1000) {
                        break;
                    }
                }

                ArrayList<String> toFstan = new ArrayList<>(nonAmbiguous);
                ArrayList<String[]> run = runner.run(toFstan);

                FrequencyHashSet<String> fstanForms = new FrequencyHashSet<>();

                for (int i = 0; i < toFstan.size(); i++) {
                    String[] res = run.get(i);

                    if (res.length == 1) {
                        continue;
                    }

                    for (int j = 1; j < res.length; j++) {
                        String okForm = res[j];
                        Matcher matcher = fstanPattern.matcher(okForm);
                        if (!matcher.find()) {
                            LOGGER.error("Error in form: {}", okForm);
                            continue;
                        }

                        String suffix = matcher.group(2);
                        fstanForms.add(suffix);
                    }
                }

                if (fstanForms.size() == 0) {
                    LOGGER.warn("No forms for: {}", type);
                    continue;
                }

                String mf = fstanForms.mostFrequent();

                mappings.put(type, mf);
            }

            ArrayList<String> allFormsArray = new ArrayList<>(allForms);
            HashMultimap<String, String> fstanMorpho = HashMultimap.create();

            if (useFstan) {
                LOGGER.info("Running fstan");
                ArrayList<String[]> run = runner.run(allFormsArray);
                for (int i = 0; i < allFormsArray.size(); i++) {
                    String form = allFormsArray.get(i);
                    String[] morphos = run.get(i);
                    if (morphos.length > 1) {
                        for (int j = 1; j < morphos.length; j++) {
                            String morpho = morphos[j];
                            fstanMorpho.put(form, morpho);
                        }
                    }
                }
            }

            LOGGER.info("Adding unknown forms");
            lines = Files.readLines(inputPath, Charsets.ISO_8859_1);
            for (String line : lines) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    LOGGER.error("Invalid line: {}", line);
                    continue;
                }

                String form = parts[0];
                String lemma = parts[1];
                if (fstanMorpho.containsKey(form)) {
                    for (String s : fstanMorpho.get(form)) {
                        writer.append(form).append(separator)
                                .append(lemma).append(separator)
                                .append(s).append("\n");
                    }
                    continue;
                }

                String morpho = parts[2];

                Matcher matcher = morphoType.matcher(morpho);
                if (!matcher.find()) {
                    LOGGER.warn("Invalid pattern: {}", morpho);
                    continue;
                }

                String type = matcher.group(1);
                if (skipTypes.contains(type)) {
                    continue;
                }
                if (noLemmaTypes.containsKey(type)) {
                    writer.append(form).append(separator)
                            .append(lemma).append(separator)
                            .append(noLemmaTypes.get(type)).append("\n");
                    continue;
                }

                String finalMorpho = null;

                if (manuallyMapped.keys().contains(morpho)) {
                    for (String m : manuallyMapped.get(morpho)) {
                        finalMorpho = lemma + m;
                    }
                }

                if (finalMorpho == null) {
                    String eaglesMorpho = mappings.get(morpho);
                    if (eaglesMorpho == null) {
                        LOGGER.error(morpho);
                        System.exit(1);
                    }

                    finalMorpho = lemma + eaglesMorpho;
                }

                writer.append(form).append(separator)
                        .append(lemma).append(separator);
                if (finalMorpho.contains("/")) {
                    writer.append(lemma).append("~");
                }
                writer.append(finalMorpho).append("\n");
            }

            writer.append("il").append(separator).append("il").append(separator).append("il+art+m+sing").append("\n");
            writer.append("lo").append(separator).append("il").append(separator).append("il+art+m+sing").append("\n");
            writer.append("la").append(separator).append("la").append(separator).append("la+art+f+sing").append("\n");
            writer.append("i").append(separator).append("il").append(separator).append("il+art+m+plur").append("\n");
            writer.append("gli").append(separator).append("il").append(separator).append("il+art+m+plur").append("\n");
            writer.append("le").append(separator).append("la").append(separator).append("la+art+f+plur").append("\n");

            writer.append("un").append(separator).append("un").append(separator).append("un+art+m+sing").append("\n");
            writer.append("uno").append(separator).append("un").append(separator).append("un+art+m+sing").append("\n");
            writer.append("una").append(separator).append("una").append(separator).append("una+art+f+sing").append("\n");

            writer.close();
        } catch (Exception e) {
//            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
