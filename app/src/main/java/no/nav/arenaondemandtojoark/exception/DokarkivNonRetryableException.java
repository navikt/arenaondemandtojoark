package no.nav.arenaondemandtojoark.exception;

public class DokarkivNonRetryableException extends ArenaondemandtojoarkNonRetryableException {

	public DokarkivNonRetryableException(String message) {
		super(message);
	}

	public DokarkivNonRetryableException(String message, Throwable cause) {
		super(message, cause);
	}
}
