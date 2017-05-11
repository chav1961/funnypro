package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes FPro operator definitions</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProOperator extends IFProEntity, IFProRuledEntity {
	public static final int		MIN_PRTY = 0;
	public static final int		MAX_PRTY = 1200;
	public static final int		LEFT = 0;
	public static final int		RIGHT = 1;
	
	/**
	 * <p>This enumeration describes operator types</p>
	 */
	public enum OperatorType {
		xf, fx, yf, fy, xfy, yfx, xfx
	}
	
	/**
	 * <p>Get operator type</p>
	 * @return type of the operator
	 */
	OperatorType getType();
	
	/**
	 * <p>Get operator priority</p>
	 * @return operator priority
	 */
	int getPriority();
	
	/**
	 * <p>Get available underlying priority for the child (available for unary)</p> 
	 * @return underlying priority
	 */
	int getUnderlyingPriority();

	/**
	 * <p>Get available underlying priority for the child (available for binary)</p> 
	 * @param prioritySide side of the priority
	 * @return underlying priority
	 */
	int getUnderlyingPriority(int prioritySide);
	
	/**
	 * <p>Get left component of the list</p>
	 * @return left component of the list
	 */
	IFProEntity getLeft();
	
	/**
	 * <p>Get right component of the list</p>
	 * @return right component of the list
	 */
	IFProEntity getRight();
	
	/**
	 * <p>Set left component of the list</p>
	 * @param node left component to set
	 * @return self
	 */
	IFProOperator setLeft(IFProEntity node);
	
	/**
	 * <p>Set right component of the list</p>
	 * @param node right component to set
	 * @return self
	 */
	IFProOperator setRight(IFProEntity node);
}
