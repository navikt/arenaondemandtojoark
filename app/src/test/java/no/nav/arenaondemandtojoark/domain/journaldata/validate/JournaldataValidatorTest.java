package no.nav.arenaondemandtojoark.domain.journaldata.validate;

import no.nav.arenaondemandtojoark.domain.journaldata.Journaldata;
import no.nav.arenaondemandtojoark.exception.JournaldataValideringException;
import org.junit.jupiter.api.Test;

import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldata;
import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldataMedJournalposttypeU;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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

		assertThatThrownBy(() -> validator.validate(journaldata))
				.isInstanceOf(JournaldataValideringException.class)
				.hasMessageContainingAll(
						"Journaldata med ondemandId=null feilet validering med feilmeldinger:",
						"Journaldata mangler påkrevd felt=onDemandId",
						"Journaldata mangler påkrevd felt=brukerId",
						"Journaldata mangler påkrevd felt=brukerType",
						"Journaldata mangler påkrevd felt=journalpostType",
						"Journaldata mangler påkrevd felt=tema",
						"Journaldata mangler påkrevd felt=datoJournal",
						"Journaldata mangler påkrevd felt=innhold",
						"Journaldata mangler påkrevd felt=journalfEnhet",
						"Journaldata mangler påkrevd felt=opprettetAvNavn",
						"Journaldata mangler påkrevd felt=brevkode"
				);
	}

	@Test
	void skalKasteJournaldataValideringExceptionForJournalposttypeU() {
		var journaldata = lagJournaldataMedJournalposttypeU();

		assertThatThrownBy(() -> validator.validate(journaldata))
				.isInstanceOf(JournaldataValideringException.class)
				.hasMessageContainingAll(
						"Journaldata med ondemandId=ODAP08031000123 feilet validering med feilmeldinger:",
						"Journaldata mangler påkrevd felt=utsendingskanal",
						"Journaldata mangler påkrevd felt=mottakerNavn",
						"Journaldata mangler påkrevd felt=mottakerId",
						"Journaldata mangler påkrevd felt=sendtPrintDato"
				);
	}

}