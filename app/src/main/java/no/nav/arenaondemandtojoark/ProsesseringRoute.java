package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.validate.JournaldataValidator;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_ONDEMAND_ID;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.RUTE_PROSESSERING;
import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
@Component
public class ProsesseringRoute extends RouteBuilder {

	private final JournaldataService journaldataService;
	private final JournaldataValidator journaldataValidator;
	private final ArenaOndemandToJoarkService arenaOndemandToJoarkService;

	public ProsesseringRoute(JournaldataService journaldataService,
							 ArenaOndemandToJoarkService arenaOndemandToJoarkService) {
		this.journaldataService = journaldataService;
		journaldataValidator = new JournaldataValidator();
		this.arenaOndemandToJoarkService = arenaOndemandToJoarkService;
	}

	@Override
	public void configure() {
		//@formatter:off

		from(RUTE_PROSESSERING)
			.log(INFO, "Inni rute-prosessering")
			.setBody(simple("${exchangeProperty.filnavn}"))
			.log(INFO, "${body}")
			.bean(journaldataService, "hentJournaldata")
			.split(body()).streaming().parallelProcessing()
				.setProperty(PROPERTY_ONDEMAND_ID, simple("${body.onDemandId}"))
				.to("direct:behandle_journaldata")
			.end();

			from("direct:behandle_journaldata")
			.routeId("behandle_journaldata")
			.bean(journaldataValidator)
			.bean(arenaOndemandToJoarkService)
			.end();

		//@formatter:on
	}

}