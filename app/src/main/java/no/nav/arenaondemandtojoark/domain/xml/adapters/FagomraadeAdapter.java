package no.nav.arenaondemandtojoark.domain.xml.adapters;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class FagomraadeAdapter extends XmlAdapter<String, String> {

	@Override
	public String unmarshal(String fagomraade) {
		if ("KLA".equals(fagomraade)) {
			return "AAP";
		} else {
			return fagomraade;
		}
	}

	@Override
	public String marshal(String fagomraade) {
		return fagomraade;
	}
}
