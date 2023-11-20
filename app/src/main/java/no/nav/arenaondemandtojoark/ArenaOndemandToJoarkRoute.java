package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.journaldata.map.JournaldataMapper;
import no.nav.arenaondemandtojoark.domain.journaldata.validate.JournaldataValidator;
import no.nav.arenaondemandtojoark.exception.ArenaondemandtojoarkFunctionalException;
import no.nav.arenaondemandtojoark.exception.ArenaondemandtojoarkTechnicalException;
import no.nav.arenaondemandtojoark.util.MDCGenerate;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static org.apache.camel.LoggingLevel.ERROR;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class ArenaOndemandToJoarkRoute extends RouteBuilder {
	private static final String PROPERTY_ORIGINAL_CSV_LINE = "OriginalCsvLine";
	private static final String PROPERTY_ONDEMAND_ID = "OndemandId";
	private static final String PROPERTY_OUTPUT_FOLDER = "OutputFolder";
	private static final String TECHNICAL_AVVIKSFIL = "technical_avvik";
	private static final String FUNCTIONAL_AVVIKSFIL = "functional_avvik";
	private static final String FUNCTIONAL_JOURNALPOST_FERDIGSTILT_FUNCTIONAL_AVVIK = "journalpost_ferdigstilt_functional_avvik";

	private static final String LES_FIL_ROUTE = "file://{{odtojoark.workdir}}?antInclude=*.xml" +
												"&antExclude=*_avvik.xml" +
												"&initialDelay=5000" +
												"&repeatCount=1" +
												"&noop=true" +
												"&maxMessagesPerPoll=1" +
												"&charset=UTF-8";

	private final ArenaOndemandToJoarkService arenaOndemandToJoarkService;
	private final ApplicationContext springContext;

	public ArenaOndemandToJoarkRoute(ArenaOndemandToJoarkService arenaOndemandToJoarkService,
									 ApplicationContext springContext) {
		this.arenaOndemandToJoarkService = arenaOndemandToJoarkService;
		this.springContext = springContext;
		MDCGenerate.generateNewCallId();
	}

	@Override
	public void configure() {
		// Alle andre exceptions havner også her med ekstra logging.
		errorHandler(deadLetterChannel("direct:avviksfil")
				.log(log)
				.loggingLevel(ERROR)
				.logHandled(true)
				.logExhausted(true)
				.logExhaustedMessageHistory(false));

		this.avviksFilSetup();

		from(LES_FIL_ROUTE)
				.routeId("lese_fil")
				.log(INFO, log, "Starter lesing av ${file:absolute.path}.")
				//.setProperty(PROPERTY_OUTPUT_FOLDER, simple("${date:now:yyyy-MM-dd_HHmmss}"))
				//.split(body().tokenize("\n")).streaming().parallelProcessing()
				//.setProperty(PROPERTY_ORIGINAL_CSV_LINE, body())
				.unmarshal(new JaxbDataFormat()) //TODO sjekk dette
				.setBody(simple("${body[0]}"))
				.to("direct:behandle_journaldata")
				.end() // split
				.log(INFO, log, "Behandlet ferdig ${file:absolute.path}.")
				.to("direct:{{odtojoark.camel.shutdown}}");

		from("direct:behandle_journaldata")// step to create Journaldata
				.routeId("behandle_journaldata")
				.setProperty(PROPERTY_ONDEMAND_ID, simple("${body[0]}"))
				.log(INFO, log, "ondemandId=${exchangeProperty." + PROPERTY_ONDEMAND_ID + "} under behandling.")
				.bean(new JournaldataMapper())
				.bean(new JournaldataValidator())
				.bean(arenaOndemandToJoarkService)
				.end();

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
				.to("direct:" + FUNCTIONAL_AVVIKSFIL);

		this.buildAvvikFrom(TECHNICAL_AVVIKSFIL);
		this.buildAvvikFrom(FUNCTIONAL_JOURNALPOST_FERDIGSTILT_FUNCTIONAL_AVVIK);
		this.buildAvvikFrom(FUNCTIONAL_AVVIKSFIL);
	}

	public void buildAvvikFrom(String avviksFil) {
		from("direct:" + avviksFil)
				.routeId(avviksFil)
				.setBody(exchangeProperty(PROPERTY_ORIGINAL_CSV_LINE))
				.transform(body().append("\n"))
				.setHeader(Exchange.FILE_NAME, simple("${exchangeProperty." + PROPERTY_OUTPUT_FOLDER + "}_" + avviksFil +".csv"))
				.to("file://{{odtojoark.workdir}}/?fileExist=Append")
				.log(LoggingLevel.WARN, log, "ondemandId=${exchangeProperty." + PROPERTY_ONDEMAND_ID + "} sendt til " + avviksFil +". Exception=${exception}");
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
}
