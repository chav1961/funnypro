package chav1961.funnypro.core.exceptions;

/**
 * <p>This is a parent exception for all Funny Prolog project exceptions</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
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
