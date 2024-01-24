package no.nav.arenaondemandtojoark;

import jakarta.xml.bind.JAXBContext;
import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.db.map.JournaldataMapper;
import no.nav.arenaondemandtojoark.domain.xml.Innlasting;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_FILNAVN;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.RUTE_INNLESING;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class InnlesingRoute extends BaseRoute {

	private static final Integer JOURNALPOST_LOGGBLOKK_STOERRELSE = 100;

	public static final String LES_FRA_FILOMRAADE_URI = "{{arenaondemandtojoark.sftp.uri}}" +
														"{{arenaondemandtojoark.sftp.inbound.folder}}" +
														"{{arenaondemandtojoark.sftp.config}}" +
														"&include={{arenaondemandtojoark.filnavn}}" +
														"stepwise=false" +
														"&charset=ISO-8859-1";

	private static final String RUTE_MAP_JOURNALDATA = "direct:map_journaldata";
	private static final String RUTE_LAGRE_JOURNALDATA = "direct:lagre_journaldata_i_bulk";

	private final JournaldataMapper journaldataMapper;
	private final JournaldataService journaldataService;

	public InnlesingRoute(JournaldataService journaldataService,
						  AvvikService avvikService) {
		super(avvikService);
		journaldataMapper = new JournaldataMapper();
		this.journaldataService = journaldataService;
	}

	@Override
	public void configure() throws Exception {
		//@formatter:off

		from(RUTE_INNLESING)
				.pollEnrich(LES_FRA_FILOMRAADE_URI, Duration.ofMinutes(2).toMillis())
				.routeId("innlesing")
				.log(INFO, log, "Starter lesing av ${file:absolute.path}.")
				.setProperty(PROPERTY_FILNAVN, simple("${file:name}"))
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(Innlasting.class)))
				.setBody(simple("${body.journaldataList}"))
				.log(INFO, log, "Starter lagring av ${body.size()} journaldata-elementer")
				.split(body(), new JournalpostAggregator()).streaming().parallelProcessing()
					.to(RUTE_MAP_JOURNALDATA)
				.end()
				.split(body()).streaming().parallelProcessing()
					.to(RUTE_LAGRE_JOURNALDATA)
				.end()
				.log(INFO, log, "Ferdig med lagring av ${body.size()} journaldata-elementer")
		.end();

		from(RUTE_MAP_JOURNALDATA)
				.routeId("map_journaldata")
				.bean(journaldataMapper);

		from(RUTE_LAGRE_JOURNALDATA)
				.routeId("lagre_journaldata")
				.process(exchange-> {
					var journaldata = exchange.getIn().getBody(Journaldata.class);
					try {
						journaldataService.lagreJournaldata(journaldata);
					} catch (DataIntegrityViolationException e) {
						log.error("Kunne ikke lagre journaldata med duplikat onDemandId={}. Feilmelding={}", journaldata.getOnDemandId(), e.getMessage(), e);
					}
    			})
				.end();

		//@formatter:on
	}

	private static class JournalpostAggregator implements AggregationStrategy {

		private final List<Journaldata> journalposter = new ArrayList<>();
		private final AtomicInteger counter = new AtomicInteger(0);

		@Override
		public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
			journalposter.add((Journaldata) newExchange.getIn().getBody());
			var count = counter.getAndIncrement();

			if (count % JOURNALPOST_LOGGBLOKK_STOERRELSE == 0)
				logAggregerteJournalposter(journalposter);

			return newExchange;
		}

		@Override
		public void onCompletion(Exchange exchange) {
			var antall = journalposter.size();
			var resterende = antall % JOURNALPOST_LOGGBLOKK_STOERRELSE;

			if (antall < JOURNALPOST_LOGGBLOKK_STOERRELSE )
				logAggregerteJournalposter(journalposter);
			else if (resterende != 0)
				logAggregerteJournalposter(journalposter.subList(antall - resterende, antall));

			exchange.getIn().setBody(journalposter);
		}

		private void logAggregerteJournalposter(List<Journaldata> journalposter) {
			var loggTil = journalposter.size();
			var loggFra = loggTil - min(loggTil, JOURNALPOST_LOGGBLOKK_STOERRELSE);

			var liste = journalposter.subList(loggFra, loggTil).stream()
					.map(Journaldata::getOnDemandId)
					.collect(Collectors.joining(", "));

			log.info("Har aggregert {}", liste);
		}
	}

}