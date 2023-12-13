package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Sak {
	Sakstype sakstype;
	String fagsakId;
	Fagsaksystem fagsaksystem;
}
