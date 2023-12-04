package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.domain.db.Avvik;
import no.nav.arenaondemandtojoark.exception.ArenaondemandtojoarkNonRetryableException;
import no.nav.arenaondemandtojoark.exception.retryable.ArenaondemandtojoarkRetryableException;
import no.nav.arenaondemandtojoark.repository.AvvikRepository;
import no.nav.arenaondemandtojoark.repository.JournaldataRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_FILNAVN;
import static no.nav.arenaondemandtojoark.ArenaOndemandToJoarkRoute.PROPERTY_ONDEMAND_ID;
import static no.nav.arenaondemandtojoark.domain.db.JournaldataStatus.AVVIK;

@Slf4j
@Service
@Transactional
public class AvvikService {

	private final AvvikRepository avvikRepository;
	private final JournaldataRepository journaldataRepository;

	public AvvikService(AvvikRepository avvikRepository,
						JournaldataRepository journaldataRepository) {
		this.avvikRepository = avvikRepository;
		this.journaldataRepository = journaldataRepository;
	}

	@Handler
	public void lagreAvvik(Exception exception, Exchange exchange) {

		log.info("Lagrer avvik for ondemandId={}", exchange.getProperty(PROPERTY_ONDEMAND_ID, String.class));

		var ondemandId = exchange.getProperty(PROPERTY_ONDEMAND_ID, String.class);
		var filnavn = exchange.getProperty(PROPERTY_FILNAVN, String.class);

		var avvik = Avvik.builder()
				.ondemandId(ondemandId)
				.filnavn(filnavn)
				.feiltype(getFeiltype(exception))
				.feilmelding(exception.getMessage())
				.build();

		avvikRepository.save(avvik);
		journaldataRepository.updateStatus(ondemandId, AVVIK);
	}

	private String getFeiltype(Exception exception) {

		return switch (exception) {
			case ArenaondemandtojoarkRetryableException e -> "Retryable";
			case ArenaondemandtojoarkNonRetryableException e -> "NonRetryable";
			default -> "Unknown";
		};
	}
}
