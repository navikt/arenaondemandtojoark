package no.nav.arenaondemandtojoark;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.JOURNALPOST_ID;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_1;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_2;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_3;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitetMedStatusInnlest;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=prosessering", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
public class AvvikITest extends AbstractIt {

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

		commitAndBeginNewTransaction();
	}

	@ParameterizedTest
	@MethodSource
	void skalLagreAvvikVedFeilFraOnDemandBrev(HttpStatus httpStatus, boolean isRetryable) throws IOException {
		stubHentOndemandDokumentMedStatus(httpStatus);

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {

			var journaldataliste = journaldataRepository.findAll();

			assertThat(journaldataliste)
					.hasSize(3)
					.extracting("status")
					.containsOnly(AVVIK);

			assertThat(journaldataliste)
					.allSatisfy(journaldata -> {
						assertThat(journaldata.getAvvik()).isNotNull();
						assertThat(journaldata.getAvvik().isRetryable()).isEqualTo(isRetryable);
					});

		});
	}

	private static Stream<Arguments> skalLagreAvvikVedFeilFraOnDemandBrev() {
		return Stream.of(
				Arguments.of(INTERNAL_SERVER_ERROR, true),
				Arguments.of(BAD_REQUEST, false));
	}

	@Test
	void skalIkkeLagreAvvikVedNotFoundFraOndemandBrev() throws IOException {
		stubHentOndemandDokumentMedStatus(NOT_FOUND);

		stubOpprettJournalpost();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {

			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(3)
					.extracting("status")
					.containsOnly(PROSESSERT);
		});
	}
}
