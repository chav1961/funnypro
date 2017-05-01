package chav1961.funnypro.core.interfaces;


/**
 * <p>This interface extends operator description with it's mutability </p>
 * @author chav1961
 *
 */
public interface IFProMutableOperator extends IFProOperator {
	/**
	 * <p>Set operator code as string</p>
	 * @param code operator code
	 * @return self
	 */
	IFProMutableOperator setCode(String code);
	
	/**
	 * <p>Set operator code as char array</p>
	 * @param code operator code
	 * @return self
	 */
	IFProMutableOperator setCodeAsChar(char[] code);

	/**
	 * <p>Set operator code as char array</p>
	 * @param code operator code
	 * @param from form index in the array
	 * @param to to index in the array
	 * @return self
	 */
	IFProMutableOperator setCodeAsChar(char[] code, int from, int to);
	
	/**
	 * <p>Set operator type</p>
	 * @param type operator type
	 * @return self
	 */
	IFProMutableOperator setType(OperatorType type);
	
	/**
	 * <p>Set operator priority</p>
	 * @param priority operator priority
	 * @return self
	 */
	IFProMutableOperator setPriority(int priority);
}
