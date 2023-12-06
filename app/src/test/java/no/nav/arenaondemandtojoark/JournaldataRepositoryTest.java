package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.db.JournaldataStatus;
import no.nav.arenaondemandtojoark.domain.db.projections.Rapportelement;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitet;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataentitetMedStatusInnlest;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVLEVERT;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.INNLEST;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.PROSESSERT;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("itest")
class JournaldataRepositoryTest {

	private static final String RELEVANT_FILNAVN = "relevantFilnavn.xml";
	private static final String IRRELEVANT_FILNAVN = "irrelevantFilnavn.xml";
	private static final String FEIL_FILNAVN = "feilfilnavn.xml";

	@Autowired
	JournaldataRepository journaldataRepository;

	@BeforeEach
	public void setup() {
		journaldataRepository.saveAll(List.of(
				lagJournaldataentitetMedStatusInnlest("ODAP08031000123", RELEVANT_FILNAVN),
				lagJournaldataentitetMedStatusInnlest("ODAP08031000234", RELEVANT_FILNAVN),
				lagJournaldataentitetMedStatusInnlest("ODAP08031000345", IRRELEVANT_FILNAVN),
				lagJournaldataentitet(RELEVANT_FILNAVN, PROSESSERT, "ODAP08031000456", "1234", "123"),
				lagJournaldataentitet(RELEVANT_FILNAVN, PROSESSERT, "ODAP08031000567", "2345", "234")
		));
	}

	@AfterEach
	public void cleanup() {
		journaldataRepository.deleteAll();
	}

	@ParameterizedTest
	@MethodSource
	void skalHenteJournaldata(List<String> forventet, JournaldataStatus status) {

		var journaldata = journaldataRepository.getAllByFilnavnAndStatus(RELEVANT_FILNAVN, status);
		var faktisk = journaldata.stream().map(Journaldata::getOnDemandId).toList();

		assertThat(faktisk).containsExactlyInAnyOrderElementsOf(forventet);
	}

	private static Stream<Arguments> skalHenteJournaldata() {
		return Stream.of(
				Arguments.of(List.of("ODAP08031000123", "ODAP08031000234"), INNLEST),
				Arguments.of(List.of("ODAP08031000456", "ODAP08031000567"), PROSESSERT)
		);
	}

	@ParameterizedTest
	@EnumSource(value = JournaldataStatus.class, names = {"INNLEST", "PROSESSERT"})
	void skalReturnereTomJournaldatalisteForUbruktFilnavn(JournaldataStatus status) {

		var journaldataliste = journaldataRepository.getAllByFilnavnAndStatus(FEIL_FILNAVN, status);

		assertThat(journaldataliste).isEmpty();
	}

	@Test
	void skalHenteRapportelementliste() {

		var forventet = List.of(
				new Rapportelement("ODAP08031000456", "1234", "123"),
				new Rapportelement("ODAP08031000567", "2345", "234")
		);

		var rapportdata = journaldataRepository.getRapportdataByFilnavnAndStatus(RELEVANT_FILNAVN, PROSESSERT);

		assertThat(rapportdata.stream().toList()).containsExactlyInAnyOrderElementsOf(forventet);
	}

	@Test
	void skalReturnereTomRapportelementlisteForUbruktFilnavn() {

		var rapportdata = journaldataRepository.getRapportdataByFilnavnAndStatus(FEIL_FILNAVN, PROSESSERT);

		assertThat(rapportdata).isEmpty();
	}

	@Test
	void skalOppdatereStatusTilAvlevert() {
		journaldataRepository.updateStatusToAvlevert(RELEVANT_FILNAVN);

		commitAndBeginNewTransaction();

		var journaldata = journaldataRepository.findAll();

		assertThat(journaldata)
				.filteredOn(j -> j.getStatus().equals(AVLEVERT))
				.hasSize(2)
				.extracting("onDemandId")
				.containsExactlyInAnyOrder("ODAP08031000456", "ODAP08031000567");
	}

	private static void commitAndBeginNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}
}