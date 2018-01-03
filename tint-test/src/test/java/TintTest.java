import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    public static void main(String[] args) {
        String sentenceText;
        try {

//            sentenceText = "The trip was very beautiful. Unfortunately, my dog has died in the meantime.";
//            Annotation annotation = new Annotation(sentenceText);
//            Properties properties = new Properties();
//            properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
//            StanfordCoreNLP stanfordCoreNLP = new StanfordCoreNLP(properties);
//            stanfordCoreNLP.annotate(annotation);
//
//            String json = JSONOutputter.jsonPrint(annotation);
//            System.out.println(json);
//
//            System.exit(1);

            sentenceText = "Domani insalatiamo tutti insieme. " +
                    "Il mio cane ha smerdazzato il tuo. " +
                    "Meglio dormirci su. " +
                    "Nel suo mondo color arcobaleno e pieno di pucciosi unicorni si aspettava che LEXenstein fosse un programma in stile Word";

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();

//            pipeline.setProperty("annotators", "ita_toksent, pos, semafor_ita");
            pipeline.setProperty("annotators", "ita_toksent, pos, upos, stem, ita_morpho, ita_lemma");
            pipeline.setProperty("customAnnotatorClass.semafor_ita", "eu.fbk.fcw.semafortranslate.SemaforTranslateAnnotator");
            pipeline.setProperty("customAnnotatorClass.stem", "eu.fbk.fcw.stemmer.corenlp.StemAnnotator");
            pipeline.setProperty("customAnnotatorClass.upos", "eu.fbk.dh.tint.upos.UPosAnnotator");

            pipeline.setProperty("stem.lang", "it");

            pipeline.setProperty("semafor_ita.yandex.key", "trnsl.1.1.20171010T091318Z.1e87b765f05f625b.099f046a438c4dd1efac8bbbd3a6f9f7d80fc866");
            pipeline.setProperty("semafor_ita.engine", "deepl");

            pipeline.setProperty("semafor_ita.stanford.annotators", "tokenize, ssplit, pos, lemma, conll_parse, semafor");
            pipeline.setProperty("semafor_ita.stanford.semafor.model_dir", "/Volumes/Dati/Resources/pikes/models/semafor-orig");
            pipeline.setProperty("semafor_ita.stanford.semafor.use_conll", "true");
            pipeline.setProperty("semafor_ita.stanford.customAnnotatorClass.mst_fake", "eu.fbk.dkm.pikes.depparseannotation.FakeMstParserAnnotator");
            pipeline.setProperty("semafor_ita.stanford.customAnnotatorClass.semafor", "eu.fbk.fcw.semafor.SemaforAnnotator");
            pipeline.setProperty("semafor_ita.stanford.customAnnotatorClass.conll_parse", "eu.fbk.fcw.mate.AnnaParseAnnotator");
            pipeline.setProperty("semafor_ita.stanford.conll_parse.model", "/Volumes/Dati/Resources/pikes/models/anna_parse.model");

            pipeline.setProperty("semafor_ita.aligner.host", "dh-server.fbk.eu");
            pipeline.setProperty("semafor_ita.aligner.port", "9010");
            pipeline.load();

//            Annotation annotation = pipeline.runRaw(sentenceText);
            pipeline.run(sentenceText, System.out, TintRunner.OutputFormat.JSON);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
