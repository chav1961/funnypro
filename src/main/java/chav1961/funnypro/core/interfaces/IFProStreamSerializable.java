package chav1961.funnypro.core.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>THis interface describes stream serializable items</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProStreamSerializable {
	/**
	 * <p>Serialize data to the given target</p>
	 * @param target target to serialize to
	 * @throws IOException any I/O errors
	 */
	void serialize(OutputStream target) throws IOException;
	
	/**
	 * <p>Deserialize data from the given source</p>
	 * @param source source to deserialize from
	 * @throws IOException any I/O errors
	 */
	void deserialize(InputStream source) throws IOException;
}
