package no.nav.arenaondemandtojoark.domain.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Innlasting {

	private List<Journaldata> journaldataList;

	@XmlElement(name = "journaldata")
	public List<Journaldata> getInnlasting() {
		return journaldataList;
	}

	public void setInnlasting(List<Journaldata> innlasting) {
		this.journaldataList = innlasting;
	}
}
