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

	public static final String PROPERTY_OPERASJON = "operasjon";
	public static final String PROPERTY_FILNAVN = "filnavn";

	public static final String RUTE_HOVED = "{{arenaondemandtojoark.main.route}}";
	public static final String RUTE_INNLESING = "direct:innlesing";
	public static final String RUTE_PROSESSERING = "direct:prosessering";
	public static final String RUTE_RAPPORTERING = "direct:rapportering";
	public static final String RUTE_SHUTDOWN = "direct:shutdown";

	private final ArenaondemandtojoarkProperties arenaondemandtojoarkProperties;
	private final ApplicationContext springContext;

	public ArenaOndemandToJoarkRoute(AvvikService avvikService,
									 ArenaondemandtojoarkProperties arenaondemandtojoarkProperties,
									 ApplicationContext springContext) {
		super(avvikService);
		this.arenaondemandtojoarkProperties = arenaondemandtojoarkProperties;
		this.springContext = springContext;
		MDCGenerate.generateNewCallId();
	}

	@Override
	public void configure() throws Exception {
		//@formatter:off
		super.configure();

		from(RUTE_HOVED)
				.routeId("start_operation")
				.setProperty(PROPERTY_OPERASJON, constant(arenaondemandtojoarkProperties.getOperasjon()))
				.setProperty(PROPERTY_FILNAVN, constant(arenaondemandtojoarkProperties.getFilnavn()))
				.log(INFO, log, "Starter ${exchangeProperty.operasjon} av ${exchangeProperty.filnavn}.")
				.choice()
					.when(simple("${exchangeProperty.operasjon} == 'innlesing'"))
						.to(RUTE_INNLESING)
					.when(simple("${exchangeProperty.operasjon} == 'prosessering'"))
						.to(RUTE_PROSESSERING)
				    .when(simple("${exchangeProperty.operasjon} == 'rapportering'"))
						.to(RUTE_RAPPORTERING)
					.otherwise()
						.log(WARN, log,"Ugyldig operasjon mottatt med verdi ${exchangeProperty.operasjon}.")
				.end()
				.log(INFO, log,"Ferdig med ${exchangeProperty.operasjon} av ${exchangeProperty.filnavn}.")
				.to(RUTE_SHUTDOWN);

		//@formatter:on

		from(RUTE_SHUTDOWN)
				.routeId("shutdown")
				.log(INFO, log, "Avslutter applikasjonen.")
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