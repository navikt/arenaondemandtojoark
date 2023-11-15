package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import lombok.Data;
import no.nav.arenaondemandtojoark.domain.JournalpostType;

@Data
public class OpprettJournalpostRequest {

	private JournalpostType journalposttype;
	private AvsenderMottaker avsenderMottaker;
	private Bruker bruker;
	private String tema;
	private String tittel;
	private String kanal;
	private String journalfoerendeEnhet;
	private String eksternReferanseId;
	private Tilleggsopplysninger tilleggsopplysninger;
	private Sak sak;
	private Dokumenter dokumenter;

	@Data
	public static class AvsenderMottaker {
		private String id;
		private MottakerIdType idType;
		private String navn;
	}

	@Data
	public static class Bruker {
		private String id;
		private BrukerIdType idType;
	}

	@Data
	public static class Tilleggsopplysninger {
		private String nokkel;
		private String verdi;
	}

	@Data
	public static class Sak {
		private Sakstype sakstype;
		private String fagsakId;
		private String fagsaksystem;
	}

	@Data
	public static class Dokumenter {
		private String tittel;
		private String brevkode;
		private Dokumentvarianter dokumentvarianter;
	}

	@Data
	public static class Dokumentvarianter {
		private String filtype;
		private Variantformat variantformat;
		private byte[] fysiskDokument;
		private String filnavn;
	}


	public enum MottakerIdType {
		ORGNR,
		FNR
	}

	public enum BrukerIdType {
		ORGNR,
		FNR
	}

	public enum Sakstype {
		FAGSAK,
		GENERELL_SAK
	}

	public enum Variantformat {
		ARKIV
	}
}
