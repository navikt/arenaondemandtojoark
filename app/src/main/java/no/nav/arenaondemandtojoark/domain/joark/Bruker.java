package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
public class Bruker {
	BrukerIdType idType;
	String id;
}