package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class Bruker {
	private BrukerIdType idType;
	private String id;
}