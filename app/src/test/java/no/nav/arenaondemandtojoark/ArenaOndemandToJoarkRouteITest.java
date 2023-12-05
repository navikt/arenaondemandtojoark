package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpStatus.CONFLICT;

public class ArenaOndemandToJoarkRouteITest extends AbstractIt {

	@Autowired
	private Path sshdPath;

	@Autowired
	private JournaldataRepository journaldataRepository;

	@BeforeEach
	void beforeEach() throws IOException {
		preparePath(sshdPath);
	}

	@Test
	void skalLeseFilMedFlereElementerFraFilomraadeOgLagre() throws IOException {
		var ondemandId = "ODAP08031000123";
		var ondemandId2 = "ODAP08031000456";
		var ondemandId3 = "ODAP08031000789";
		var journalpostId = "467010363";

		stubHentOndemandDokument(ondemandId);
		stubHentOndemandDokument(ondemandId2);
		stubHentOndemandDokument(ondemandId3);
		stubOpprettJournalpost();
		stubFerdigstillJournalpost(journalpostId);

		copyFileFromClasspathToInngaaende("journaldata.xml", sshdPath);

		await().atMost(5, SECONDS).untilAsserted(() -> {
			var result = new ArrayList<String>();
			journaldataRepository.findAll().forEach(el -> result.add(el.getOnDemandId()));
			assertThat(result).hasSameElementsAs(List.of(ondemandId, ondemandId2, ondemandId3));
		});
	}

	@Test
	void skalHandtere409FraDokarkiv() throws IOException {
		var ondemandId = "ODAP08031000123";
		var journalpostId = "467010363";

		stubHentOndemandDokument(ondemandId);
		stubOpprettJournalpostMedStatus(CONFLICT);
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
