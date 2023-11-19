package no.nav.arenaondemandtojoark.consumer.dokarkiv.map;

import no.nav.arenaondemandtojoark.consumer.dokarkiv.FerdigstillJournalpostRequest;
import no.nav.arenaondemandtojoark.domain.Journaldata;

import static no.nav.arenaondemandtojoark.domain.JournalpostType.U;

public class FerdigstillJournalpostRequestMapper {

	public static FerdigstillJournalpostRequest map(Journaldata journaldata) {

		return FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet(journaldata.getJournalfoerendeEnhet())
				.journalfortAvNavn(journaldata.getOpprettetAvNavn())
				.opprettetAvNavn(journaldata.getOpprettetAvNavn())
				.datoJournal(journaldata.getJournaldato()) //TODO Verdifiser at disse datoene er på riktig format
				.datoSendtPrint(U.equals(journaldata.getJournalpostType()) ? journaldata.getSendtPrintDato() : null)
				.build();
	}
}