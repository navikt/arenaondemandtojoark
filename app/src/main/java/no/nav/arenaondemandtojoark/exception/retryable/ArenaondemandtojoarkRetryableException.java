package no.nav.arenaondemandtojoark.exception.retryable;

public abstract class ArenaondemandtojoarkRetryableException extends RuntimeException {

	public ArenaondemandtojoarkRetryableException(String message) {
		super(message);
	}

	public ArenaondemandtojoarkRetryableException(String message, Throwable cause) {
		super(message, cause);
	}
}
