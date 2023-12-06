package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

public class ArenaOndemandToJoarkRouteITest extends AbstractIt {

	@Autowired
	private JournaldataRepository journaldataRepository;

	@Test
	void skalHandtere409FraDokarkiv() throws IOException {
		var ondemandId = "ODAP08031000123";
		var journalpostId = "467010363";

		stubHentOndemandDokument(ondemandId);
		stubOpprettJournalpostMedStatusConflict();
		stubFerdigstillJournalpost(journalpostId);

		copyFileFromClasspathToInngaaende("journaldata-ett-element.xml", sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsExactly(tuple(ondemandId, PROSESSERT));
		});
	}

}
