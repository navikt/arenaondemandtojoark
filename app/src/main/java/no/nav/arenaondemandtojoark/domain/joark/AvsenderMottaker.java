package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class AvsenderMottaker {
	private String id;
	private String navn;
	private AvsenderMottakerIdType idType;
}
