package chav1961.funnypro.core.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>THis interface describes stream serializable items</p>
 * @author chav1961
 *
 */
public interface IStreamSerializable {
	/**
	 * <p>Serialize data to the given target</p>
	 * @param target target to serialize to
	 */
	void serialize(OutputStream target) throws IOException;
	
	/**
	 * <p>Deserialize data from the given source</p>
	 * @param source source to deserialize from
	 */
	void deserialize(InputStream source) throws IOException;
}
