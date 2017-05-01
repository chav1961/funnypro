package chav1961.funnypro.core.exceptions;

public class FProPrintingException extends FProException {
	private static final long serialVersionUID = 6747558191548783494L;

	public FProPrintingException() {
		super();
	}

	public FProPrintingException(final String message) {
		super(message);
	}

	public FProPrintingException(final Throwable t) {
		super(t);
	}
	
	public FProPrintingException(final String message, final Throwable t) {
		super(message,t);
	}
}
