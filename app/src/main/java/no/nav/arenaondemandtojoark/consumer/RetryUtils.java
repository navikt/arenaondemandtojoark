package no.nav.arenaondemandtojoark.consumer;

import lombok.extern.slf4j.Slf4j;
import no.nav.arenaondemandtojoark.exception.retryable.ArenaondemandtojoarkRetryableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;

import static java.time.Duration.ofMillis;

@Slf4j
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
		log.info("Retryer {} ganger med {} ms mellom hver retry", retryMaxAttempts, retryTimeBetweenAttempts);
		return Retry.backoff(retryMaxAttempts, ofMillis(retryTimeBetweenAttempts))
				.filter(e -> e instanceof ArenaondemandtojoarkRetryableException)
				.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());
	}
}
