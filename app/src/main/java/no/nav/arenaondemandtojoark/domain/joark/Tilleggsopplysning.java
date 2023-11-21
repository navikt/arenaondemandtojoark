package no.nav.arenaondemandtojoark.domain.joark;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class Tilleggsopplysning {
	private String nokkel;
	private String verdi;
}
