package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Tilleggsopplysning {
	String nokkel;
	String verdi;
}
