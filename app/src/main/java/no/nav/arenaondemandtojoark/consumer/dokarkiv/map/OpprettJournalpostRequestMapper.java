package no.nav.arenaondemandtojoark.consumer.dokarkiv.map;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.consumer.dokarkiv.OpprettJournalpostRequest;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.joark.AvsenderMottaker;
import no.nav.arenaondemandtojoark.domain.joark.AvsenderMottakerIdType;
import no.nav.arenaondemandtojoark.domain.joark.Bruker;
import no.nav.arenaondemandtojoark.domain.joark.BrukerIdType;
import no.nav.arenaondemandtojoark.domain.joark.Dokument;
import no.nav.arenaondemandtojoark.domain.joark.DokumentVariant;
import no.nav.arenaondemandtojoark.domain.joark.JournalpostType;
import no.nav.arenaondemandtojoark.domain.joark.Sak;
import no.nav.arenaondemandtojoark.domain.joark.Tilleggsopplysning;
import no.nav.arenaondemandtojoark.exception.JournalpostdataMappingException;

import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.arenaondemandtojoark.domain.db.Journalposttype.U;
import static no.nav.arenaondemandtojoark.domain.joark.Fagsaksystem.AO01;
import static no.nav.arenaondemandtojoark.domain.joark.JournalpostType.NOTAT;
import static no.nav.arenaondemandtojoark.domain.joark.JournalpostType.UTGAAENDE;
import static no.nav.arenaondemandtojoark.domain.joark.Sakstype.FAGSAK;
import static no.nav.arenaondemandtojoark.domain.joark.Sakstype.GENERELL_SAK;
import static org.apache.logging.log4j.util.Strings.isBlank;

@Slf4j
public class OpprettJournalpostRequestMapper {

	private static final String PERSON = "Person";
	private static final String ONDEMAND_ID = "onDemandId";
	private static final String BRUKER_ID = "BrukerId";
	private static final String PDF = "PDF";
	private static final String ARKIV = "ARKIV";
	private static final String TSS_ID_PREFIX = "80";

	public static OpprettJournalpostRequest map(Journaldata journaldata, byte[] pdfDocument) {

		return OpprettJournalpostRequest.builder()
				.journalposttype(toJournalpostType(journaldata))
				.avsenderMottaker(toAvsenderMottaker(journaldata))
				.bruker(toBruker(journaldata))
				.tema(journaldata.getFagomraade().name())
				.tittel(journaldata.getInnhold())
				.kanal(toUtsendingskanal(journaldata))
				.journalfoerendeEnhet(journaldata.getJournalfoerendeEnhet())
				.eksternReferanseId(journaldata.getOnDemandId())
				.tilleggsopplysninger(toTilleggsopplysninger(journaldata))
				.sak(toSak(journaldata))
				.dokumenter(toDokumenter(journaldata, pdfDocument))
				.build();
	}

	private static String toUtsendingskanal(Journaldata journaldata) {
		var utsendingskanal = journaldata.getUtsendingskanal();

		if (U.equals(journaldata.getJournalposttype()))
			return utsendingskanal == null ? "S" : utsendingskanal.name();

		return utsendingskanal.name();
	}

	private static JournalpostType toJournalpostType(Journaldata journaldata) {
		return switch (journaldata.getJournalposttype()) {
			case U -> UTGAAENDE;
			case N -> NOTAT;
		};
	}

	private static AvsenderMottaker toAvsenderMottaker(Journaldata journaldata) {
		if (U.equals(journaldata.getJournalposttype())) {
			return AvsenderMottaker.builder()
					.id(toMottakerId(journaldata.getMottakerId()))
					.idType(toAvsenderMottakerIdType(journaldata.getMottakerId()))
					.navn(journaldata.getMottakernavn())
					.build();
		}

		return null;
	}

	private static String toMottakerId(String mottakerId) {
		if ((mottakerId.length() == 9) || (mottakerId.length() == 11 && !mottakerId.startsWith(TSS_ID_PREFIX))) {
			return mottakerId;
		}

		return null;
	}

	private static AvsenderMottakerIdType toAvsenderMottakerIdType(String mottakerId) {
		if (mottakerId.length() == 9) {
			return AvsenderMottakerIdType.ORGNR;
		} else if (mottakerId.length() == 11 && !mottakerId.startsWith(TSS_ID_PREFIX)) {
			return AvsenderMottakerIdType.FNR;
		}

		return null;
	}

	private static Bruker toBruker(Journaldata journaldata) {
		return Bruker.builder()
				.id(valider(journaldata, journaldata.getBrukerId(), BRUKER_ID))
				.idType(PERSON.equalsIgnoreCase(journaldata.getBrukertype()) ? BrukerIdType.FNR : BrukerIdType.ORGNR)
				.build();
	}

	private static List<Tilleggsopplysning> toTilleggsopplysninger(Journaldata journaldata) {
		return singletonList(
				Tilleggsopplysning.builder()
						.nokkel(ONDEMAND_ID)
						.verdi(valider(journaldata, journaldata.getOnDemandId(), ONDEMAND_ID))
						.build()
		);
	}

	private static Sak toSak(Journaldata journaldata) {
		var saksnummerErSatt = isBlank(journaldata.getSaksnummer());
		var sakstype = saksnummerErSatt ? FAGSAK : GENERELL_SAK;
		var fagsakId = saksnummerErSatt ? journaldata.getSaksnummer() : null;
		var fagsaksystem = saksnummerErSatt ? AO01 : null;

		return Sak.builder()
				.sakstype(sakstype)
				.fagsakId(fagsakId)
				.fagsaksystem(fagsaksystem)
				.build();
	}

	public static List<Dokument> toDokumenter(Journaldata journaldata, byte[] pdfDocument) {
		if (pdfDocument == null || pdfDocument.length == 0) {
			throw new JournalpostdataMappingException("Kan ikke mappe journaldata med ondemandId=%s. PDF-dokument mangler eller er tomt."
					.formatted(journaldata.getOnDemandId()));
		}

		List<DokumentVariant> dokumentvarianter = singletonList(
				DokumentVariant.builder()
						.filtype(PDF)
						.variantformat(ARKIV)
						.fysiskDokument(pdfDocument)
						.filnavn(journaldata.getOnDemandId() + ".pdf")
						.build());

		return singletonList(
				Dokument.builder()
						.tittel(journaldata.getInnhold())
						.brevkode(journaldata.getBrevkode())
						.dokumentKategori(journaldata.getDokumentkategori().name())
						.dokumentvarianter(dokumentvarianter)
						.build());
	}

	public static String valider(Journaldata journaldata, String value, String valueName) {
		if (value == null || value.isEmpty()) {
			throw new JournalpostdataMappingException("Kan ikke mappe journaldata med ondemandId=%s. Feltet %s=%s mangler eller er tomt"
					.formatted(valueName, value, journaldata.getOnDemandId()));
		}

		return value;
	}
}
