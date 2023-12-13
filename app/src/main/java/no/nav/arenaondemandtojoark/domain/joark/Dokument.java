package no.nav.arenaondemandtojoark.domain.joark;


import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Dokument {
	String tittel;
	String brevkode;
	String dokumentKategori;
	List<DokumentVariant> dokumentvarianter;
}
