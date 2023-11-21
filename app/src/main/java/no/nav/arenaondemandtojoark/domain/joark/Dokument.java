package no.nav.arenaondemandtojoark.domain.joark;


import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
public class Dokument {
	private String tittel;
	private String brevkode;
	private String dokumentKategori;
	private List<DokumentVariant> dokumentvarianter;
}
