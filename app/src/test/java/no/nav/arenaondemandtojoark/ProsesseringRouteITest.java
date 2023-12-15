package no.nav.arenaondemandtojoark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.JOURNALPOST_ID;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_1;
import static no.nav.arenaondemandtojoark.TestUtils.ONDEMAND_ID_2;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitetMedStatusInnlest;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=prosessering", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
class ProsesseringRouteITest extends AbstractIt {

	@Value("${arenaondemandtojoark.filnavn}")
	String filnavn;

	@Test
	void skalProsessereJournaldata() {
		stubHentOndemandDokument(ONDEMAND_ID_1);
		stubHentOndemandDokument(ONDEMAND_ID_2);
		stubOpprettJournalpost();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		journaldataRepository.saveAll(List.of(
				lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn),
				lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_2, filnavn)
		));

		commitAndBeginNewTransaction();

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var journaldata = journaldataRepository.findAll();
			assertThat(journaldata)
					.hasSize(2)
					.extracting("status")
					.containsOnly(PROSESSERT);
		});
	}

	@Test
	void skalIkkeHaRetryVedBadRequestFraOndemand() throws IOException {
		stubHentOndemandDokumentMedStatusBadRequest(ONDEMAND_ID_1);
		stubOpprettJournalpost();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		journaldataRepository.save(lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn));

		commitAndBeginNewTransaction();

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsExactly(tuple(ONDEMAND_ID_1, AVVIK));
		});
		verify(1, getRequestedFor(urlEqualTo(format(HENT_ONDEMAND_DOKUMENT_URL, ONDEMAND_ID_1))));
	}

	@Test
	void skalHaRetryVedInternalServerErrorFraOndemand() throws IOException {
		stubHentDokumentMedStatusInternalServerErrorToGanger(ONDEMAND_ID_1);
		stubOpprettJournalpost();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		journaldataRepository.save(lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn));

		commitAndBeginNewTransaction();

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsExactly(tuple(ONDEMAND_ID_1, PROSESSERT));
		});
		verify(3, getRequestedFor(urlEqualTo(format(HENT_ONDEMAND_DOKUMENT_URL, ONDEMAND_ID_1))));
	}

	@Test
	void skalHandtereConflictFraOpprettJournalpost() throws IOException {
		stubHentOndemandDokument(ONDEMAND_ID_1);
		stubOpprettJournalpostMedStatusConflict();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		journaldataRepository.save(lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn));

		commitAndBeginNewTransaction();

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsExactly(tuple(ONDEMAND_ID_1, PROSESSERT));
		});
	}

	@Test
	void skalIkkeHaRetryVedBadRequestFraOpprettJournalpost() throws IOException {
		stubHentOndemandDokument(ONDEMAND_ID_1);
		stubOpprettJournalpostMedStatusBadRequest();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		journaldataRepository.save(lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn));

		commitAndBeginNewTransaction();

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsExactly(tuple(ONDEMAND_ID_1, AVVIK));
		});
		verify(1, postRequestedFor(urlEqualTo(OPPRETT_JOURNALPOST_URL)));
	}

	@Test
	void skalHaRetryVedInternalServerErrorFraOpprettJournalpost() throws IOException {
		stubHentOndemandDokument(ONDEMAND_ID_1);
		stubOpprettJournalpostMedStatusInternalServerErrorToGanger();
		stubFerdigstillJournalpost(JOURNALPOST_ID);

		journaldataRepository.save(lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn));

		commitAndBeginNewTransaction();

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsExactly(tuple(ONDEMAND_ID_1, PROSESSERT));
		});
		verify(3, postRequestedFor(urlEqualTo(OPPRETT_JOURNALPOST_URL)));
	}

	@Test
	void skalHaRetryVedInternalServerErrorFraFerdigstillJournalpost() throws IOException {
		stubHentOndemandDokument(ONDEMAND_ID_1);
		stubOpprettJournalpost();
		stubFerdigstillJournalpostMedStatusInternalServerErrorToGanger(JOURNALPOST_ID);

		journaldataRepository.save(lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn));

		commitAndBeginNewTransaction();

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var result = journaldataRepository.findAll();
			assertThat(result)
					.hasSize(1)
					.extracting("onDemandId", "status")
					.containsExactly(tuple(ONDEMAND_ID_1, PROSESSERT));
		});
		verify(3, patchRequestedFor(urlEqualTo(format(FERDIGSTILL_JOURNALPOST_URL, JOURNALPOST_ID))));
	}

}