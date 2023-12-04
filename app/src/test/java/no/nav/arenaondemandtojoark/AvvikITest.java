package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.repository.AvvikRepository;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class AvvikITest extends AbstractIt{

	private static final List<String> ONDEMAND_IDER = List.of(
			"ODAP08031000123",
			"ODAP08031000456",
			"ODAP08031000789"
	);

	@Autowired
	private Path sshdPath;

	@Autowired
	private JournaldataRepository journaldataRepository;

	@Autowired
	private AvvikRepository avvikRepository;

	@Test
	void skalLagreAvvikVedRetryableFeilFraOnDemandBrev() throws IOException {
		stubHentOndemandDokumentMedStatus(INTERNAL_SERVER_ERROR);

		copyFileFromClasspathToInngaaende("journaldata.xml", sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var avvik = avvikRepository.findAll();
			assertThat(avvik)
					.hasSize(3)
					.extracting("ondemandId")
					.hasSameElementsAs(ONDEMAND_IDER);
		});
	}

	@Test
	void skalLagreAvvikVedNonRetryableFeilFraOnDemandBrev() throws IOException {
		stubHentOndemandDokumentMedStatus(HttpStatus.BAD_REQUEST);

		copyFileFromClasspathToInngaaende("journaldata.xml", sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var avvik = avvikRepository.findAll();
			assertThat(avvik)
					.hasSize(3)
					.extracting("ondemandId", "feiltype")
					.containsExactlyInAnyOrder(
							tuple("ODAP08031000123", "NonRetryable"),
							tuple("ODAP08031000456", "NonRetryable"),
							tuple("ODAP08031000789", "NonRetryable")
					);

			var journaldata = journaldataRepository.findAll();
			assertThat(journaldata)
					.hasSize(3)
					.extracting("onDemandId", "status")
					.containsExactlyInAnyOrder(
							tuple("ODAP08031000123", AVVIK),
							tuple("ODAP08031000456", AVVIK),
							tuple("ODAP08031000789", AVVIK)
					);
		});
	}

	@Test
	void skalIkkeLagreAvvikVedNotFoundFraOndemandBrev() throws IOException {
		stubHentOndemandDokumentMedStatus(NOT_FOUND);

		stubOpprettJournalpost();
		stubFerdigstillJournalpost("467010363");

		copyFileFromClasspathToInngaaende("journaldata-ett-element.xml", sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsExactly(tuple("ODAP08031000123", PROSESSERT));
		});
	}
}
