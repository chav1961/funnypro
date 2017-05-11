package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes FPro list entities</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProList extends IFProEntity {
	/**
	 * <p>Get head of the list</p>
	 * @return head of the list. Can be empty, but not null
	 */
	IFProEntity getChild();
	
	/**
	 * <p>Set head of the list</p>
	 * @param head head of the list
	 * @return self
	 */
	IFProList setChild(IFProEntity head);
	
	/**
	 * <p>Get tail of the list</p>
	 * @return tail of the list. Can be null, if tail is missing
	 */
	IFProEntity getTail();
	
	/**
	 * <p>Set tail of the list</p>
	 * @param tail tail of the list.
	 * @return self
	 */
	IFProList setTail(IFProEntity tail);
}
