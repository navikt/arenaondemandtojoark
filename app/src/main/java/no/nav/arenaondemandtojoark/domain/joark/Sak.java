package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;

@Builder
public class Sak {
	private Sakstype sakstype;
	private String fagsakId;
	private Fagsaksystem fagsaksystem;
}
