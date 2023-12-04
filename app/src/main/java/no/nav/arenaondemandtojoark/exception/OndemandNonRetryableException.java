package no.nav.arenaondemandtojoark.exception;

public class OndemandNonRetryableException extends ArenaondemandtojoarkNonRetryableException {

	public OndemandNonRetryableException(String message) {
		super(message);
	}
	public OndemandNonRetryableException(String message, Throwable cause) {
		super(message, cause);
	}
}
