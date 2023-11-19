package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;

@Builder
public class AvsenderMottaker {
	private String id;
	private String navn;
	private AvsenderMottakerIdType idType;
}
