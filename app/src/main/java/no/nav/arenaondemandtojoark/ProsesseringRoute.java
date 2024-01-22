package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.validate.JournaldataValidator;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.RUTE_PROSESSERING;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class ProsesseringRoute extends BaseRoute {

	private static final String PROPERTY_ANTALL_JOURNALDATA = "antallJournaldata";

	private static final String RUTE_BEHANDLE_JOURNALDATA = "direct:behandle_journaldata";
	private final JournaldataService journaldataService;
	private final JournaldataValidator journaldataValidator;
	private final ArenaOndemandToJoarkService arenaOndemandToJoarkService;

	public ProsesseringRoute(JournaldataService journaldataService,
							 ArenaOndemandToJoarkService arenaOndemandToJoarkService,
							 AvvikService avvikService) {
		super(avvikService);
		this.journaldataService = journaldataService;
		journaldataValidator = new JournaldataValidator();
		this.arenaOndemandToJoarkService = arenaOndemandToJoarkService;
	}

	@Override
	public void configure() throws Exception {
		//@formatter:off
		super.configure();

		from(RUTE_PROSESSERING)
			.routeId("prosessering")
			.setBody(simple("${exchangeProperty.filnavn}"))
			.bean(journaldataService, "hentJournaldata")
				.log(INFO, log, "Hentet ${body.size()} journaldata fra fil=${exchangeProperty.filnavn}.")
			.setProperty(PROPERTY_ANTALL_JOURNALDATA, simple("${body.size()}"))
				.process(exchange -> log.info("Skal prosessere {} journaldata-elementer.", exchange.getProperty(PROPERTY_ANTALL_JOURNALDATA, Integer.class)))
			.split(body(), new ProgressAggregator()).streaming().parallelProcessing()
				.to(RUTE_BEHANDLE_JOURNALDATA)
			.end()
			.setBody(simple("${exchangeProperty.filnavn}"))
			.bean(journaldataService, "lagOppsummering");

		from(RUTE_BEHANDLE_JOURNALDATA)
			.routeId("behandle_journaldata")
			.bean(journaldataValidator)
			.bean(arenaOndemandToJoarkService)
			.end();

		//@formatter:on
	}

	private static class ProgressAggregator implements AggregationStrategy {

		AtomicInteger counter = new AtomicInteger(0);

		@Override
		public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
			var antallJournaldata = newExchange.getProperty(PROPERTY_ANTALL_JOURNALDATA, Integer.class);
			var antallProsesserte = counter.incrementAndGet();
			var fremdrift = antallProsesserte * 100 / antallJournaldata;

			if (antallProsesserte % 100 == 0)
				log.info("Har prosessert {} av {} journaldata-elementer ({}%).", antallProsesserte, antallJournaldata, fremdrift);


			return oldExchange != null ? oldExchange : newExchange;
		}

		@Override
		public void onCompletion(Exchange exchange) {
			var antallJournaldata = exchange.getProperty(PROPERTY_ANTALL_JOURNALDATA, Integer.class);

			log.info("Har prosessert {} av {} journaldata-elementer (100%).", antallJournaldata, antallJournaldata);
		}
	}

}