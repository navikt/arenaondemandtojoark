package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import no.nav.arenaondemandtojoark.repository.Journalpostrapportelement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitetMedStatusInnlest;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitetMedStatusProsessert;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("itest")
class JournaldataRepositoryTest {

	private static final String RELEVANT_FILNAVN = "relevantFilnavn.xml";
	private static final String IRRELEVANT_FILNAVN = "irrelevantFilnavn.xml";
	private static final String FEIL_FILNAVN = "feilfilnavn.xml";
	private static final String STATUS_INNLEST = "INNLEST";
	private static final String STATUS_PROSESSERT = "PROSESSERT";
	private static final String STATUS_UGYLDIG = "UGYLDIG";

	@Autowired
	JournaldataRepository journaldataRepository;

	@BeforeEach
	public void setup() {
		journaldataRepository.saveAll(List.of(
				lagJournaldataentitetMedStatusInnlest("ODAP08031000123", RELEVANT_FILNAVN),
				lagJournaldataentitetMedStatusInnlest("ODAP08031000234", RELEVANT_FILNAVN),
				lagJournaldataentitetMedStatusInnlest("ODAP08031000345", IRRELEVANT_FILNAVN),
				lagJournaldataentitetMedStatusProsessert(RELEVANT_FILNAVN, "ODAP08031000456", "1234", "123"),
				lagJournaldataentitetMedStatusProsessert(RELEVANT_FILNAVN, "ODAP08031000567", "2345", "234")
		));
	}

	@AfterEach
	public void cleanup() {
		journaldataRepository.deleteAll();
	}

	@ParameterizedTest
	@MethodSource
	void skalHenteJournaldata(List<String> forventedeOndemandIder, String status) {

		var result = journaldataRepository.getAllByFilnavnAndStatus(RELEVANT_FILNAVN, status);
		var faktiskeOndemandIder = result.stream().map(Journaldata::getOnDemandId).toList();

		assertThat(faktiskeOndemandIder).containsExactlyInAnyOrderElementsOf(forventedeOndemandIder);
	}

	private static Stream<Arguments> skalHenteJournaldata() {
		return Stream.of(
				Arguments.of(List.of("ODAP08031000123", "ODAP08031000234"), STATUS_INNLEST),
				Arguments.of(List.of("ODAP08031000456", "ODAP08031000567"), STATUS_PROSESSERT)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {STATUS_INNLEST, STATUS_PROSESSERT})
	void skalReturnereTomJournaldatalisteForUbruktFilnavn(String status) {

		var result = journaldataRepository.getAllByFilnavnAndStatus(FEIL_FILNAVN, status);

		assertThat(result).isEmpty();
	}

	@Test
	void skalReturnereTomJournaldatalisteDersomStatusErUgyldig() {

		var result = journaldataRepository.getAllByFilnavnAndStatus(RELEVANT_FILNAVN, STATUS_UGYLDIG);

		assertThat(result).isEmpty();
	}

	@Test
	void skalHenteListeMedJournalpostrapportelementer() {

		var forventetJournaldatarapportelementer = List.of(
				new Journalpostrapportelement("ODAP08031000456", "1234", "123"),
				new Journalpostrapportelement("ODAP08031000567", "2345", "234")
		);

		var result = journaldataRepository.getRapportdataByFilnavnAndStatus(RELEVANT_FILNAVN, STATUS_PROSESSERT);

		assertThat(result.stream().toList()).containsExactlyInAnyOrderElementsOf(forventetJournaldatarapportelementer);
	}

	@Test
	void skalReturnereTomListeMedJournalpostrapportelementerForUbruktFilnavn() {

		var result = journaldataRepository.getRapportdataByFilnavnAndStatus(FEIL_FILNAVN, STATUS_PROSESSERT);

		assertThat(result).isEmpty();
	}

	@Test
	void skalReturnereTomListeMedJournalpostrapportelementerForUgyldigStatus() {

		var result = journaldataRepository.getRapportdataByFilnavnAndStatus(RELEVANT_FILNAVN, STATUS_UGYLDIG);

		assertThat(result).isEmpty();
	}
}