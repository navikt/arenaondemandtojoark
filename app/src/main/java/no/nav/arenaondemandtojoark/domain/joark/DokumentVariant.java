package no.nav.arenaondemandtojoark.domain.joark;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DokumentVariant {
	String filtype;
	String variantformat;
	byte[] fysiskDokument;
	String filnavn;
}
