package chav1961.funnypro.core.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This interface describes IFPro VM</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.2
 */
public interface IFProVM extends AutoCloseable {
	public static final String		VM_FRB_SOURCE = "frbSource";
	public static final String		VM_FRB_READONLY = "frbReadOnly";
	public static final String		PROP_DONT_LOAD_ALL_PLUGINS = "fpro.dontload.allplugins";
	
	/**
	 * <p>This is a callback interface for the resolutions</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	@FunctionalInterface
	public interface IFProCallback {
		/**
		 * <p>Called after start resolution, independently of 'onResolution' call.</p>
		 */
		default void beforeFirstCall() {}
		
		/**
		 * <p>Call on every successful resolution</p>
		 * @param names variable names
		 * @param resolvedValues values bound to variables
		 * @param printedValues string representation of values bound to variables
		 * @return true if need continue resolutions, false otherwise
		 * @throws SyntaxException any problem on parsing data
		 * @throws FProPrintingException any problem on printing data
		 */
		boolean onResolution(String[] names, IFProEntity[] resolvedValues, String[] printedValues) throws SyntaxException, PrintingException;
		
		/**
		 * <p>Called before end resolution, independently of 'onResolution' call.</p>
		 */
		default void afterLastCall() {}
	}
	
	/**
	 * <p>Turn on VM</p>
	 * @param source to deserialize database from. If null - database will be clean
	 * @throws ContentException any persistence format processing problems
	 * @throws IOException any I/O exceptions
	 */
	void turnOn(InputStream source) throws ContentException, IOException;

	/**
	 * <p>Turn off VM</p>
	 * @param target to serialize database to. If null - database will not be serialized
	 * @throws ContentException any persistence format processing problems
	 * @throws IOException any I/O exceptions
	 */
	void turnOff(OutputStream target) throws ContentException, IOException;
	
	/**
	 * <p>Create and store new fact/rule base</p>
	 * @param target target to store database
	 * @throws ContentException any persistence format processing problems
	 * @throws IOException any I/O exceptions
	 */
	void newFRB(OutputStream target) throws ContentException, IOException;
	
	/**
	 * <p>Is VM turned on</p>
	 * @return true if yes
	 */
	boolean isTurnedOn();

	/**
	 * <p>Process question</p>
	 * @param question question text
	 * @param callback callback to process answer
	 * @return true if question resolved successfully
	 * @throws ContentException any processing problems
	 * @throws IOException any I/O exceptions
	 */
	boolean question(String question, IFProCallback callback) throws ContentException, IOException;
	
	/**
	 * <p>Process question</p>
	 * @param question question text
	 * @param repo entites repository
	 * @param callback callback to process answer
	 * @return true if question resolved successfully
	 * @throws ContentException any processing problems
	 * @throws IOException any I/O exceptions
	 */
	boolean question(String question, IFProEntitiesRepo repo, IFProCallback callback) throws ContentException, IOException;

	/**
	 * <p>Process goal</p>
	 * @param goal goal test
	 * @param callback callback to process goal answer
	 * @return true if goal was achieved
	 * @throws ContentException any processing problems
	 * @throws IOException any I/O exceptions
	 */
	boolean goal(String goal, IFProCallback callback) throws ContentException, IOException;
	
	/**
	 * <p>Process goal</p>
	 * @param goal goal test
	 * @param repo entites repo
	 * @param callback callback to process goal answer
	 * @return true if goal was achived
	 * @throws ContentException any processing problems
	 * @throws IOException any I/O exceptions
	 */
	boolean goal(String goal, IFProEntitiesRepo repo, IFProCallback callback) throws ContentException, IOException;
	
	/**
	 * <p>Consult data into the fact/rule base</p>
	 * @param source source to consult data from
	 * @throws SyntaxException any content parsing problems
	 * @throws IOException any I/O exceptions
	 */
	void consult(CharacterSource source) throws SyntaxException, IOException;
	
	/**
	 * <p>Save data from the fact/rule base</p>
	 * @param target target to save data to
	 * @throws FProPrintingException any content saving problems
	 * @throws IOException any I/O exceptions
	 */
	void save(CharacterTarget target) throws PrintingException, IOException;
	
	/**
	 * <p>Execute console for the VM</p>
	 * @param source source input
	 * @param target target output
	 * @param errors target error output
	 * @throws ContentException any processing problems
	 */
	void console(Reader source, Writer target, Writer errors) throws ContentException;
}
