package chav1961.funnypro.core.exceptions;

public class FProException extends Exception {
	private static final long serialVersionUID = 1532254101225110790L;

	public FProException() {
		super();
	}

	public FProException(final String message) {
		super(message);
	}

	public FProException(final Throwable t) {
		super(t);
	}
	
	public FProException(final String message, final Throwable t) {
		super(message,t);
	}
}
