package no.nav.arenaondemandtojoark.consumer.dokarkiv.map;

import no.nav.arenaondemandtojoark.exception.JournalpostdataMappingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldata;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OpprettJournalpostRequestMapperTest {

	private static final String TSS_ID = "80123456789";
	private static final byte[] DOCUMENT = "document".getBytes();


	@Test
	void skalMappeManglendeUtsendingskanalTilSForJournalposttypeU() {
		var journaldata = lagJournaldata();
		journaldata.setUtsendingskanal(null);

		var result = OpprettJournalpostRequestMapper.map(journaldata, DOCUMENT);

		assertEquals("S", result.getKanal());
	}

	@Test
	void skalMappeUtsendingskanal() {
		var journaldata = lagJournaldata();

		var result = OpprettJournalpostRequestMapper.map(journaldata, DOCUMENT);

		assertEquals("L", result.getKanal());
	}

	@ParameterizedTest
	@ValueSource(strings = {TSS_ID, "12345678", "1234567890", "123456789012"})
	void skalKasteExceptionVedUgyldigBrukerId(String brukerId) {
		var journaldata = lagJournaldata();
		journaldata.setBrukerId(brukerId);

		assertThatExceptionOfType(JournalpostdataMappingException.class)
				.isThrownBy(() -> OpprettJournalpostRequestMapper.map(journaldata, DOCUMENT))
				.withMessage("Kan ikke mappe journaldata med ondemandId=%s. BrukerId=%s er ikke et gyldig FNR eller ORGNR, eller det er en TSS-id (starter med 80)"
						.formatted(journaldata.getOnDemandId(), brukerId));
	}

}