package chav1961.funnypro.core.interfaces;

import java.io.IOException;
import java.util.List;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This interface supports parsing and printing for the FPro entities</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProParserAndPrinter {
	/**
	 * <p>This interface describes callback for processing goals and questions</p>  
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	public interface FProParserCallback {
		/**
		 * <p>Process one resolved goal (question)</p>
		 * @param entity source goal
		 * @param vars values of the variables binded
		 * @return true to continue backtracking, false to terminale resolution
		 * @throws FProException if any problems was detected
		 * @throws IOException if any I/O errors was detected
		 */
		boolean process(IFProEntity entity, List<IFProVariable> vars) throws SyntaxException, IOException;
	}
	
	/**
	 * <p>Parse entities from input stream and process it</p>
	 * @param source input stream containing entities
	 * @param callback callback to process parsed entity
	 * @throws FProParsingException any parsing data problems
	 * @throws ContentException any character source content problems
	 * @throws IOException any I/O problems
	 */
	void parseEntities(CharacterSource source, FProParserCallback callback) throws SyntaxException, ContentException, IOException ;

	/**
	 * <p>Parse entities from input char array and process it</p>
	 * @param source input char array containing entities
	 * @param from start position of the array
	 * @param callback callback to process parsed entity
	 * @return last position in the source array where parsing ended
	 * @throws FProParsingException any parsing data problems
	 * @throws IOException any I/O problems
	 */
	int parseEntities(char[] source, int from, FProParserCallback callback) throws SyntaxException, IOException ;
	
	/**
	 * <p>Put entity to the output stream</p>
	 * @param entity entity to put
	 * @param target target to put to
	 * @throws IOException any I/O problems
	 * @throws PrintingException any data printing problems
	 */
	void putEntity(IFProEntity entity, CharacterTarget target) throws IOException, PrintingException;

	/**
	 * <p>Put entity to the char array
	 * @param entity entity to put
	 * @param target array to put to
	 * @param from start position to put
	 * @return end position after storing data. If no enough place to store entity, position will be negative and it's value reflects real requirements to store data
	 * @throws IOException any I/O problems
	 * @throws PrintingException any data printing problems
	 */
	int putEntity(IFProEntity entity, char[] target, int from) throws IOException, PrintingException;
}
