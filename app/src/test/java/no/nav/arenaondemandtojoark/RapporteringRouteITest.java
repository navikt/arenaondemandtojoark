package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.db.JournaldataStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.DOKUMENT_INFO_ID;
import static no.nav.arenaondemandtojoark.TestUtils.JOURNALPOST_ID;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_1;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_2;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_3;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitet;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=rapportering", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
public class RapporteringRouteITest extends AbstractIt {

	@Value("${arenaondemandtojoark.filnavn}")
	private String filnavn;

	@Test
	void skalRapportere() {

		journaldataRepository.saveAll(List.of(
				lagJournaldataentitet(filnavn, PROSESSERT, ONDEMAND_ID_1, JOURNALPOST_ID, DOKUMENT_INFO_ID),
				lagJournaldataentitet(filnavn, PROSESSERT, ONDEMAND_ID_2, JOURNALPOST_ID, DOKUMENT_INFO_ID),
				lagJournaldataentitet(filnavn, PROSESSERT, ONDEMAND_ID_3, JOURNALPOST_ID, DOKUMENT_INFO_ID)
		));

		commitAndBeginNewTransaction();

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(3)
					.extracting("status")
					.containsOnly(JournaldataStatus.AVLEVERT);

			assertThat(new File(sshdPath.toString() + "/journalpostrapporter/" + filnavn))
					.exists();
		});
	}
}
