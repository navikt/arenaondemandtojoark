package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.xml.rapport.Journalpostrapport;
import no.nav.arenaondemandtojoark.domain.xml.rapport.JournalpostrapportElement;
import no.nav.arenaondemandtojoark.domain.xml.rapport.map.JournalpostrapportMapper;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_FILNAVN;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.RUTE_RAPPORTERING;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class RapporteringRoute extends BaseRoute {

	private static final String PROPERTY_RAPPORTFIL = "rapportfil";

	private static final String JOURNALPOSTRAPPORT_URI = "{{arenaondemandtojoark.sftp.uri}}" +
														 "{{arenaondemandtojoark.sftp.outbound.folder}}" +
														 "{{arenaondemandtojoark.sftp.config}}" +
														 "&fileName=%s";
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmmss");
	private static final String JOURNALPOSTRAPPORT_FILNAVN = "GR81_journalpostrapport_%s.xml";

	private final JournaldataService journaldataService;

	public RapporteringRoute(JournaldataService journaldataService,
							 AvvikService avvikService) {
		super(avvikService);
		this.journaldataService = journaldataService;
	}

	@Override
	public void configure() throws Exception {
		//@formatter:off
		super.configure();

		from(RUTE_RAPPORTERING)
				.routeId("rapportering")
				.log(INFO, log, "Starter generering av rapport for fil=${exchangeProperty.filnavn}")
				.setBody(simple("${exchangeProperty.filnavn}"))
				.bean(journaldataService, "lagJournalpostrapport")
				.split(body(), new RapportAggregator()).streaming().parallelProcessing()
				    .bean(JournalpostrapportMapper.class)
				.end()
				.marshal("rapporteringDataFormat")
				.process(this::genererFilnavn)
				.log(INFO, log, "Skriver rapport til fil=${exchangeProperty.%s}".formatted(PROPERTY_RAPPORTFIL))
				.to(JOURNALPOSTRAPPORT_URI.formatted("${exchangeProperty.%s}".formatted(PROPERTY_RAPPORTFIL)))
				.setBody(simple("${exchangeProperty.filnavn}"))
				.process(exchange -> {
					var filnavn = exchange.getProperty(PROPERTY_FILNAVN, String.class);
					var rapportfil = exchange.getProperty(PROPERTY_RAPPORTFIL, String.class);
					journaldataService.oppdaterStatusTilAvlevert(filnavn, rapportfil);
				})
				.bean(journaldataService, "lagOppsummering")
				.end();

		//@formatter:on
	}

	private void genererFilnavn(Exchange exchange) {
		var filnavn = JOURNALPOSTRAPPORT_FILNAVN.formatted(LocalDateTime.now().format(formatter));

		exchange.setProperty(PROPERTY_RAPPORTFIL, filnavn);
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