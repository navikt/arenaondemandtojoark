package no.nav.arenaondemandtojoark;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.JOURNALPOST_ID;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_1;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_2;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_3;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.INNLEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=innlesing", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
class InnlesingRouteITest extends AbstractIt {

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