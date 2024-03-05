package no.nav.arenaondemandtojoark.domain.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class Journaldata {
	String onDemandId;
	String saksnummer;
	String brukerId;
	String brukerType;
	String journalpostType;
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

	public void setFagomraade(String fagomraade) {
		if ("KLA".equals(fagomraade)) {
			this.fagomraade = "AAP";
		} else {
			this.fagomraade = fagomraade;
		}
	}
}
