package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.config.ArenaondemandtojoarkProperties;
import no.nav.arenaondemandtojoark.domain.xml.rapport.Journalpostrapport;
import no.nav.arenaondemandtojoark.domain.xml.rapport.JournalpostrapportElement;
import no.nav.arenaondemandtojoark.exception.ArenaondemandtojoarkNonRetryableException;
import no.nav.arenaondemandtojoark.exception.retryable.ArenaondemandtojoarkRetryableException;
import no.nav.arenaondemandtojoark.util.MDCGenerate;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
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

	public static final String PROPERTY_ONDEMAND_ID = "OndemandId";
	public static final String PROPERTY_FILNAVN = "Filnavn";

	public static final String RUTE_INNLESING = "direct:innlesing";
	public static final String RUTE_PROSESSERING = "direct:prosessering";

	private final ApplicationContext springContext;
	private final AvvikService avvikService;
	private final ArenaondemandtojoarkProperties arenaondemandtojoarkProperties;

	public ArenaOndemandToJoarkRoute(ApplicationContext springContext,
									 AvvikService avvikService,
									 ArenaondemandtojoarkProperties arenaondemandtojoarkProperties) {
		this.avvikService = avvikService;
		this.springContext = springContext;
		this.arenaondemandtojoarkProperties = arenaondemandtojoarkProperties;
		MDCGenerate.generateNewCallId();
	}

	@Override
	public void configure() {
		//@formatter:off

		onException(ArenaondemandtojoarkRetryableException.class,
				ArenaondemandtojoarkNonRetryableException.class)
				.log("Håndterer definerte exceptions: ${exception}")
				.bean(avvikService)
				.handled(true)
				.end();

		onException(Exception.class)
				.log("Håndterer alle exceptions: ${exception}")
				.bean(avvikService)
				.handled(true)
				.end();

		from("timer://runOnce?repeatCount=1&delay=1000")
				.routeId("start_operation")
				.setProperty("operasjon", constant(arenaondemandtojoarkProperties.getOperasjon()))
				.setProperty("filnavn", constant(arenaondemandtojoarkProperties.getFilnavn()))
				.choice()
					.when(simple("${exchangeProperty.operasjon} == 'innlesing'"))
						.log(INFO, "Starter innlesing av fil")
						.to(RUTE_INNLESING)
					.when(simple("${exchangeProperty.operasjon} == 'prosessering'"))
						.log(INFO, "Starter prosessering av fil")
						.to(RUTE_PROSESSERING)
					.otherwise()
						.log(WARN, "Ugyldig operasjon mottatt med verdi ${exchangeProperty.operasjon}.")
				.end();

//				.to("direct:lag_rapport")
//				.log(INFO, log, "Behandlet ferdig ${file:absolute.path}.")
//				.end();



//		from("direct:lag_rapport")
//				.marshal(new JaxbDataFormat(JAXBContext.newInstance(Journalpostrapport.class)))
//				.to("{{arenaondemandtojoark.endpointuri}}/rapport" +
//					"?{{arenaondemandtojoark.endpointconfig}}" +
//					"&fileName=placeholder.xml" //FIXME
//				);
//
//		this.shutdownSetup();
		//@formatter:on
	}

	public void shutdownSetup() {
		from("direct:shutdown")
				.routeId("shutdown")
				.process(new Processor() {
					Thread stop;

					@Override
					public void process(final Exchange exchange) {
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
