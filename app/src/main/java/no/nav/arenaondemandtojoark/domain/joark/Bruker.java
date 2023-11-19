package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;

@Builder
public class Bruker {
	private BrukerIdType idType;
	private String id;
}