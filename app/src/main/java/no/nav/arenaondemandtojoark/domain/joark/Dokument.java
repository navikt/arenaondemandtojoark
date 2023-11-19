package no.nav.arenaondemandtojoark.domain.joark;


import lombok.Builder;

import java.util.List;

@Builder
public class Dokument {
	private String tittel;
	private String brevkode;
	private String dokumentKategori;
	private List<DokumentVariant> dokumentvarianter;
}
