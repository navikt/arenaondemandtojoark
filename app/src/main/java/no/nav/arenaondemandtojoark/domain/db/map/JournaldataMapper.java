package no.nav.arenaondemandtojoark.domain.db.map;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Dokumentkategori;
import no.nav.arenaondemandtojoark.domain.db.Fagomraade;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.domain.db.JournaldataStatus;
import no.nav.arenaondemandtojoark.domain.db.Journalposttype;
import no.nav.arenaondemandtojoark.domain.db.Utsendingskanal;
import no.nav.arenaondemandtojoark.exception.JournaldataMappingException;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_FILNAVN;

@Slf4j
public class JournaldataMapper {

	private static final String KAN_IKKE_MAPPE_FEILMELDING = "Kan ikke mappe til journaldata. Ugyldig verdi for %s=%s";

	@Handler
	public Journaldata map(no.nav.arenaondemandtojoark.domain.xml.Journaldata journaldata, Exchange exchange) {
		log.info("Mapper journaldata med ondemandId={}", journaldata.getOnDemandId());
		var filnavn = exchange.getProperty(PROPERTY_FILNAVN, String.class);

		return Journaldata.builder()
				.onDemandId(journaldata.getOnDemandId())
				.saksnummer(journaldata.getSaksnummer())
				.brukerId(journaldata.getBrukerId())
				.brukertype(journaldata.getBrukerType())
				.journalposttype(toEnum(Journalposttype.class, journaldata.getJournalpostType()))
				.fagomraade(toEnum(Fagomraade.class, journaldata.getFagomraade()))
				.journaldato(toLocalDateTime(journaldata.getJournaldato(), "journaldato"))
				.innhold(journaldata.getInnhold())
				.mottakernavn(journaldata.getMottakerNavn())
				.mottakerId(journaldata.getMottakerId())
				.utsendingskanal(toEnum(Utsendingskanal.class, journaldata.getUtsendingskanal()))
				.journalfoerendeEnhet(journaldata.getJournalfoerendeEnhet())
				.sendtPrintDato(toLocalDateTime(journaldata.getSendtPrintDato(), "sendtprintdato"))
				.opprettetAvNavn(journaldata.getOpprettetAvNavn())
				.dokumentkategori(toEnum(Dokumentkategori.class, journaldata.getDokumentkategori()))
				.brevkode(journaldata.getBrevkode())
				.status(JournaldataStatus.INNLEST)
				.filnavn(filnavn)
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
