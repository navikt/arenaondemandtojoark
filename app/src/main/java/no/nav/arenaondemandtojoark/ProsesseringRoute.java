package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.validate.JournaldataValidator;
import org.springframework.stereotype.Component;

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_ONDEMAND_ID;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.RUTE_PROSESSERING;

@Slf4j
@Component
public class ProsesseringRoute extends BaseRoute {

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
			.split(body()).streaming().parallelProcessing()
				.setProperty(PROPERTY_ONDEMAND_ID, simple("${body.onDemandId}"))
				.to(RUTE_BEHANDLE_JOURNALDATA)
			.end();

			from(RUTE_BEHANDLE_JOURNALDATA)
			.routeId("behandle_journaldata")
			.bean(journaldataValidator)
			.bean(arenaOndemandToJoarkService)
			.end();

		//@formatter:on
	}

}