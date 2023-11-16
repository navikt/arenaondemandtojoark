package no.nav.arenaondemandtojoark.consumer.ondemandbrev;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.config.ArenaondemandtojoarkProperties;
import no.nav.arenaondemandtojoark.exception.OndemandDokumentIkkeFunnetException;
import no.nav.arenaondemandtojoark.exception.OndemandFunctionalException;
import no.nav.arenaondemandtojoark.exception.OndemandTechnicalException;
import no.nav.arenaondemandtojoark.exception.OndemandTomResponseException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Component
public class OndemandBrevConsumer {
	private static final String ARENA_ONDEMAND_FOLDER = "AREP1";
	private static final String IDNR = "IDNR";

	private final WebClient webClient;

	public OndemandBrevConsumer(
			ArenaondemandtojoarkProperties arenaondemandtojoarkProperties,
			WebClient webClient) {
		this.webClient = webClient.mutate()
				.baseUrl(arenaondemandtojoarkProperties.getEndpoints().getOndemand())
				.defaultUriVariables(Map.of("appID", ARENA_ONDEMAND_FOLDER))
				.build();
	}

	public byte[] hentPdf(final String ondemandId) {

		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam(IDNR, ondemandId)
						.build())
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(Duration.ofSeconds(30));
				})
				.retrieve()
				.bodyToMono(byte[].class)
				.switchIfEmpty(Mono.error(new OndemandTomResponseException("Payload fra ondemandbrev var tom.")))
				.doOnError(this::handleError)
				.retryWhen(Retry.backoff(10, Duration.ofMillis(2000))
						.filter(e -> e instanceof OndemandTechnicalException)
						.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()))
				.block();
	}

	private void handleError(Throwable error) {
		if (!(error instanceof WebClientResponseException response)) {
			String feilmelding = format("Kall mot ondemand feilet teknisk med feilmelding=%s", error.getMessage());

			log.warn(feilmelding);

			throw new OndemandTechnicalException(feilmelding, error);
		}

		String feilmelding = format("Kall mot ondemand feilet %s med status=%s, feilmelding=%s, response=%s",
				response.getStatusCode().is4xxClientError() ? "funksjonelt" : "teknisk",
				response.getStatusCode(),
				response.getMessage(),
				response.getResponseBodyAsString());

		log.warn(feilmelding);

		if (response.getStatusCode().is4xxClientError()) {
			if (response.getStatusCode().isSameCodeAs(NOT_FOUND))
				throw new OndemandDokumentIkkeFunnetException("Dokument ikke funnet i Ondemand");
			throw new OndemandFunctionalException(feilmelding, error);
		} else {
			throw new OndemandTechnicalException(feilmelding, error);
		}
	}

}
