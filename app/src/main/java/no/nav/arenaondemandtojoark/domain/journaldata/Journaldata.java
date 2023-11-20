package no.nav.arenaondemandtojoark.domain.journaldata;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class Journaldata {
	String onDemandId;
	String saksnummer;
	String brukerId;
	String brukerType;
	JournalpostType journalpostType;
	Fagomraade fagomraade;
	LocalDateTime journaldato;
	Journalstatus journalstatus;
	String innhold;
	String mottakerNavn;
	String mottakerId;
	Utsendingskanal utsendingskanal;
	String journalfoerendeEnhet;
	LocalDateTime sendtPrintDato;
	String opprettetAvNavn;
	Dokumentkategori dokumentkategori;
	LocalDateTime dokumentFerdigDato;
	String brevkode;
	boolean sensitivt;
}
