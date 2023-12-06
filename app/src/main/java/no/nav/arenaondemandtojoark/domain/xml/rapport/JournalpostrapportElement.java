package no.nav.arenaondemandtojoark.domain.xml.rapport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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