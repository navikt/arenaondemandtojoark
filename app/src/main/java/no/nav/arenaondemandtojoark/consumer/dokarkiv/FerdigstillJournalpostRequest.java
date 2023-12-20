package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Value
@Builder
public class FerdigstillJournalpostRequest {
		String journalfoerendeEnhet;
		String journalfortAvNavn;
		String opprettetAvNavn;
		Date datoJournal;
		Date datoSendtPrint;
}
