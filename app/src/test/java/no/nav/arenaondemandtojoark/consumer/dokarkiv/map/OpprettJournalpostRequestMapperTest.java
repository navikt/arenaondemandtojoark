package no.nav.arenaondemandtojoark.consumer.dokarkiv.map;

import org.junit.jupiter.api.Test;

import static no.nav.arenaondemandtojoark.TestUtils.lagJournaldata;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OpprettJournalpostRequestMapperTest {

	@Test
	void skalMappeManglendeUtsendingskanalTilSForJournalposttypeU() {
		var journaldata = lagJournaldata();
		journaldata.setUtsendingskanal(null);

		var result = OpprettJournalpostRequestMapper.map(journaldata, "document".getBytes());

		assertEquals("S", result.getKanal());
	}

	@Test
	void skalMappeUtsendingskanal() {
		var journaldata = lagJournaldata();

		var result = OpprettJournalpostRequestMapper.map(journaldata, "document".getBytes());

		assertEquals("L", result.getKanal());
	}

}