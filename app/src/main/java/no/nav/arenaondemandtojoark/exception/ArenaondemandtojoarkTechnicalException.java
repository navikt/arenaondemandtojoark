package no.nav.arenaondemandtojoark.exception;

public abstract class ArenaondemandtojoarkTechnicalException extends RuntimeException {
	public ArenaondemandtojoarkTechnicalException(String message) {
		super(message);
	}

	public ArenaondemandtojoarkTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
