package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import java.util.List;

public record OpprettJournalpostResponse(
		List<DokumentInfo> dokumenter,
		String journalpostId,
		boolean journalpostferdigstilt) {

	public record DokumentInfo(String dokumentInfoId) {
	}
}

