package no.nav.arenaondemandtojoark.domain.xml.rapport;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "ondemandkonvertering")
@XmlAccessorType(XmlAccessType.FIELD)
public class Journalpostrapport {

	@XmlElement(name="bruker")
	private List<JournalpostrapportElement> journalposter;
}
