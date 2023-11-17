package no.nav.arenaondemandtojoark.domain.xml;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import no.nav.arenaondemandtojoark.domain.Dokumentkategori;
import no.nav.arenaondemandtojoark.domain.Fagomraade;
import no.nav.arenaondemandtojoark.domain.JournalpostType;
import no.nav.arenaondemandtojoark.domain.Journalstatus;
import no.nav.arenaondemandtojoark.domain.Utsendingskanal;

import java.time.LocalDateTime;

@Data
@XmlType(name = "journaldata")
@XmlAccessorType(XmlAccessType.FIELD)
public class Journaldata {
	@XmlElement(required = true)
	String onDemandId;

	String saksnummer;

	@XmlElement(required = true)
	String brukerId;

	@XmlElement(required = true)
	String brukerType;

	@XmlElement(required = true)
	JournalpostType journalpostType;

	@XmlElement(required = true)
	Fagomraade fagomraade;

	@XmlElement(required = true)
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	LocalDateTime journaldato;

	@XmlElement(required = true)
	Journalstatus journalstatus;

	@XmlElement(required = true)
	String innhold;

	String mottakerNavn;

	@XmlElement(required = true)
	String mottakerId;

	Utsendingskanal utsendingskanal;

	@XmlElement(required = true)
	String journalfoerendeEnhet;

	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	LocalDateTime sendtPrintDato;

	@XmlElement(required = true)
	String opprettetAvNavn;

	Dokumentkategori dokumentkategori;

	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	LocalDateTime dokumentFerdigDato;

	@XmlElement(required = true)
	String brevkode;

	boolean sensitivt;
}
