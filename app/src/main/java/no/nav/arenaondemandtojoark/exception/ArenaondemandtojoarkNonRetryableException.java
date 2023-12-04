package no.nav.arenaondemandtojoark.exception;

public class ArenaondemandtojoarkNonRetryableException extends RuntimeException {

	public ArenaondemandtojoarkNonRetryableException(String message, Throwable cause) {
		super(message, cause);
	}
	public ArenaondemandtojoarkNonRetryableException(String message) {
		super(message);
	}
}
