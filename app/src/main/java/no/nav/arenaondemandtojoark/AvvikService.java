package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Avvik;
import no.nav.arenaondemandtojoark.exception.retryable.ArenaondemandtojoarkRetryableException;
import no.nav.arenaondemandtojoark.repository.AvvikRepository;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.Math.min;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_FILNAVN;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_ONDEMAND_ID;
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

		log.info("Lagrer avvik for ondemandId={}", exchange.getProperty(PROPERTY_ONDEMAND_ID, String.class), exception);

		var ondemandId = exchange.getProperty(PROPERTY_ONDEMAND_ID, String.class);
		var filnavn = exchange.getProperty(PROPERTY_FILNAVN, String.class);
		var feilmelding = exception.getMessage();

		var avvik = Avvik.builder()
				.ondemandId(ondemandId)
				.filnavn(filnavn)
				.retryable(isRetryable(exception))
				.feilmelding(feilmelding.substring(0, min(feilmelding.length(), MAX_FEILMELDING_LENGDE)))
				.build();

		var journaldata = journaldataRepository.getByOnDemandId(ondemandId);
		journaldata.setAvvik(avvik);
		journaldata.setStatus(AVVIK);

		journaldataRepository.save(journaldata);
	}

	private boolean isRetryable(Exception exception) {
		return exception instanceof ArenaondemandtojoarkRetryableException;
	}
}
