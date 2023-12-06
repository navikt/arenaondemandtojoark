package no.nav.arenaondemandtojoark;

import jakarta.xml.bind.JAXBContext;
import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.xml.rapport.Journalpostrapport;
import no.nav.arenaondemandtojoark.domain.xml.rapport.JournalpostrapportElement;
import no.nav.arenaondemandtojoark.domain.xml.rapport.map.JournalpostrapportMapper;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class RapporteringRoute extends BaseRoute {

	private static final String JOURNALPOSTRAPPORT_URI = "{{arenaondemandtojoark.endpointuri}}/journalpostrapporter" +
														 "?{{arenaondemandtojoark.endpointconfig}}" +
														 "&fileName=${exchangeProperty.filnavn}";

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

		from("direct:lag_rapport")
				.routeId("rapportering")
				.log(INFO, log, "Starter generering av rapport for fil=${exchangeProperty.filnavn}")
				.setBody(simple("${exchangeProperty.filnavn}"))
				.bean(journaldataService, "lagJournalpostrapport")
				.split(body(), new RapportAggregator()).streaming().parallelProcessing()
				    .bean(JournalpostrapportMapper.class)
				.end()
				.marshal(new JaxbDataFormat(JAXBContext.newInstance(Journalpostrapport.class)))
				.to(JOURNALPOSTRAPPORT_URI)
				.setBody(simple("${exchangeProperty.filnavn}"))
				.bean(journaldataService, "oppdaterStatusTilAvlevert")
				.end();

		//@formatter:on
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