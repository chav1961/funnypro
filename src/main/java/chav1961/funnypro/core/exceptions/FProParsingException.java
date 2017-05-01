package chav1961.funnypro.core.exceptions;

public class FProParsingException extends FProException {
	private static final long 		serialVersionUID = 6747558191548783494L;
	private static final String		MESSAGE_FORMAT = "Parse error at %1$d:%2$d : %3$s";

	public FProParsingException(final int row, final int col, final String message) {
		super(String.format(MESSAGE_FORMAT,row,col,message));
	}

	public FProParsingException(final int row, final int col, final Throwable t) {
		super(String.format(MESSAGE_FORMAT,row,col,t.getMessage()),t);
	}
	
	public FProParsingException(final int row, final int col, final String message, final Throwable t) {
		super(String.format(MESSAGE_FORMAT,row,col,message),t);
	}

	public FProParsingException(final int[] rowCol, final String message) {
		this(rowCol[0],rowCol[1],message);
	}

	public FProParsingException(final int[] rowCol, final Throwable t) {
		this(rowCol[0],rowCol[1],t);
	}
	
	public FProParsingException(final int[] rowCol, final String message, final Throwable t) {
		this(rowCol[0],rowCol[1],message,t);
	}
}
