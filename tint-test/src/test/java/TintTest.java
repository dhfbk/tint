import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    public static void main(String[] args) {
        String sentenceText;
        try {

            sentenceText = "Ho comprato delle latte.";

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
//            pipeline.setProperty("semafor_ita.stanford.annotators", "tokenize, ssplit");
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
