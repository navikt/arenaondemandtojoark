package no.nav.arenaondemandtojoark.consumer.dokarkiv.map;

import no.nav.arenaondemandtojoark.consumer.dokarkiv.FerdigstillJournalpostRequest;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static no.nav.arenaondemandtojoark.domain.db.Journalposttype.U;

public class FerdigstillJournalpostRequestMapper {

	public static FerdigstillJournalpostRequest map(Journaldata journaldata) {

		return FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet(journaldata.getJournalfoerendeEnhet())
				.journalfortAvNavn(journaldata.getOpprettetAvNavn())
				.opprettetAvNavn(journaldata.getOpprettetAvNavn())
				.datoJournal(toDatoString(journaldata.getJournaldato()))
				.datoSendtPrint(U.equals(journaldata.getJournalposttype()) ? toDatoString(journaldata.getSendtPrintDato()) : null)
				.build();
	}
	
	private static String toDatoString(LocalDateTime dato) {
		return dato.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")).replace(" ", "T");
	}
}
