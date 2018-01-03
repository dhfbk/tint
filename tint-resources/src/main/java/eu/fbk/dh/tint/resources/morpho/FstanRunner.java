package eu.fbk.dh.tint.resources.morpho;


import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by alessio on 20/07/15.
 */

public class FstanRunner {

    String command, model;

    public FstanRunner(String command, String model) {
        this.command = command;
        this.model = model;
    }

    public ArrayList<String[]> run(ArrayList<String> requests) {

        ArrayList<String[]> ret = new ArrayList<>();

        try {

            ProcessBuilder pb = new ProcessBuilder(command, model);
            pb.redirectErrorStream(true);

            Process process;
            OutputStream stdin;
            InputStream stdout;
            BufferedReader reader;
            process = pb.start();
            stdin = process.getOutputStream();
            stdout = process.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stdout));
            String line;

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

            for (String request : requests) {
                writer.write(request);
                writer.newLine();
            }

            writer.flush();
            writer.close();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                ret.add(parts);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public HashSet<String> getTypes(String word) {
        ArrayList<String> requests = new ArrayList<>();
        requests.add(word);

        ArrayList<String[]> strings = new ArrayList<>();
        strings = run(requests);

        HashSet<String> types = new HashSet<>();
        for (String[] parts : strings) {
            for (String part : parts) {
                String[] subparts = part.trim().split("\\+");
                if (subparts.length > 1) {
                    types.add(subparts[1]);
                }
            }

        }

        return types;
    }

    public HashSet<String[]> getParts(String word) {
        ArrayList<String> requests = new ArrayList<>();
        requests.add(word);

        ArrayList<String[]> strings = new ArrayList<>();
        strings = run(requests);

        HashSet<String[]> types = new HashSet<>();
        for (String[] parts : strings) {
            for (String part : parts) {
                String[] subparts = part.trim().split("\\+");
                if (subparts.length > 1) {
                    types.add(subparts);
                }
            }

        }

        return types;
    }

    public ArrayList<String> get(String word) {
        return get(word, null);
    }

    public ArrayList<String> get(String word, @Nullable String type) {
        return get(word, type, null);
    }

    public ArrayList<String> get(String word, @Nullable String type, @Nullable ArrayList<String[]> rq) {
        ArrayList<String> requests = new ArrayList<>();
        requests.add(word);

        HashSet<String> words = new HashSet<>();

        if (type == null) {
            type = "";
        } else {
            if (type.equals("a")) {
                type = "adj";
            }
            if (type.equals("r")) {
                type = "adv";
            }
            if (type.equals("NOUN")) {
                type = "n";
            }
            if (type.equals("VERB")) {
                type = "v";
            }
            if (type.equals("ADJECTIVE")) {
                type = "adj";
            }
            if (type.equals("ADVERB")) {
                type = "adv";
            }
            if (type.equals("PREPOSITION")) {
                type = "prep";
            }
            if (type.equals("DETERMINER")) {
                type = "art";
            }
            if (type.equals("PRONOUN")) {
                type = "pron";
            }
            if (type.equals("PUNCTUATION")) {
                type = "punc";
            }
            if (type.equals("OTHER")) {
                type = "";
            }
        }

        try {
            if (rq == null) {
                rq = run(requests);
            }

            for (String[] parts : rq) {
                for (int i = 1; i < parts.length; i++) {
                    if (parts[i].length() > 0) {
                        String[] subparts = parts[i].trim().split("\\+");
                        if (type.length() > 0) {
                            try {
                                if (subparts[1].equals(type)) {
                                    String[] subsubparts = subparts[0].trim().split("~");
                                    words.add(subsubparts[subsubparts.length - 1]);
                                }
                            } catch (Exception e) {
                                words.add(subparts[0]);
                                // System.out.println(Arrays.toString(subparts));
                            }
                        } else {
                            for (@SuppressWarnings("unused") String part : subparts) {
                                String[] subsubparts = subparts[0].trim()
                                        .split("~"); //TODO: shouldn't this be part instead of subparts?
                                words.add(subsubparts[subsubparts.length - 1]);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        ArrayList<String> ret = new ArrayList<String>();
        ret.addAll(words);
        if (ret.size() < 1) {
            ret.add(word);
        }
        return ret;
    }

    public static void main(String[] args) {
        String command = "/Users/alessio/Documents/scripts/textpro/modules/MorphoPro/bin/fstan/x86_64/fstan";
        String model = "/Users/alessio/Documents/scripts/textpro/modules/MorphoPro/models/italian-utf8.fsa";

        FstanRunner runner = new FstanRunner(command, model);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("vincerlo");

        ArrayList<String[]> list = runner.run(strings);
        for (String[] strings1 : list) {
            System.out.println(Arrays.toString(strings1));
        }

    }

}