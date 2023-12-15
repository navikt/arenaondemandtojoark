package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.config.ArenaondemandtojoarkProperties;
import no.nav.arenaondemandtojoark.exception.DokarkivNonRetryableException;
import no.nav.arenaondemandtojoark.exception.retryable.DokarkivRetryableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.time.Duration.ofMillis;
import static no.nav.arenaondemandtojoark.config.AzureTokenProperties.CLIENT_REGISTRATION_DOKARKIV;
import static no.nav.arenaondemandtojoark.config.AzureTokenProperties.getOAuth2AuthorizeRequestForAzure;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;
import static reactor.util.retry.Retry.backoff;

@Slf4j
@Component
public class DokarkivConsumer {

	@Value("${arenaondemandtojoark.consumer.max-attempts}")
	Long RETRY_MAX_ATTEMPTS;
	@Value("${arenaondemandtojoark.consumer.time-between-attempts}")
	Long RETRY_TIME_BETWEEN_ATTEMPTS;

	private final WebClient webClient;
	private final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

	public DokarkivConsumer(final ArenaondemandtojoarkProperties arenaondemandtojoarkProperties,
							final WebClient webClient,
							final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
		this.webClient = webClient.mutate()
				.baseUrl(arenaondemandtojoarkProperties.getEndpoints().getDokarkiv().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
		this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
	}

	public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
		log.info("Oppretter journalpost med ondemandId={}", request.getEksternReferanseId());

		return webClient.post()
				.uri(uriBuilder -> uriBuilder
						.queryParam("forsoekFerdigstill", false)
						.build())
				.attributes(getOAuth2AuthorizedClient())
				.bodyValue(request)
				.retrieve()
				.onStatus(httpStatus -> httpStatus.isSameCodeAs(CONFLICT), response -> Mono.empty())
				.bodyToMono(OpprettJournalpostResponse.class)
				.doOnError(this::handleError)
				.retryWhen(backoff(RETRY_MAX_ATTEMPTS, ofMillis(RETRY_TIME_BETWEEN_ATTEMPTS))
						.filter(e -> e instanceof DokarkivRetryableException)
						.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()))
				.block();
	}

	public void ferdigstillJournalpost(String journalpostId, FerdigstillJournalpostRequest request) {
		log.info("Ferdigstiller journalpost med journalpostId={}", journalpostId);

		webClient.patch()
				.uri(uriBuilder -> uriBuilder
						.path("/{journalpostId}/ferdigstill")
						.build(journalpostId))
				.attributes(getOAuth2AuthorizedClient())
				.bodyValue(request)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.retryWhen(backoff(RETRY_MAX_ATTEMPTS, ofMillis(RETRY_TIME_BETWEEN_ATTEMPTS))
						.filter(e -> e instanceof DokarkivRetryableException)
						.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure()))
				.block();
	}

	private void handleError(Throwable error) {
		if (!(error instanceof WebClientResponseException response)) {
			String feilmelding = format("Kall mot journalpostapi feilet teknisk med feilmelding=%s", error.getMessage());

			log.warn(feilmelding);

			throw new DokarkivRetryableException(feilmelding, error);
		}

		String feilmelding = format("Kall mot journalpostapi feilet %s med status=%s, feilmelding=%s, response=%s",
				response.getStatusCode().is4xxClientError() ? "funksjonelt" : "teknisk",
				response.getStatusCode(),
				response.getMessage(),
				response.getResponseBodyAsString());

		log.warn(feilmelding);

		if (response.getStatusCode().is4xxClientError()) {
			throw new DokarkivNonRetryableException(feilmelding, error);
		} else {
			throw new DokarkivRetryableException(feilmelding, error);
		}
	}

	private Consumer<Map<String, Object>> getOAuth2AuthorizedClient() {
		Mono<OAuth2AuthorizedClient> clientMono = oAuth2AuthorizedClientManager.authorize(getOAuth2AuthorizeRequestForAzure(CLIENT_REGISTRATION_DOKARKIV));
		return oauth2AuthorizedClient(clientMono.block());
	}
}