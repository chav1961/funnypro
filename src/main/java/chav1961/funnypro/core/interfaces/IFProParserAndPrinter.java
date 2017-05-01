package chav1961.funnypro.core.interfaces;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;

/**
 * <p>This interface supports parsing and printing for the FPro entities</p>
 * @author chav1961
 *
 */
public interface IFProParserAndPrinter {
	@FunctionalInterface
	public interface FProParserCallback {
		boolean process(IFProEntity entity, List<IFProVariable> vars) throws FProException, IOException;
	}
	
	/**
	 * <p>Parse entities from input stream and process it</p>
	 * @param source input stream containing entities
	 * @param callback callback to process parsed entity
	 * @throws FProParsingException
	 * @throws IOException
	 */
	void parseEntities(Reader source, FProParserCallback callback) throws FProException, IOException ;

	/**
	 * <p>Parse entities from input char array and process it</p>
	 * @param source input char array containing entities
	 * @param from start position of the array
	 * @param callback callback to process parsed entity
	 * @return last position in the source array where parsing ended
	 * @throws FProParsingException
	 * @throws IOException
	 */
	int parseEntities(char[] source, int from, FProParserCallback callback) throws FProException, IOException ;
	
	/**
	 * <p>Put entity to the output stream</p>
	 * @param entity entity to put
	 * @param target target to put to
	 * @throws IOException
	 * @throws FProPrintingException
	 */
	void putEntity(IFProEntity entity, Writer target) throws IOException, FProException;

	/**
	 * <p>Put entity to the output stream</p>
	 * @param entity entity to put
	 * @param target target to put to
	 * @throws IOException
	 * @throws FProPrintingException
	 */
	void putEntity(IFProEntity entity, StringBuilder target) throws IOException, FProException;
	
	/**
	 * <p>Put entity to the char array
	 * @param entity entity to put
	 * @param target array to put to
	 * @param from start position to put
	 * @return end position after storing data. If no enough place to store entity, position will be negative and it's value reflects real requirements to store data
	 * @throws IOException
	 */
	int putEntity(IFProEntity entity, char[] target, int from) throws IOException, FProException;
}
