package chav1961.funnypro.core.interfaces;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import chav1961.funnypro.core.exceptions.FProParsingException;

/**
 * <p>This interface describes string serializable items</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProStringSerializable {
	/**
	 * <p>Serialize item to string</p>
	 * @return serialized string
	 */
	String serialize();
	
	/**
	 * <p>Serialize item to string builder</p>
	 * @param sb string builder to put serialized data
	 * @return actual position below the end string
	 */
	int serialize(StringBuilder sb); 
	
	/**
	 * <p>Serialize item to char array</p>
	 * @param target char array to serialize to
	 * @param from start position in the char array
	 * @return end position in the char array. In char array is shorter than need, returns negative value for the end position awaited! You can use this value to expand your array
	 */
	int serialize(char[] target, int from);
	
	/**
	 * <p>Serialize item to writer</p>
	 * @param target writer to serialize item to
	 * @throws IOException any I/O exceptions
	 */
	void serialize(Writer target) throws IOException;
	
	/**
	 * <p>Deserialize item from string</p>
	 * @param source char source containing item
	 * @throws FProParsingException any data structure problems were detected
	 */
	void deserialize(Reader source) throws FProParsingException;
}
