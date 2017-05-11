package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes quick array-based ordered list to use</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @param <D> quick list content
 */
public interface IFProQuickList<D> {
	/**
	 * <p>Test that array contains the given element</p> 
	 * @param key element to test
	 * @return true if contains
	 */
	boolean contains(long key);
	
	/**
	 * <p>Get content of the list</p>
	 * @return content of the list. Can be empty but not null
	 */
	Iterable<Long> content();
	
	/**
	 * <p>Get size of the list</p>
	 * @return size of the list
	 */
	int size();
	
	/**
	 * <p>Get data by it's key</p>
	 * @param key data key 
	 * @return data found of null if missing
	 */
	D get(long key);
	
	/**
	 * <p>Insert new data into array</p> 
	 * @param key data key do insert 
	 * @param data data value to insert
	 */
	void insert(long key, D data);
}
