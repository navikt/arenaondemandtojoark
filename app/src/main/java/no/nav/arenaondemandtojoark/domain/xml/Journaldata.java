package no.nav.arenaondemandtojoark.domain.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Getter;
import lombok.Setter;
import no.nav.arenaondemandtojoark.domain.xml.adapters.FagomraadeAdapter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Journaldata {
	String onDemandId;
	String saksnummer;
	String brukerId;
	String brukerType;
	String journalpostType;
	@XmlJavaTypeAdapter(FagomraadeAdapter.class)
	String fagomraade;
	String journaldato;
	String innhold;
	String mottakerNavn;
	String mottakerId;
	String utsendingskanal;
	String journalfoerendeEnhet;
	String sendtPrintDato;
	String opprettetAvNavn;
	String dokumentkategori;
	String brevkode;
}
