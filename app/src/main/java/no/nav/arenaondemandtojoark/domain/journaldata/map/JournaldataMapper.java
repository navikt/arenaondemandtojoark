package no.nav.arenaondemandtojoark.domain.journaldata.map;

import no.nav.arenaondemandtojoark.domain.journaldata.Dokumentkategori;
import no.nav.arenaondemandtojoark.domain.journaldata.Fagomraade;
import no.nav.arenaondemandtojoark.domain.journaldata.Journaldata;
import no.nav.arenaondemandtojoark.domain.journaldata.JournalpostType;
import no.nav.arenaondemandtojoark.domain.journaldata.Utsendingskanal;
import no.nav.arenaondemandtojoark.exception.JournaldataMappingException;
import org.apache.camel.Handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class JournaldataMapper {

	private static final String KAN_IKKE_MAPPE_FEILMELDING = "Kan ikke mappe til journaldata. Ugyldig verdi for %s=%s";

	@Handler
	public Journaldata map(no.nav.arenaondemandtojoark.domain.xml.Journaldata journaldata) {

		return Journaldata.builder()
			.onDemandId(journaldata.getOnDemandId())
			.saksnummer(journaldata.getSaksnummer())
			.brukerId(journaldata.getBrukerId())
			.brukerType(journaldata.getBrukerType())
			.journalpostType(toEnum(JournalpostType.class, journaldata.getJournalpostType()))
			.fagomraade(toEnum(Fagomraade.class, journaldata.getFagomraade()))
			.journaldato(toLocalDateTime(journaldata.getJournaldato(), "journaldato"))
			.innhold(journaldata.getInnhold())
			.mottakerNavn(journaldata.getMottakerNavn())
			.mottakerId(journaldata.getMottakerId())
			.utsendingskanal(toEnum(Utsendingskanal.class, journaldata.getUtsendingskanal()))
			.journalfoerendeEnhet(journaldata.getJournalfoerendeEnhet())
			.sendtPrintDato(toLocalDateTime(journaldata.getSendtPrintDato(), "sendtprintdato"))
			.opprettetAvNavn(journaldata.getOpprettetAvNavn())
			.dokumentkategori(toEnum(Dokumentkategori.class, journaldata.getDokumentkategori()))
			.brevkode(journaldata.getBrevkode())
			.build();
	}

	private static <T extends Enum<T>> T toEnum(Class<T> enumType, String value) {
		if (value == null) {
			return null;
		}

		try {
			return Enum.valueOf(enumType, value);
		} catch (IllegalArgumentException e) {
			throw new JournaldataMappingException(KAN_IKKE_MAPPE_FEILMELDING
					.formatted(enumType.getSimpleName().toLowerCase(), value));
		}
	}

	private static LocalDateTime toLocalDateTime(String value, String feltnavn) {
		if (value == null) {
			return null;
		}

		try {
			return LocalDateTime.parse(value);
		} catch (DateTimeParseException e) {
			throw new JournaldataMappingException(KAN_IKKE_MAPPE_FEILMELDING
					.formatted(feltnavn, value));
		}
	}
}
