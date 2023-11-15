package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import java.time.LocalDateTime;

public record FerdigstillJournalpostRequest (
		String journalfoerendeEnhet,
		String journalfortAvNavn,
		String opprettetAvNavn,
		LocalDateTime datoJournal,
		LocalDateTime datoSendtPrint) {
}
