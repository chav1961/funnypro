package chav1961.funnypro.core.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;

/**
 * <p>This interface describes IFPro VM</p>
 * @author chav1961
 *
 */
public interface IFProVM extends AutoCloseable {
	public static final String		VM_FRB_SOURCE = "frbSource";
	public static final String		VM_FRB_READONLY = "frbReadOnly";
	
	/**
	 * <p>This is a callback interface for the resolutions</p>
	 * @author chav1961
	 *
	 */
	public interface IFProCallback {
		/**
		 * <p>Called after start resolution, undependently of 'onResolution' call.</p>
		 */
		void beforeFirstCall();
		
		/**
		 * <p>Call on every successful resolution</p>
		 * @param resolvedVariables variables bound</p>
		 * @return true if need continue resolutions, false otherwise
		 * @throws FProParsingException
		 * @throws FProPrintingException
		 */
		boolean onResolution(final Map<String,Object> resolvedVariables) throws FProParsingException, FProPrintingException;
		
		/**
		 * <p>Called before end resolution, undependently of 'onResolution' call.</p>
		 */
		void afterLastCall();
	}
	
	/**
	 * <p>Turn on VM</p>
	 * @param source to deserialize database from. If null - database will be clean
	 * @throws FProException
	 * @throws IOException
	 */
	void turnOn(InputStream source) throws FProException, IOException;

	/**
	 * <p>Turn off VM</p>
	 * @param target to serialize database to. If null - database will not be serialized
	 * @throws FProException
	 * @throws IOException
	 */
	void turnOff(OutputStream target) throws FProException, IOException;
	
	/**
	 * <p>Create and store new fact/rule base</p>
	 * @param target target to store database
	 * @throws FProException
	 * @throws IOException
	 */
	void newFRB(OutputStream target) throws FProException, IOException;
	
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
	 * @throws FProException
	 * @throws IOException
	 */
	boolean question(String question, IFProCallback callback) throws FProException, IOException;
	
	/**
	 * <p>Process question</p>
	 * @param question question text
	 * @param repo entites repo
	 * @param callback callback to process answer
	 * @return true if question resolved successfully
	 * @throws FProException
	 * @throws IOException
	 */
	boolean question(String question, IFProEntitiesRepo repo, IFProCallback callback) throws FProException, IOException;

	/**
	 * <p>Process goal</p>
	 * @param goal goal test
	 * @param callback callback to process goal answer
	 * @return true if goal was achived
	 * @throws FProException
	 * @throws IOException
	 */
	boolean goal(String goal, IFProCallback callback) throws FProException, IOException;
	
	/**
	 * <p>Process goal</p>
	 * @param goal goal test
	 * @param repo entites repo
	 * @param callback callback to process goal answer
	 * @return true if goal was achived
	 * @throws FProException
	 * @throws IOException
	 */
	boolean goal(String goal, IFProEntitiesRepo repo, IFProCallback callback) throws FProException, IOException;
	
	/**
	 * <p>Consult data into the fact/rule base</p>
	 * @param source source to consult data from
	 * @throws FProParsingException
	 * @throws IOException
	 */
	void consult(Reader source) throws FProParsingException, IOException;
	
	/**
	 * <p>Save data from the fact/rule base</p>
	 * @param target target to save data to
	 * @throws FProPrintingException
	 * @throws IOException
	 */
	void save(PrintWriter target) throws FProPrintingException, IOException;
	
	/**
	 * <p>Execute console for the VM</p>
	 * @param source source input
	 * @param target target output
	 * @param errors target error output
	 * @throws FProException
	 */
	void console(Reader source, PrintWriter target, PrintWriter errors) throws FProException;
}
