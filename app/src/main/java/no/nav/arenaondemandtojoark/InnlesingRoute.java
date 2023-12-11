package no.nav.arenaondemandtojoark;

import jakarta.xml.bind.JAXBContext;
import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.db.map.JournaldataMapper;
import no.nav.arenaondemandtojoark.domain.xml.Innlasting;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_FILNAVN;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.RUTE_INNLESING;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class InnlesingRoute extends BaseRoute {

	public static final String LES_FRA_FILOMRAADE_URI = "{{arenaondemandtojoark.sftp.uri}}" + "/inbound" +
														"{{arenaondemandtojoark.sftp.config}}" +
														"&antInclude=*.xml" +
														"&charset=UTF-8";

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
				.pollEnrich(LES_FRA_FILOMRAADE_URI)
				.routeId("innlesing")
				.log(INFO, log, "Starter lesing av ${file:absolute.path}.")
				.setProperty(PROPERTY_FILNAVN, simple("${file:name}"))
				.unmarshal(new JaxbDataFormat(JAXBContext.newInstance(Innlasting.class)))
				.setBody(simple("${body.journaldataList}")) // List<xml.Journaldata>
				.split(body(), new JournalpostAggregator()).streaming().parallelProcessing() //map alle journaldata-elementa til db-entitetar, og valider påkrevde felt
					.to(RUTE_MAP_JOURNALDATA)
				.end()
				.to(RUTE_LAGRE_JOURNALDATA)
				.log(INFO, log,"Ferdig med lagring av journaldata i bulk")
		.end();

		from(RUTE_MAP_JOURNALDATA)
				.routeId("map_journaldata")
				.bean(journaldataMapper);

		from(RUTE_LAGRE_JOURNALDATA)
				.routeId("lagre_journaldata_i_bulk")
				.bean(journaldataService, "lagreJournaldata");

		//@formatter:on
	}

	private static class JournalpostAggregator implements AggregationStrategy {

		private final List<Journaldata> journalposter = new ArrayList<>();

		@Override
		public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
			journalposter.add((Journaldata) newExchange.getIn().getBody());
			log.info("Har aggregert {}", ((Journaldata) newExchange.getIn().getBody()).getOnDemandId());

			return newExchange;
		}

		@Override
		public void onCompletion(Exchange exchange) {
			exchange.getIn().setBody(journalposter);
		}
	}

}