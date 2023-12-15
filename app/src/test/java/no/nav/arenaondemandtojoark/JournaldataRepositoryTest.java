package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.db.Avvik;
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
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;
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
		commitAndBeginNewTransaction();
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

	@Test
	void skalHenteJournaldataMedInnlestEllerRetryableAvvik() {
		var journaldataliste = journaldataRepository.getAllByFilnavnAndStatus(RELEVANT_FILNAVN, INNLEST);
		var ondemandId1 = "ODAP08031000123";
		var ondemandId2 = "ODAP08031000234";
		var retryable = journaldataliste.stream().filter(j -> j.getOnDemandId().equals(ondemandId1)).findFirst().get();
		retryable.setStatus(AVVIK);
		retryable.setAvvik(new Avvik(
				retryable.getJournaldataId(),
				true,
				"En retryable exception har skjedd",
				retryable));

		var nonRetryable = journaldataliste.stream().filter(j -> j.getOnDemandId().equals(ondemandId2)).findFirst().get();
		nonRetryable.setStatus(AVVIK);
		nonRetryable.setAvvik(new Avvik(
				nonRetryable.getJournaldataId(),
				false,
				"En non-retryable exception har skjedd",
				nonRetryable));

		var ondemandId3 = "ODAP08031000555";
		var innlest = lagJournaldataentitetMedStatusInnlest(ondemandId3, RELEVANT_FILNAVN);
		journaldataRepository.saveAll(List.of(retryable, nonRetryable, innlest));
		commitAndBeginNewTransaction();

		var result = journaldataRepository.getAllByFilnavnAndStatuses(RELEVANT_FILNAVN, List.of(INNLEST, AVVIK));
		assertThat(result)
				.hasSize(2)
				.extracting("onDemandId")
				.containsExactlyInAnyOrder(ondemandId1, ondemandId3);
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