package no.nav.arenaondemandtojoark.consumer.ondemandbrev;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.config.ArenaondemandtojoarkProperties;
import no.nav.arenaondemandtojoark.exception.OndemandDokumentIkkeFunnetException;
import no.nav.arenaondemandtojoark.exception.OndemandNonRetryableException;
import no.nav.arenaondemandtojoark.exception.retryable.OndemandRetryableException;
import no.nav.arenaondemandtojoark.exception.retryable.OndemandTomResponseException;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.time.Duration;

import static java.lang.String.format;
import static no.nav.arenaondemandtojoark.consumer.RetryUtils.backoffRetrySpec;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@Slf4j
@Component
public class OndemandBrevConsumer {

	private static final String IDNR = "IDNR";
	private static final String APP_ID = "appID";

	private final WebClient webClient;
	private final String ondemandFolder;

	public OndemandBrevConsumer(
			ArenaondemandtojoarkProperties arenaondemandtojoarkProperties,
			WebClient webClient) {
		ondemandFolder = arenaondemandtojoarkProperties.getOndemandFolder();
		this.webClient = webClient.mutate()
				.baseUrl(arenaondemandtojoarkProperties.getEndpoints().getOndemand())
				.defaultHeader("nav-team-dok", "arenaondemandtojoark")
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(clientCodecConfigurer -> clientCodecConfigurer
								.defaultCodecs().maxInMemorySize((int) DataSize.ofMegabytes(100).toBytes()))
						.build())
				.build();
	}

	public byte[] hentPdf(final String ondemandId) {
		log.info("Henter pdf fra ondemand med ondemandId={}", ondemandId);

		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam(IDNR, ondemandId)
						.queryParam(APP_ID, ondemandFolder)
						.build())
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(Duration.ofSeconds(30));
				})
				.accept(APPLICATION_PDF)
				.retrieve()
				.bodyToMono(byte[].class)
				.switchIfEmpty(Mono.error(new OndemandTomResponseException("Payload fra ondemandbrev var tom.")))
				.doOnError(this::handleError)
				.retryWhen(backoffRetrySpec())
				.block();
	}

	private void handleError(Throwable error) {
		if (!(error instanceof WebClientResponseException response)) {
			String feilmelding = format("Kall mot ondemand feilet teknisk med feilmelding=%s", error.getMessage());

			log.warn(feilmelding);

			throw new OndemandRetryableException(feilmelding, error);
		}

		String feilmelding = format("Kall mot ondemand feilet %s med status=%s, feilmelding=%s, response=%s",
				response.getStatusCode().is4xxClientError() ? "funksjonelt" : "teknisk",
				response.getStatusCode(),
				response.getMessage(),
				response.getResponseBodyAsString());

		log.warn(feilmelding);

		if (response.getStatusCode().is4xxClientError()) {
			if (response.getStatusCode().isSameCodeAs(NOT_FOUND))
				throw new OndemandDokumentIkkeFunnetException("Dokument ikke funnet i Ondemand", error);
			throw new OndemandNonRetryableException(feilmelding, error);
		} else {
			throw new OndemandRetryableException(feilmelding, error);
		}
	}

}
