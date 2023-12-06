package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.config.ArenaondemandtojoarkProperties;
import no.nav.arenaondemandtojoark.util.MDCGenerate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@Slf4j
@Component
public class ArenaOndemandToJoarkRoute extends BaseRoute {

	public static final String PROPERTY_ONDEMAND_ID = "OndemandId";
	public static final String PROPERTY_FILNAVN = "Filnavn";

	public static final String RUTE_INNLESING = "direct:innlesing";
	public static final String RUTE_PROSESSERING = "direct:prosessering";

	private final ApplicationContext springContext;
	private final ArenaondemandtojoarkProperties arenaondemandtojoarkProperties;

	public ArenaOndemandToJoarkRoute(ApplicationContext springContext,
									 AvvikService avvikService,
									 ArenaondemandtojoarkProperties arenaondemandtojoarkProperties) {
		super(avvikService);
		this.springContext = springContext;
		this.arenaondemandtojoarkProperties = arenaondemandtojoarkProperties;
		MDCGenerate.generateNewCallId();
	}

	@Override
	public void configure() throws Exception {
		//@formatter:off
		super.configure();

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
				    .when(simple("${exchangeProperty.operasjon} == 'rapportering'"))
				         .log(INFO, "Starter rapportering av fil")
				         .to("direct:lag_rapport")
					.otherwise()
						.log(WARN, "Ugyldig operasjon mottatt med verdi ${exchangeProperty.operasjon}.")
				.end();

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
}
