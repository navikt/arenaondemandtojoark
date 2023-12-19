package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Avvik;
import no.nav.arenaondemandtojoark.domain.db.Journaldata;
import no.nav.arenaondemandtojoark.exception.retryable.ArenaondemandtojoarkRetryableException;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.Math.min;
import static no.nav.arenaondemandtojoark.domain.db.Avvik.MAX_FEILMELDING_LENGDE;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;

@Slf4j
@Service
@Transactional
public class AvvikService {

	private final JournaldataRepository journaldataRepository;

	public AvvikService(JournaldataRepository journaldataRepository) {
		this.journaldataRepository = journaldataRepository;
	}

	@Handler
	public void lagreAvvik(Exception exception, Exchange exchange) {

		Journaldata journaldata = (Journaldata) exchange.getIn().getBody();

		var feilmelding = exception.getMessage();

		log.info("Lagrer avvik for ondemandId={} og filnavn={}",
				journaldata.getOnDemandId(),
				journaldata.getFilnavn(),
				exception);

		var avvik = journaldata.getAvvik();

		if (avvik == null) {
			avvik = new Avvik();
			journaldata.setAvvik(avvik);
			journaldata.setStatus(AVVIK);
		}

		avvik.setRetryable(isRetryable(exception));
		avvik.setFeilmelding(mapFeilmelding(feilmelding));

		journaldataRepository.save(journaldata);
	}

	private static String mapFeilmelding(String feilmelding) {
		return feilmelding.substring(0, min(feilmelding.length(), MAX_FEILMELDING_LENGDE));
	}

	private boolean isRetryable(Exception exception) {
		return exception instanceof ArenaondemandtojoarkRetryableException;
	}
}
