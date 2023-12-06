package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.repository.AvvikRepository;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitetMedStatusInnlest;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=prosessering", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
@Transactional
public class AvvikITest extends AbstractIt {

	private static final String ONDEMAND_ID_1 = "ODAP08031000123";
	private static final String ONDEMAND_ID_2 = "ODAP08031000456";
	private static final String ONDEMAND_ID_3 = "ODAP08031000789";

	private static final String JOURNALPOST_ID = "467010363";

	private static final List<String> ONDEMAND_IDER = List.of(ONDEMAND_ID_1, ONDEMAND_ID_2, ONDEMAND_ID_3);

	@Autowired
	private JournaldataRepository journaldataRepository;

	@Autowired
	private AvvikRepository avvikRepository;

	@Value("${arenaondemandtojoark.filnavn}")
	String filnavn;

	@BeforeEach
	void beforeEach() {
		System.out.println(sshdPath.toString());

		journaldataRepository.saveAll(List.of(
				lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_1, filnavn),
				lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_2, filnavn),
				lagJournaldataentitetMedStatusInnlest(ONDEMAND_ID_3, filnavn)
		));

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	@AfterEach
	void afterEach() {
		journaldataRepository.deleteAll();
		avvikRepository.deleteAll();
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

	private Tuple[] getOndemandIdAndFeiltype(String feiltype) {
		return ONDEMAND_IDER.stream()
				.map(ondemandId -> tuple(ondemandId, feiltype))
				.toArray(Tuple[]::new);
	}

	@Test
	void skalLagreAvvikVedNonRetryableFeilFraOnDemandBrev() throws IOException {
		stubHentOndemandDokumentMedStatus(HttpStatus.BAD_REQUEST);

		copyFileFromClasspathToInngaaende(filnavn, sshdPath);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			var avvik = avvikRepository.findAll();
			assertThat(avvik)
					.hasSize(3)
					.extracting("ondemandId", "feiltype")
					.containsExactlyInAnyOrder(
							tuple(ONDEMAND_ID_1, "NonRetryable"),
							tuple(ONDEMAND_ID_2, "NonRetryable"),
							tuple(ONDEMAND_ID_3, "NonRetryable")
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
