package no.nav.arenaondemandtojoark.domain.joark;


import lombok.Builder;

@Builder
public class DokumentVariant {
	private String filtype;
	private String variantformat;
	private byte[] fysiskDokument;
	private String filnavn;
}
