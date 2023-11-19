package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class FerdigstillJournalpostRequest {
		String journalfoerendeEnhet;
		String journalfortAvNavn;
		String opprettetAvNavn;
		LocalDateTime datoJournal;
		LocalDateTime datoSendtPrint;
}
