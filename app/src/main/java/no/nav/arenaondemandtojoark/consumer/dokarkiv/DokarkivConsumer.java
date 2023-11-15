package no.nav.arenaondemandtojoark.consumer.dokarkiv;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.config.ArenaondemandtojoarkProperties;
import no.nav.arenaondemandtojoark.exception.DokarkivFunctionalException;
import no.nav.arenaondemandtojoark.exception.DokarkivTechnicalException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Consumer;

import static java.lang.String.format;
import static no.nav.arenaondemandtojoark.config.AzureTokenProperties.CLIENT_REGISTRATION_DOKARKIV;
import static no.nav.arenaondemandtojoark.config.AzureTokenProperties.getOAuth2AuthorizeRequestForAzure;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Slf4j
@Service
public class DokarkivConsumer {

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
		return webClient.post()
				.uri(uriBuilder -> uriBuilder
						.queryParam("forsoekFerdigstill", false)
						.build())
				.attributes(getOAuth2AuthorizedClient())
				.bodyValue(request)
				.retrieve()
				.bodyToMono(OpprettJournalpostResponse.class)
				.doOnError(this::handleError)
				.block();
	}

	public void ferdigstillJournalpost(String journalpostId, FerdigstillJournalpostRequest request) {
		webClient.patch()
				.uri(uriBuilder -> uriBuilder
						.queryParam("journalpostId", journalpostId)
						.build())
				.attributes(getOAuth2AuthorizedClient())
				.bodyValue(request)
				.retrieve()
				.toBodilessEntity()
				.doOnError(this::handleError)
				.block();
	}

	private void handleError(Throwable error) {
		if (!(error instanceof WebClientResponseException response)) {
			String feilmelding = format("Kall mot journalpostapi feilet teknisk med feilmelding=%s", error.getMessage());

			log.warn(feilmelding);

			throw new DokarkivTechnicalException(feilmelding, error);
		}

		String feilmelding = format("Kall mot journalpostapi feilet %s med status=%s, feilmelding=%s, response=%s",
				response.getStatusCode().is4xxClientError() ? "funksjonelt" : "teknisk",
				response.getStatusCode(),
				response.getMessage(),
				response.getResponseBodyAsString());

		log.warn(feilmelding);

		if (response.getStatusCode().is4xxClientError()) {
			throw new DokarkivFunctionalException(feilmelding, error);
		} else {
			throw new DokarkivTechnicalException(feilmelding, error);
		}
	}

	private Consumer<Map<String, Object>> getOAuth2AuthorizedClient() {
		Mono<OAuth2AuthorizedClient> clientMono = oAuth2AuthorizedClientManager.authorize(getOAuth2AuthorizeRequestForAzure(CLIENT_REGISTRATION_DOKARKIV));
		return oauth2AuthorizedClient(clientMono.block());
	}
}