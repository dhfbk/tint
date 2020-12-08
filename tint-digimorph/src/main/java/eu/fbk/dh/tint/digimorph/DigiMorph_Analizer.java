package eu.fbk.dh.tint.digimorph;

import com.google.common.base.Joiner;
import org.mapdb.SortedTableMap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Giovanni Moretti at Digital Humanities group at FBK.
 * @version 0.2a
 */
public class DigiMorph_Analizer implements Callable<List<String>> {

    // Volume volume = null;
    private SortedTableMap<String, String> map = null;

    List<String> tokens;

    private ArrayList<String> prefix = new ArrayList() {{
        add("anti");
        add("ante");
        add("arci");
        add("inter");
        add("super");
        add("trans");
        add("ultra");
        add("tri");
        add("bi");
        add("xeno");
        add("zoo");
        add("circon");
        add("circom");
        add("circum");
        add("in");
    }};

    private ArrayList<String> suffix = new ArrayList() {{
        add("li");
        add("lo");
        add("la");
        add("le");
        add("ci");
        add("vi");
        add("ti");
        add("mi");
        add("si");
        add("ne");
        add("gli");
    }};

    //middle suffix
    private ArrayList<String> middle_suffix = new ArrayList() {{
        add("ci");
        add("vi");
        add("ce");
        add("me");
        add("te");
        add("ve");
        add("se");
        add("glie");
    }};

    public DigiMorph_Analizer(List<String> tokens, SortedTableMap<String, String> map) {
        this.tokens = tokens;
        this.map = map;
    }

    public List<String> call() {
        List<String> results = new LinkedList<String>();
        for (String s : this.tokens) {
            results.add(getMorphology(s));
        }
        return results;
    }

    public String getMorphology(String token) {
        String original_token = token;
        String output = "";
        String no_prefix_phase = process_token(token);
        if (no_prefix_phase.length() == 0) {
            for (String p : prefix) {
                if (token.startsWith(p)) {
                    token = token.substring(p.length(), token.length());
                    String prefix_phase = process_token(token);
                    if (prefix_phase.length() > 0) {
                        String prefisso = process_token(p);
                        output = p + "/" + prefix_phase.replace(" ", " " + p);
                    }
                }
            }
        } else {
            output = no_prefix_phase;
        }

        if (output.length() == 0) {
            return original_token;
        } else {
            return output;
        }
        //this.volume.close();

    }

    private String process_token(String token) {
        String orginal_token = token;
        token = token.toLowerCase();
        StringBuffer out_buffer = new StringBuffer();
        String basic_result = this.map.get(token);
        out_buffer.append(basic_result != null ? basic_result : "");

        /////////// fermati qui per risolvere formario completo ////////

        //return token;

        //forme composte
        // suffix

        String suffix_substring = "";

        if (token.endsWith("gli")) {
            suffix_substring = "gli";
        } else {
            for (String suf : this.suffix) {
                if (token.endsWith(suf)) {
                    suffix_substring = suf;
                }
            }
        }

        if (suffix_substring.length() > 0) {
            String head = token.substring(0, token.length() - suffix_substring.length());
            String middle_suffix_substring = "";
            for (String suf : this.middle_suffix) {
                if (head.endsWith(suf)) {
                    middle_suffix_substring = suf;
                }
            }

            head = head.substring(0, head.length() - middle_suffix_substring.length());

            String possible_middle_suffix =
                    middle_suffix_substring.length() > 0 ? map.get(middle_suffix_substring) : "";
            String possible_suffix = suffix_substring.length() > 0 ? map.get(suffix_substring) : "";

            //refine head
            String possible_verb;
            boolean ends_with_double = false;
            char head_ending = '\0';
            if (head.length() > 0) {
                head_ending = head.charAt(head.length() - 1);
            }
            char close_suffix_head = '\0';

            if (possible_middle_suffix.length() > 0) {
                close_suffix_head = middle_suffix_substring.charAt(0);
            } else if (possible_suffix.length() > 0) {
                close_suffix_head = suffix_substring.charAt(0);
            }

            if (head_ending == close_suffix_head) {
                head = head.substring(0, head.length() - 1);
                possible_verb = map.get((head));
            } else {
                possible_verb = map.get((head + "e"));
            }

            if (possible_verb == null && head.endsWith("r")) {

                possible_verb = map.get((head + "re"));
            } else if (possible_verb == null) {
                possible_verb = map.get((head));
            }

            if (possible_verb == null && possible_middle_suffix.length() > 0) {  //try to re add middle suffix
                head = head + middle_suffix_substring;
                middle_suffix_substring = "";
                possible_middle_suffix = "";
                possible_verb = map.get((head));
            }

            if (possible_verb != null) {
                String inf = "";
                String suf = "";
                String mid_suf = "";
                String[] verb_items = possible_verb.split(" ");
                String[] suffix_items = possible_suffix.split(" ");
                String[] mid_suffix_items = possible_middle_suffix.split(" ");

                List<String> infiniti = new ArrayList<String>();
                List<String> mid_suff = new ArrayList<String>();
                List<String> suff = new ArrayList<String>();

                // System.out.println(possible_verb);

                for (String v : verb_items) {
                    if (v.contains("+infinito") || v.contains("impr") || v.contains("indic") || v.contains("part") || v.contains("gerundio")) {
                        infiniti.add(" " + v);
                        inf += " " + v;
                    }
                }

                for (String f : mid_suffix_items) {
                    if (f.contains("+pron")) {
                        mid_suff.add("/" + f);
                    }
                }
                for (String f : suffix_items) {
                    if (f.contains("+pron")) {
                        suff.add("/" + f);
                    }
                }
                List<List<String>> lists = new ArrayList<List<String>>();
                lists.add(infiniti);
                lists.add(mid_suff);
                lists.add(suff);

                List<String> results = new ArrayList<String>();

                if (inf.length() > 0) {
                    if (mid_suff.size() > 0) {
                        for (String verb_hypernym : infiniti) {
                            for (String object_hypernym : mid_suff) {
                                for (String subject_hypernym : suff) {
                                    results.add(verb_hypernym + object_hypernym + subject_hypernym);
                                }
                            }
                        }
                    } else {
                        for (String verb_hypernym : infiniti) {
                            for (String subject_hypernym : suff) {
                                results.add(verb_hypernym + subject_hypernym);
                            }
                        }
                    }
                    out_buffer.append(Joiner.on(" ").join(results));

                }
            }
        }
        if (out_buffer.length() == 0) {
            return "";
        } else {
            return (orginal_token + out_buffer.toString());
        }
    }

}
