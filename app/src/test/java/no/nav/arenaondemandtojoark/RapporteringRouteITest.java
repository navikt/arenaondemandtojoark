package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.db.JournaldataStatus;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitet;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
		properties = {"arenaondemandtojoark.operasjon=rapportering", "arenaondemandtojoark.filnavn=journaldata.xml"}
)
@Transactional
public class RapporteringRouteITest extends AbstractIt {

	private static final Journaldata journaldata1 = lagJournaldataentitet("journaldata.xml", PROSESSERT, "ODAP08031000123", "1234", "123");
	private static final Journaldata journaldata2 = lagJournaldataentitet("journaldata.xml", PROSESSERT, "ODAP08031000456", "1234", "123");
	private static final Journaldata journaldata3 = lagJournaldataentitet("journaldata.xml", PROSESSERT, "ODAP08031000789", "1234", "123");

	private static final List<Journaldata> journaldataList = List.of(journaldata1, journaldata2, journaldata3);

	@Autowired
	private JournaldataRepository journaldataRepository;

	@Value("${arenaondemandtojoark.filnavn}")
	private String filnavn;

	@BeforeEach
	void beforeEach() {
		journaldataRepository.saveAll(journaldataList);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

	}

	@Test
	void skalRapportere() {

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
