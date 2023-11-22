package no.nav.arenaondemandtojoark;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.journaldata.map.JournaldataMapper;
import no.nav.arenaondemandtojoark.domain.journaldata.validate.JournaldataValidator;
import no.nav.arenaondemandtojoark.domain.xml.Innlasting;
import no.nav.arenaondemandtojoark.domain.xml.rapport.Journalpostrapport;
import no.nav.arenaondemandtojoark.domain.xml.rapport.JournalpostrapportElement;
import no.nav.arenaondemandtojoark.exception.ArenaondemandtojoarkFunctionalException;
import no.nav.arenaondemandtojoark.exception.ArenaondemandtojoarkTechnicalException;
import no.nav.arenaondemandtojoark.util.MDCGenerate;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Slf4j
@Component
public class ArenaOndemandToJoarkRoute extends RouteBuilder {

	private static final String PROPERTY_ORIGINAL_CSV_LINE = "OriginalCsvLine";
	private static final String PROPERTY_ONDEMAND_ID = "OndemandId";
	private static final String PROPERTY_OUTPUT_FOLDER = "OutputFolder";
	private static final String TECHNICAL_AVVIKSFIL = "technical_avvik";
	private static final String FUNCTIONAL_AVVIKSFIL = "functional_avvik";
	private static final String FUNCTIONAL_JOURNALPOST_FERDIGSTILT_FUNCTIONAL_AVVIK = "journalpost_ferdigstilt_functional_avvik";

	private final ArenaOndemandToJoarkService arenaOndemandToJoarkService;
	private final ApplicationContext springContext;

	public ArenaOndemandToJoarkRoute(ArenaOndemandToJoarkService arenaOndemandToJoarkService,
									 ApplicationContext springContext) {
		this.arenaOndemandToJoarkService = arenaOndemandToJoarkService;
		this.springContext = springContext;
		MDCGenerate.generateNewCallId();
	}

	@Override
	public void configure() throws JAXBException {
		// Alle andre exceptions havner også her med ekstra logging.
//		errorHandler(deadLetterChannel("direct:avviksfil")
//				.log(log)
//				.loggingLevel(ERROR)
//				.logHandled(true)
//				.logExhausted(true)
//				.logExhaustedMessageHistory(false));

//		avviksFilSetup();

		from("{{arenaondemandtojoark.endpointuri}}" +
			 "?{{arenaondemandtojoark.endpointconfig}}" +
			 "&antInclude=*.xml" +
			 "&move=processed/${date:now:yyyyMMdd}/${file:name}")
				.routeId("lese_fil")
				.log(INFO, log, "Starter lesing av ${file:absolute.path}.")
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(Innlasting.class)))
				.setBody(simple("${body.journaldataList}"))
				.split(body(), new RapportAggregator())
					.to("direct:behandle_journaldata")
				.end() // split
				.to("direct:file")
				.log(INFO, log, "Behandlet ferdig ${file:absolute.path}.")
				.to("direct:shutdown");

		from("direct:behandle_journaldata")
				.routeId("behandle_journaldata")
				.bean(new JournaldataMapper())
				.bean(new JournaldataValidator())
				.bean(arenaOndemandToJoarkService)
				.end();

		from("direct:file")
				.marshal(new JaxbDataFormat(JAXBContext.newInstance(Journalpostrapport.class)))
				.to("{{arenaondemandtojoark.endpointuri}}/rapport" +
					"?{{arenaondemandtojoark.endpointconfig}}" +
					"&fileName=placeholder.xml" //FIXME
				);

		this.shutdownSetup();
	}


	// AvviksFilSetup er for å differensiere exception for ferdigstill og resten av prossesen for journalpost. Dette er for å unngå å lage duplisering i databasen.
	public void avviksFilSetup() {
		onException(ArenaondemandtojoarkTechnicalException.class)
				.handled(true)
				.to("direct:" + TECHNICAL_AVVIKSFIL);

//		onException(JournalpostFerdigstillingFunctionalException.class)
//				.handled(true)
//				.to("direct:" + FUNCTIONAL_JOURNALPOST_FERDIGSTILT_FUNCTIONAL_AVVIK);

		onException(ArenaondemandtojoarkFunctionalException.class)
				.handled(true)
				.log(WARN, "Funksjonell feil.")
				.to("direct:" + FUNCTIONAL_AVVIKSFIL);

		buildAvvikFrom(TECHNICAL_AVVIKSFIL);
		buildAvvikFrom(FUNCTIONAL_JOURNALPOST_FERDIGSTILT_FUNCTIONAL_AVVIK);
		buildAvvikFrom(FUNCTIONAL_AVVIKSFIL);
	}

	public void buildAvvikFrom(String avviksFil) {
		from("direct:" + avviksFil)
				.routeId(avviksFil)
				.setBody(exchangeProperty(PROPERTY_ORIGINAL_CSV_LINE))
				.transform(body().append("\n"))
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty." + PROPERTY_OUTPUT_FOLDER + "}_" + avviksFil +".csv"))
				.to("file://{{odtojoark.workdir}}/?fileExist=Append")
				.log(WARN, log, "ondemandId=${exchangeProperty." + PROPERTY_ONDEMAND_ID + "} sendt til " + avviksFil + ". Exception=${exception}");
	}

	public void shutdownSetup() {
		from("direct:shutdown")
				.routeId("shutdown")
				.process(new Processor() {
					Thread stop;

					@Override
					public void process(final Exchange exchange) throws Exception {
						// stop this route using a thread that will stop
						// this route gracefully while we are still running
						if (stop == null) {
							stop = new Thread() {
								@Override
								public void run() {
									try {
										exchange.getContext().shutdown();
										SpringApplication.exit(springContext, () -> 0);
										System.exit(0);
									} catch (Exception e) {
										// ignore
									}
								}
							};
						}

						// start the thread that stops this route
						stop.start();
					}
				});
	}

	private static class RapportAggregator implements AggregationStrategy {

		private final List<JournalpostrapportElement> journalposter = new ArrayList<>();

		@Override
		public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
			JournalpostrapportElement element = (JournalpostrapportElement) newExchange.getIn().getBody();
			journalposter.add(element);

			return newExchange;
		}

		@Override
		public void onCompletion(Exchange exchange) {
			exchange.getIn().setBody(new Journalpostrapport(journalposter));
		}
	}

}
