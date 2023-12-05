package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitetMedStatusInnlest;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=prosessering", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
class ProsesseringRouteITest extends AbstractIt {

	@Autowired
	private Path sshdPath;

	@Autowired
	private JournaldataRepository journaldataRepository;

	@BeforeEach
	void beforeEach() throws IOException {
		preparePath(sshdPath);

		var RELEVANT_FILNAVN = "journaldata.xml";
		journaldataRepository.saveAll(List.of(
				lagJournaldataentitetMedStatusInnlest("ODAP08031000123", RELEVANT_FILNAVN),
				lagJournaldataentitetMedStatusInnlest("ODAP08031000234", RELEVANT_FILNAVN)
		));
	}

	@AfterEach
	public void cleanup() {
		journaldataRepository.deleteAll();
	}

	@Test
	void skalProsessereJournaldata() {
		stubHentOndemandDokument("ODAP08031000123");
		stubHentOndemandDokument("ODAP08031000234");
		stubOpprettJournalpost();
		stubFerdigstillJournalpost("467010363");

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var journaldata = journaldataRepository.findAll();
			assertThat(journaldata)
					.hasSize(2)
					.extracting("status")
					.containsOnly(PROSESSERT);
		});
	}
}