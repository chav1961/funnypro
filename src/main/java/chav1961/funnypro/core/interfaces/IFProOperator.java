package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes FPro operator definitions</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProOperator extends IFProEntity, IFProRuledEntity {
	public static final int	MIN_PRTY = 0;
	public static final int	MAX_PRTY = 1200;
	public static final int	LEFT = 0;
	public static final int	RIGHT = 1;

	/**
	 * <p>This enumeration rescribes operator sort</p> 
	 */
	public static enum OperatorSort {
		prefix, postfix, infix
	}
	
	/**
	 * <p>This enumeration describes operator types</p>
	 */
	public static enum OperatorType {
		xf(OperatorSort.prefix, 1), 
		fx(OperatorSort.postfix, 1), 
		yf(OperatorSort.prefix, 1), 
		fy(OperatorSort.postfix, 1), 
		xfy(OperatorSort.infix, 2), 
		yfx(OperatorSort.infix, 2), 
		xfx(OperatorSort.infix, 2);
		
		private final OperatorSort	sort;
		private final int			argCount;
		
		OperatorType(final OperatorSort sort, final int argCount) {
			this.sort = sort;
			this.argCount = argCount;
		}
		
		public OperatorSort getSort() {
			return sort;
		}
		
		public int getArgumentCount() {
			return argCount;
		}
	}
	
	/**
	 * <p>Get operator type</p>
	 * @return type of the operator
	 */
	OperatorType getOperatorType();
	
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
	
	/**
	 * <p>Is the entity ruled</p>
	 * @return true if yes
	 */
	default boolean isRuled() {
		return true;
	}
	
	static int getUnderlyingPriority(final IFProOperator op) {
		if (op == null) {
			throw new NullPointerException("Operator to test can't be null");
		}
		else {
			switch (op.getOperatorType()) {
				case fx 	: return op.getPriority() - 1;
				case fy 	: return op.getPriority();
				case xf 	: return op.getPriority() - 1;
				case yf 	: return op.getPriority();
				default 	: 
					throw new IllegalArgumentException("This call unavailable for infix operator!");
			}
		}
	}

	static int getUnderlyingPriority(final IFProOperator op, final int prioritySide) {
		if (op == null) {
			throw new NullPointerException("Operator to test can't be null");
		}
		else if (prioritySide == LEFT) {
			switch (op.getOperatorType()) {
				case xfx 	: return op.getPriority() - 1;
				case xfy 	: return op.getPriority() - 1;
				case yfx 	: return op.getPriority();
				default 	: 
					throw new IllegalArgumentException("This call is available for infix operators only!");
			}
		}
		else if (prioritySide == RIGHT) {
			switch (op.getOperatorType()) {
				case xfx 	: return op.getPriority() - 1;
				case xfy 	: return op.getPriority();
				case yfx 	: return op.getPriority() - 1;
				default 	: 
					throw new IllegalArgumentException("This call is available for infix operators only!");
			}
		}
		else {
			throw new IllegalArgumentException("Priority side can be [IFProOperator.LEFT] and [IFProOperator.RIGHT] only!"); 
		}
	}
}
