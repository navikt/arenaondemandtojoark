package no.nav.arenaondemandtojoark.domain.xml.rapport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "bruker")
@XmlAccessorType(XmlAccessType.FIELD)
public class JournalpostrapportElement {
	@XmlElement(name="journalpost_id")
	private String journalpostId;
	@XmlElement(name="dokument_info_id")
	private String dokumentInfoId;
	@XmlElement(name="on_demand_id_fk")
	private String onDemandId;
}