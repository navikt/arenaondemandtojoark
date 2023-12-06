package no.nav.arenaondemandtojoark.domain.xml.rapport.map;

import no.nav.arenaondemandtojoark.domain.db.projections.Rapportelement;
import no.nav.arenaondemandtojoark.domain.xml.rapport.JournalpostrapportElement;

public class JournalpostrapportMapper {

	public static JournalpostrapportElement map(Rapportelement rapportelement) {
		return JournalpostrapportElement.builder()
				.journalpostId(rapportelement.journalpostId())
				.dokumentInfoId(rapportelement.dokumentInfoId())
				.onDemandId(rapportelement.onDemandId())
				.build();
	}

}
