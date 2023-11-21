package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class Sak {
	private Sakstype sakstype;
	private String fagsakId;
	private Fagsaksystem fagsaksystem;
}
