package no.nav.arenaondemandtojoark;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.exception.ArenaondemandtojoarkNonRetryableException;
import no.nav.arenaondemandtojoark.exception.retryable.ArenaondemandtojoarkRetryableException;
import org.apache.camel.builder.RouteBuilder;

import static org.apache.camel.LoggingLevel.INFO;

@Slf4j
public abstract class BaseRoute extends RouteBuilder {

	private final AvvikService avvikService;

	protected BaseRoute(AvvikService avvikService) {
		this.avvikService = avvikService;
	}

	@Override
	public void configure() throws Exception{
		//@formatter:off
		onException(ArenaondemandtojoarkRetryableException.class,
				ArenaondemandtojoarkNonRetryableException.class)
				.log(INFO, log, "Håndterer definerte exceptions: ${exception}")
				.bean(avvikService)
				.handled(true)
				.end();

		onException(Exception.class)
				.log(INFO, log, "Håndterer alle exceptions: ${exception}")
				.bean(avvikService)
				.handled(true)
				.end();
		//@formatter:on
	}
}
