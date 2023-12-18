package no.nav.arenaondemandtojoark.consumer;

import no.nav.arenaondemandtojoark.exception.retryable.ArenaondemandtojoarkRetryableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import static java.time.Duration.ofMillis;

@Component
public class RetryUtils {

	private static Integer retryMaxAttempts;
	private static Integer retryTimeBetweenAttempts;

	public RetryUtils(@Value("${arenaondemandtojoark.consumer.max-attempts}") Integer retryMaxAttempts,
					  @Value("${arenaondemandtojoark.consumer.time-between-attempts}") Integer retryTimeBetweenAttempts) {
		RetryUtils.retryMaxAttempts = retryMaxAttempts;
		RetryUtils.retryTimeBetweenAttempts = retryTimeBetweenAttempts;
	}

	public static Retry backoffRetrySpec() {
		return Retry.backoff(retryMaxAttempts, ofMillis(retryTimeBetweenAttempts))
				.filter(e -> e instanceof ArenaondemandtojoarkRetryableException)
				.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
	}
}
