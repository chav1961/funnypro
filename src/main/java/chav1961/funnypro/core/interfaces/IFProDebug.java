package chav1961.funnypro.core.interfaces;

import java.io.Closeable;

/**
 * <p>This interface uses for debugging purposes. Supports transactions on the logger - any output can be rolled back if it's not need to
 * store to log (for example, if no program error was detected inside transaction). Allow to make detailed trace for powerful debug analyze,
 * but not really store it into log, if possible bug was not detected.</p>
 * @author chav1961
 *
 */
public interface IFProDebug extends Closeable {
	public enum SeverityLevel {
		trace, debug, info, warning, error
	}
	
	/**
	 * <p>Start transaction and push mark for the given log stream</p> 
	 * @param mark mark to push to. Template is <loggerName>:<anySequence>
	 * @return new interface to process transaction. This interface not need to be thread-safe
	 */
	IFProDebug push(String mark);
	
	/**
	 * <p>Print log message (possibly inside transaction)
	 * @param level severity level
	 * @param format message format
	 * @param parameters message parameters</p>
	 */
	void log(SeverityLevel level, String format, Object... parameters);
	
	/**
	 * <p>Print log message (possibly inside transaction)
	 * @param level severity level
	 * @param t exception to print
	 * @param format message format
	 * @param parameters message parameters</p>
	 */
	void log(SeverityLevel level, Throwable t, String format, Object... parameters);

	/**
	 * <p>Is this interface inside transaction</p>
	 * @return true if yes
	 */
	boolean isInsideTransaction();
	
	/**
	 * <p>Rollback transaction. Rolled transaction will not be placed into log</p>
	 */
	void rollback();
}
