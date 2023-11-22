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
	String brukertype;
	Journalposttype journalposttype;
	Fagomraade fagomraade;
	LocalDateTime journaldato;
	String innhold;
	String mottakernavn;
	String mottakerId;
	Utsendingskanal utsendingskanal;
	String journalfoerendeEnhet;
	LocalDateTime sendtPrintDato;
	String opprettetAvNavn;
	Dokumentkategori dokumentkategori;
	String brevkode;
}
