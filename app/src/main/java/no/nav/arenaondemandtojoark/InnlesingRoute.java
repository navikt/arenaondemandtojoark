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

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.RUTE_INNLESING;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class InnlesingRoute extends BaseRoute {

	public static final String PROPERTY_FILNAVN = "Filnavn";
	public static final String LES_FRA_FILOMRAADE_URI = "{{arenaondemandtojoark.sftp.uri}}" + "/inbound" +
														"{{arenaondemandtojoark.sftp.config}}" +
														"&antInclude=*.xml";
														//"&move=processed/${date:now:yyyyMMdd}/${file:name}";

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
					.to("direct:map_journaldata")
				.end()
				.to("direct:lagre_journaldata_i_bulk")
				.log(INFO, "Ferdig med lagring av journaldata i bulk")
				.end();

		from("direct:map_journaldata")
				.routeId("map_journaldata")
				.bean(journaldataMapper)
				.end();

		from("direct:lagre_journaldata_i_bulk")
				.routeId("lagre_journaldata_i_bulk")
				.bean(journaldataService, "lagreJournaldata")
				.end();

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