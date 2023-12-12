package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FerdigstillJournalpostRequest {
		String journalfoerendeEnhet;
		String journalfortAvNavn;
		String opprettetAvNavn;
		String datoJournal;
		String datoSendtPrint;
}
