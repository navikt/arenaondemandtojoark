package no.nav.arenaondemandtojoark;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.JOURNALPOST_ID;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_1;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.INNLEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=innlesing", "arenaondemandtojoark.filnavn=journaldata-duplikat.xml"}
)
class InnlesingRouteDuplikatITest extends AbstractIt {

	@Test
	void skalIgnorereJournaldataMedDuplikatOnDemandId() throws IOException {

		stubHentOndemandDokument(ONDEMAND_ID_1);
		stubOpprettJournalpost();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		copyFileFromClasspathToInngaaende("journaldata-duplikat.xml", sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();

			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsOnly(tuple(ONDEMAND_ID_1, INNLEST));
		});
	}
}