package no.nav.arenaondemandtojoark.domain.db.validate;

import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.exception.JournaldataValideringException;
import org.apache.camel.Handler;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.nav.arenaondemandtojoark.domain.db.Journalposttype.U;
import static org.apache.logging.log4j.util.Strings.isBlank;

public class JournaldataValidator {

	private static final String FEILMELDING_FELT_MANGLER = "Journaldata mangler påkrevd felt=%s";

	@Handler
	public void validate(Journaldata journaldata) {
		final List<String> validationErrors = new ArrayList<>();

		validateAllePaakrevdeFelt(journaldata, validationErrors);

		if (!validationErrors.isEmpty()) {
			var feilmelding = "Journaldata med ondemandId=%s feilet validering med feilmeldinger: %s"
					.formatted(journaldata.getOnDemandId(), String.join("\n", validationErrors));

			throw new JournaldataValideringException(feilmelding);
		}
	}

	private void validateAllePaakrevdeFelt(Journaldata journaldata, List<String> validationErrors) {
		validerPaakrevdFelt("onDemandId", journaldata.getOnDemandId(), validationErrors);
		validerPaakrevdFelt("brukerId", journaldata.getBrukerId(), validationErrors);
		validerPaakrevdFelt("brukertype", journaldata.getBrukertype(), validationErrors);
		validerPaakrevdFelt("journalposttype", journaldata.getJournalposttype(), validationErrors);
		validerPaakrevdFelt("fagomraade", journaldata.getFagomraade(), validationErrors);
		validerPaakrevdFelt("journaldato", journaldata.getJournaldato(), validationErrors);
		validerPaakrevdFelt("innhold", journaldata.getInnhold(), validationErrors);
		validerPaakrevdFelt("journalfoerendeEnhet", journaldata.getJournalfoerendeEnhet(), validationErrors);
		validerPaakrevdFelt("opprettetAvNavn", journaldata.getOpprettetAvNavn(), validationErrors);
		validerPaakrevdFelt("brevkode", journaldata.getBrevkode(), validationErrors);

		if (U.equals(journaldata.getJournalposttype())) {
			validerPaakrevdFelt("mottakernavn", journaldata.getMottakernavn(), validationErrors);
			validerPaakrevdFelt("mottakerId", journaldata.getMottakerId(), validationErrors);
		}
	}

	private void validerPaakrevdFelt(String feltnavn, Object feltverdi, List<String> feilmeldinger) {
		if (feltverdi == null) {
			feilmeldinger.add(format(FEILMELDING_FELT_MANGLER, feltnavn));
		}
	}

	private void validerPaakrevdFelt(String feltnavn, String feltverdi, List<String> feilmeldinger) {
		if (isBlank(feltverdi)) {
			feilmeldinger.add(format(FEILMELDING_FELT_MANGLER, feltnavn));
		}
	}
}
