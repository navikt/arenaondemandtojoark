package no.nav.arenaondemandtojoark.domain.db.validate;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.exception.JournaldataValideringException;
import org.junit.jupiter.api.Test;

import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldata;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataMedJournalposttypeU;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;

class JournaldataValidatorTest {

	JournaldataValidator validator = new JournaldataValidator();

	@Test
	void skalValidereOk() {
		var journaldata = lagJournaldata();

		assertThatNoException().isThrownBy(() -> validator.validate(journaldata));
	}

	@Test
	void skalKasteJournaldataValideringExceptionHvisAlleDataMangler() {
		var journaldata = Journaldata.builder().build();

		assertThatExceptionOfType(JournaldataValideringException.class)
				.isThrownBy(() -> validator.validate(journaldata))
				.withMessageContainingAll(
						"Journaldata med ondemandId=null feilet validering med feilmeldinger:",
						"Journaldata mangler påkrevd felt=onDemandId",
						"Journaldata mangler påkrevd felt=brukerId",
						"Journaldata mangler påkrevd felt=brukertype",
						"Journaldata mangler påkrevd felt=journalposttype",
						"Journaldata mangler påkrevd felt=fagomraade",
						"Journaldata mangler påkrevd felt=journaldato",
						"Journaldata mangler påkrevd felt=innhold",
						"Journaldata mangler påkrevd felt=journalfoerendeEnhet",
						"Journaldata mangler påkrevd felt=opprettetAvNavn",
						"Journaldata mangler påkrevd felt=brevkode"
				);
	}

	@Test
	void skalKasteJournaldataValideringExceptionForJournalposttypeU() {
		var journaldata = lagJournaldataMedJournalposttypeU();

		assertThatExceptionOfType(JournaldataValideringException.class)
				.isThrownBy(() -> validator.validate(journaldata))
				.withMessageContainingAll(
						"Journaldata med ondemandId=ODAP08031000123 feilet validering med feilmeldinger:",
						"Journaldata mangler påkrevd felt=mottakernavn",
						"Journaldata mangler påkrevd felt=mottakerId"
				);
	}

}