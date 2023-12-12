package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AvsenderMottaker {
	String id;
	String navn;
	AvsenderMottakerIdType idType;
}
