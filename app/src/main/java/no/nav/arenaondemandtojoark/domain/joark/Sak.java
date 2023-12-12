package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
public class Sak {
	Sakstype sakstype;
	String fagsakId;
	Fagsaksystem fagsaksystem;
}
