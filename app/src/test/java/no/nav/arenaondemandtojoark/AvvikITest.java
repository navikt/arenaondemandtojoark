package no.nav.arenaondemandtojoark;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.JOURNALPOST_ID;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_1;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_2;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_3;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitetMedStatusInnlest;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=prosessering", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
public class AvvikITest extends AbstractIt {

	private static final List<String> ONDEMAND_IDER = List.of(ONDEMAND_ID_1, ONDEMAND_ID_2, ONDEMAND_ID_3);

	@Value("${arenaondemandtojoark.filnavn}")
	String filnavn;

	@BeforeEach
	void beforeEach() {
		journaldataRepository.saveAll(List.of(
				lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn),
				lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_2, filnavn),
				lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_3, filnavn)
		));

		commitAndBeginNewTransaction();
	}

	@AfterEach
	void afterEach() {
		journaldataRepository.deleteAll();
		avvikRepository.deleteAll();

		commitAndBeginNewTransaction();
	}

	@Test
	void skalLagreAvvikVedRetryableFeilFraOnDemandBrev() throws IOException {
		stubHentOndemandDokumentMedStatus(INTERNAL_SERVER_ERROR);

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

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
		stubHentOndemandDokumentMedStatus(BAD_REQUEST);

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var avvik = avvikRepository.findAll();
			assertThat(avvik)
					.hasSize(3)
					.extracting("ondemandId", "retryable")
					.containsExactlyInAnyOrder(
							tuple(ONDEMAND_ID_1, false),
							tuple(ONDEMAND_ID_2, false),
							tuple(ONDEMAND_ID_3, false)
					);

			var journaldata = journaldataRepository.findAll();
			assertThat(journaldata)
					.hasSize(3)
					.extracting("onDemandId", "status")
					.containsExactlyInAnyOrder(
							tuple(ONDEMAND_ID_1, AVVIK),
							tuple(ONDEMAND_ID_2, AVVIK),
							tuple(ONDEMAND_ID_3, AVVIK)
					);
		});
	}

	@Test
	void skalIkkeLagreAvvikVedNotFoundFraOndemandBrev() throws IOException {
		stubHentOndemandDokumentMedStatus(NOT_FOUND);

		stubOpprettJournalpost();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var avvik = avvikRepository.findAll();
			assertThat(avvik).hasSize(0);

			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(3)
					.extracting("onDemandId", "status")
					.containsExactlyInAnyOrder(
							tuple(ONDEMAND_ID_1, PROSESSERT),
							tuple(ONDEMAND_ID_2, PROSESSERT),
							tuple(ONDEMAND_ID_3, PROSESSERT)
					);


		});
	}
}
