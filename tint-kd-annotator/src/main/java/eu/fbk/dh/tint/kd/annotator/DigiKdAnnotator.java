package eu.fbk.dh.tint.kd.annotator;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.kd.lib.KD_configuration;
import eu.fbk.dh.kd.lib.KD_core;
import eu.fbk.dh.kd.lib.KD_keyconcept;
import eu.fbk.dh.kd.lib.KD_loader;

import java.util.*;

import static eu.fbk.dh.tint.kd.annotator.DigiKdAnnotations.DH_KEYPHRASE_REQUIREMENT;

/**
 * Created by giovannimoretti on 22/05/16.
 */
public class DigiKdAnnotator implements Annotator {

    KD_configuration configuration = new KD_configuration();
    KD_core.Language lang = KD_core.Language.ENGLISH;
    KD_core kd;

    public DigiKdAnnotator(String annotatorName, Properties prop) {

        this.lang = KD_core.Language.valueOf(prop.getProperty(annotatorName + ".language", "ENGLISH").toUpperCase());

        configuration.languagePackPath = prop.getProperty(annotatorName + ".languageFolder", "languages");

        configuration.only_multiword = Boolean
                .parseBoolean(prop.getProperty(annotatorName + ".only_multiword", "false"));

        configuration.numberOfConcepts = Integer.parseInt(prop.getProperty(annotatorName + ".numberOfConcepts", "20"));
        configuration.local_frequency_threshold = Integer
                .parseInt(prop.getProperty(annotatorName + ".local_frequency_threshold", "2"));
        configuration.max_keyword_length = Integer
                .parseInt(prop.getProperty(annotatorName + ".max_keyword_length", "4"));

        configuration.prefer_specific_concept = KD_configuration.Prefer_Specific_Concept
                .valueOf(prop.getProperty(annotatorName + ".prefer_specific_concept", "MEDIUM").toUpperCase());
        configuration.tagset = KD_configuration.Tagset.STANFORD;
        configuration.column_configuration = KD_configuration.ColumExtraction.TOKEN_LEMMA_POS;
        configuration.group_by = KD_configuration.Group
                .valueOf(prop.getProperty(annotatorName + ".group_by", "NONE").toUpperCase());

        configuration.skip_keyword_with_proper_noun = Boolean
                .parseBoolean(prop.getProperty(annotatorName + ".skip_keyword_with_proper_noun", "false"));
        configuration.skip_proper_noun = Boolean
                .parseBoolean(prop.getProperty(annotatorName + ".skip_proper_noun", "false"));
        configuration.use_pattern_weight = Boolean
                .parseBoolean(prop.getProperty(annotatorName + ".use_pattern_weight", "false"));
        configuration.no_abstract = Boolean.parseBoolean(prop.getProperty(annotatorName + ".no_abstract", "false"));
        configuration.rerank_by_position = Boolean
                .parseBoolean(prop.getProperty(annotatorName + ".rerank_by_position", "false"));

        configuration.skip_keyword_with_not_allowed_words = Boolean
                .parseBoolean(prop.getProperty(annotatorName + ".skip_keyword_with_not_allowed_words", "false"));

        configuration.verbose = false;

        if (Boolean.parseBoolean(prop.getProperty(annotatorName + ".update", "false"))) {
            KD_loader.run_the_updater(this.lang, configuration.languagePackPath);
        }

        int cores = Runtime.getRuntime().availableProcessors();
        KD_core.Threads t;
        switch (cores) {
        case 1:
            t = KD_core.Threads.ONE;
            break;
        case 2:
            t = KD_core.Threads.TWO;
            break;
        case 4:
            t = KD_core.Threads.FOUR;
            break;
        case 6:
            t = KD_core.Threads.SIX;
            break;
        case 8:
            t = KD_core.Threads.EIGHT;
            break;
        case 10:
            t = KD_core.Threads.TEN;
            break;
        case 12:
            t = KD_core.Threads.TWELVE;
            break;
        default:
            t = KD_core.Threads.TWO;
            break;
        }
        if (cores > 12) {
            t = KD_core.Threads.TWELVE;
        }

        this.kd = DigiKdModel.getInstance(t);

    }

    public void annotate(Annotation annotation) {

        StringBuffer doc = new StringBuffer();

        if (annotation.has(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel c : tokens) {
                    //System.out.println(c.word()+"\t"+c.get(CoreAnnotations.LemmaAnnotation.class)+"\t"+c.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                    doc.append(c.word() + "\t" + c.get(CoreAnnotations.LemmaAnnotation.class) + "\t" + c
                            .get(CoreAnnotations.PartOfSpeechAnnotation.class) + "\n");
                }
                doc.append("\n");
            }
        }

        List<DigiKdResult> listOfKeys = new ArrayList<>();

        try {
            for (KD_keyconcept k : kd.extractExpressions(this.lang, this.configuration, null, doc)) {
                listOfKeys
                        .add(new DigiKdResult(k.getString(), k.frequency, k.score, k.getLemmaArray(), k.getTokenArray(),
                                k.getPosList()));
            }

        } catch (NullPointerException n) {

        }
        annotation.set(DigiKdAnnotations.KeyphrasesAnnotation.class, listOfKeys);

    }

    public Set<Requirement> requirementsSatisfied() {
        return Collections.singleton(DH_KEYPHRASE_REQUIREMENT);
    }

    public Set<Requirement> requires() {
        return Collections.unmodifiableSet(
                new ArraySet<Requirement>(TOKENIZE_REQUIREMENT, LEMMA_REQUIREMENT, SSPLIT_REQUIREMENT,
                        POS_REQUIREMENT));

    }
}

