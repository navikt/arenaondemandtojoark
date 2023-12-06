package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.INNLEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=innlesing", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
class InnlesingRouteITest extends AbstractIt {

	private static final String ONDEMAND_ID_1 = "ODAP08031000123";
	private static final String ONDEMAND_ID_2 = "ODAP08031000456";
	private static final String ONDEMAND_ID_3 = "ODAP08031000789";

	private static final String JOURNALPOST_ID = "467010363";

	@Autowired
	private JournaldataRepository journaldataRepository;

	@Test
	void skalLeseFilMedFlereElementerFraFilomraadeOgLagre() throws IOException {

		stubHentOndemandDokument(ONDEMAND_ID_1);
		stubHentOndemandDokument(ONDEMAND_ID_2);
		stubHentOndemandDokument(ONDEMAND_ID_3);
		stubOpprettJournalpost();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		copyFileFromClasspathToInngaaende("journaldata.xml", sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();

			assertThat(result)
					.hasSize(3)
					.extracting("onDemandId", "status")
					.containsExactlyInAnyOrder(
							tuple(ONDEMAND_ID_1, INNLEST),
							tuple(ONDEMAND_ID_2, INNLEST),
							tuple(ONDEMAND_ID_3, INNLEST)
					);
		});
	}
}