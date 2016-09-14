import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by alessio on 16/06/16.
 */

public class HeidelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeidelTest.class);

    public static void main(String[] args) {
        HeidelTimeStandalone heidelTimeStandalone = new HeidelTimeStandalone(Language.ITALIAN, DocumentType.NEWS,
                OutputType.XMI, "/Users/alessio/Documents/scripts/simpatico-dashboard/config.props");

        try {
            String process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            process = heidelTimeStandalone.process(
                    "Notte e alba di fuoco nel Palermitano. Lo straordinario scirocco delle ultime ore - sono stati raggiunti picchi di 39-40 gradi - unito alle raffiche di vento, stanno alimentando decine di roghi scoppiati ieri in tarda sera in diversi comuni della provincia.",
                    new Date());
            System.out.println(process);

            Thread.sleep(100000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
