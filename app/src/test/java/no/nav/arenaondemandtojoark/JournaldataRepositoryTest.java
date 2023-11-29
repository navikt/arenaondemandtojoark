package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitet;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("itest")
class JournaldataRepositoryTest {

	@Autowired
	JournaldataRepository journaldataRepository;

	@BeforeEach
	public void setup() {
		var relevantFilnavn = "relevant.xml";
		var irrelevantFilnavn = "irrelevant.xml";
		var journaldata1 = lagJournaldataentitet("ODAP08031000123", relevantFilnavn);
		var journaldata2 = lagJournaldataentitet("ODAP08031000234", relevantFilnavn);
		var journaldata3 = lagJournaldataentitet("ODAP08031000345", irrelevantFilnavn);

		journaldataRepository.saveAll(List.of(journaldata1, journaldata2, journaldata3));
	}

	@AfterEach
	public void cleanup() {
		journaldataRepository.deleteAll();
	}

	@Test
	void skalHenteJournaldataForGittFilnavn() {
		String filnavn = "relevant.xml";
		var forventedeOndemandIder = List.of("ODAP08031000234", "ODAP08031000123");

		var result = journaldataRepository.getAllByFilnavn(filnavn);
		var faktiskeOndemandIder = result.stream().map(Journaldata::getOnDemandId).toList();

		assertThat(faktiskeOndemandIder).hasSameElementsAs(forventedeOndemandIder);
	}

	@Test
	void skalIkkeHenteJournaldataMedFeilFilnavn() {
		String filnavn = "feilfilnavn.xml";

		var result = journaldataRepository.getAllByFilnavn(filnavn);
		var faktiskeOndemandIder = result.stream().map(Journaldata::getOnDemandId).toList();

		assertThat(faktiskeOndemandIder).isEmpty();
	}

}