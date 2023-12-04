package no.nav.arenaondemandtojoark;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;

import java.time.LocalDateTime;

import static no.nav.arenaondemandtojoark.domain.db.Dokumentkategori.B;
import static no.nav.arenaondemandtojoark.domain.db.Fagomraade.OPP;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.INNLEST;
import static no.nav.arenaondemandtojoark.domain.db.Journalposttype.U;
import static no.nav.arenaondemandtojoark.domain.db.Utsendingskanal.L;

public class TestUtils {

	public static no.nav.arenaondemandtojoark.domain.xml.Journaldata lagXmlJournaldata() {
		var journaldata = new no.nav.arenaondemandtojoark.domain.xml.Journaldata();

		journaldata.setOnDemandId("ODAP08031000123");
		journaldata.setSaksnummer("3133123");
		journaldata.setBrukerId("11114928123");
		journaldata.setBrukerType("PERSON");
		journaldata.setJournalpostType("U");
		journaldata.setFagomraade("OPP");
		journaldata.setJournaldato("2008-03-10T17:19:22");
		journaldata.setInnhold("Innk. til dialogmøte innen 26 uker der mottaker er arb.giver");
		journaldata.setMottakerNavn("DUCK DONALD");
		journaldata.setMottakerId("80000009123");
		journaldata.setUtsendingskanal("L");
		journaldata.setJournalfoerendeEnhet("0211");
		journaldata.setSendtPrintDato("2011-12-08T15:07:48");
		journaldata.setOpprettetAvNavn("Tryll, Magika Von");
		journaldata.setDokumentkategori("B");
		journaldata.setBrevkode("brevkode1");

		return journaldata;
	}

	public static no.nav.arenaondemandtojoark.domain.xml.Journaldata lagXmlJournaldataMedJournalposttypeU() {
		var journaldata = new no.nav.arenaondemandtojoark.domain.xml.Journaldata();

		journaldata.setOnDemandId("ODAP08031000123");
		journaldata.setSaksnummer("3133123");
		journaldata.setBrukerId("11114928123");
		journaldata.setBrukerType("PERSON");
		journaldata.setJournalpostType("U");
		journaldata.setFagomraade("OPP");
		journaldata.setJournaldato("2008-03-10T17:19:22");
		journaldata.setInnhold("Innk. til dialogmøte innen 26 uker der mottaker er arb.giver");
		journaldata.setJournalfoerendeEnhet("0211");
		journaldata.setOpprettetAvNavn("Tryll, Magika Von");
		journaldata.setDokumentkategori("B");
		journaldata.setBrevkode("brevkode1");

		return journaldata;
	}


	public static Journaldata lagJournaldata() {
		return Journaldata.builder()
				.onDemandId("ODAP08031000123")
				.saksnummer("3133123")
				.brukerId("11114928123")
				.brukertype("PERSON")
				.journalposttype(U)
				.fagomraade(OPP)
				.journaldato(LocalDateTime.parse("2008-03-10T17:19:22"))
				.innhold("Innk. til dialogmøte innen 26 uker der mottaker er arb.giver")
				.mottakernavn("DUCK DONALD")
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
				.brukertype("PERSON")
				.journalposttype(U)
				.fagomraade(OPP)
				.journaldato(LocalDateTime.parse("2008-03-10T17:19:22"))
				.innhold("Innk. til dialogmøte innen 26 uker der mottaker er arb.giver")
				.journalfoerendeEnhet("0211")
				.opprettetAvNavn("Tryll, Magika Von")
				.dokumentkategori(B)
				.brevkode("brevkode1")
				.build();
	}

	public static no.nav.arenaondemandtojoark.domain.db.Journaldata lagJournaldataentitetMedStatusInnlest(
			String ondemandId, String filnavn) {

		return no.nav.arenaondemandtojoark.domain.db.Journaldata.builder()
				.onDemandId(ondemandId)
				.saksnummer("3133123")
				.brukerId("11114928123")
				.brukertype("PERSON")
				.journalposttype(U)
				.fagomraade(OPP)
				.journaldato(LocalDateTime.parse("2008-03-10T17:19:22"))
				.innhold("Innk. til dialogmøte innen 26 uker der mottaker er arb.giver")
				.mottakernavn("DUCK DONALD")
				.mottakerId("80000009123")
				.utsendingskanal(L)
				.journalfoerendeEnhet("0211")
				.sendtPrintDato(LocalDateTime.parse("2011-12-08T15:07:48"))
				.opprettetAvNavn("Tryll, Magika Von")
				.dokumentkategori(B)
				.brevkode("brevkode1")
				.status(INNLEST)
				.filnavn(filnavn)
				.build();
	}

	public static no.nav.arenaondemandtojoark.domain.db.Journaldata lagJournaldataentitetMedStatusProsessert(
			String filnavn, String ondemandId, String journalpostId, String dokumentInfoId) {

		return no.nav.arenaondemandtojoark.domain.db.Journaldata.builder()
				.onDemandId(ondemandId)
				.saksnummer("3133123")
				.brukerId("11114928123")
				.brukertype("PERSON")
				.journalposttype(U)
				.fagomraade(OPP)
				.journaldato(LocalDateTime.parse("2008-03-10T17:19:22"))
				.innhold("Innk. til dialogmøte innen 26 uker der mottaker er arb.giver")
				.mottakernavn("DUCK DONALD")
				.mottakerId("80000009123")
				.utsendingskanal(L)
				.journalfoerendeEnhet("0211")
				.sendtPrintDato(LocalDateTime.parse("2011-12-08T15:07:48"))
				.opprettetAvNavn("Tryll, Magika Von")
				.dokumentkategori(B)
				.brevkode("brevkode1")
				.status(INNLEST)
				.filnavn(filnavn)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.build();
	}


}
