package no.nav.arenaondemandtojoark.consumer.dokarkiv.map;

import no.nav.arenaondemandtojoark.consumer.dokarkiv.FerdigstillJournalpostRequest;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static no.nav.arenaondemandtojoark.domain.db.Journalposttype.U;

public class FerdigstillJournalpostRequestMapper {

	public static final ZoneId EUROPE_OSLO = ZoneId.of("Europe/Oslo");

	public static FerdigstillJournalpostRequest map(Journaldata journaldata) {

		return FerdigstillJournalpostRequest.builder()
				.journalfoerendeEnhet(journaldata.getJournalfoerendeEnhet())
				.journalfortAvNavn(journaldata.getOpprettetAvNavn())
				.opprettetAvNavn(journaldata.getOpprettetAvNavn())
				.datoJournal(toDato(journaldata.getJournaldato()))
				.datoSendtPrint(U.equals(journaldata.getJournalposttype()) ? toDato(journaldata.getSendtPrintDato()) : null)
				.build();
	}

	private static Date toDato(LocalDateTime dato) {
		return Date.from(dato.atZone(EUROPE_OSLO).toInstant());
	}
}
