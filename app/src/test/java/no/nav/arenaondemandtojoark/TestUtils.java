package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.journaldata.Journaldata;

import java.time.LocalDateTime;

import static no.nav.arenaondemandtojoark.domain.journaldata.Dokumentkategori.B;
import static no.nav.arenaondemandtojoark.domain.journaldata.Fagomraade.OPP;
import static no.nav.arenaondemandtojoark.domain.journaldata.JournalpostType.U;
import static no.nav.arenaondemandtojoark.domain.journaldata.Utsendingskanal.L;

public class TestUtils {

	public static Journaldata lagJournaldata() {
		return Journaldata.builder()
				.onDemandId("ODAP08031000123")
				.saksnummer("3133123")
				.brukerId("11114928123")
				.brukerType("PERSON")
				.journalpostType(U)
				.fagomraade(OPP)
				.journaldato(LocalDateTime.parse("2008-03-10T17:19:22"))
				.innhold("Innk. til dialogmøte innen 26 uker der mottaker er arb.giver")
				.mottakerNavn("DUCK DONALD")
				.mottakerId("80000009123")
				.utsendingskanal(L)
				.journalfoerendeEnhet("0211")
				.sendtPrintDato(LocalDateTime.parse("2011-12-08T15:07:48"))
				.opprettetAvNavn("Tryll, Magika Von")
				.dokumentkategori(B)
				.brevkode("brevkode1")
				.build();
	}

	public static Journaldata lagJournaldataMedJournalposttypeU() {
		return Journaldata.builder()
				.onDemandId("ODAP08031000123")
				.saksnummer("3133123")
				.brukerId("11114928123")
				.brukerType("PERSON")
				.journalpostType(U)
				.fagomraade(OPP)
				.journaldato(LocalDateTime.parse("2008-03-10T17:19:22"))
				.innhold("Innk. til dialogmøte innen 26 uker der mottaker er arb.giver")
				.journalfoerendeEnhet("0211")
				.opprettetAvNavn("Tryll, Magika Von")
				.dokumentkategori(B)
				.brevkode("brevkode1")
				.build();
	}

}
