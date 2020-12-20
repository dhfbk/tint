import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;

public class TokenizerTest {

    public static void main(String[] args) {
        String text = "Ma la guerra non fu molto fortunata. Noi avevamo 260.000 soldati dell'esercito regolare e circa 35.000 volontari; tuttavia le difficoltà erano grandi, sia perché bisognava superare la barriera del Quadrilatero (le fortezze austriache di Verona, Legnago, Mantova e Peschiera) sia perché, sebbene avessimo fatto un prestito di 300.000.000 di lire, le nostre finanze non erano in buone condizioni. Inoltre non tutti i comandanti dell'esercito erano all'altezza del loro compito. A Custoza (24 giugno 1866) noi fummo sconfitti e costretti a ritirarci su Piacenza e su Modena, e a Lissa, in battaglia navale, ebbimo la peggio (20 luglio); mentre invece l'esercito dei volontari, comandato da Garibaldi, riportava considerevoli vittorie nel Trentino, dove s'era inoltrato per divergere le forze austriache, e una parte dell'esercito regolare, comandata dal generale Cialdini, aveva notevoli successi occupando Rovigo, Padova e giungendo a due ore da Gradisca, e un'altra parte, comandata dal Medici, correndo in aiuto di Garibaldi, vinceva ripetutamente gli Austriaci e si avviava su Trento.";
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent, pos, ita_upos, ita_splitter");
        pipeline.load();

        Annotation annotation = pipeline.runRaw(text);

        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            System.out.println(token + " " + token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
        }

    }
}
