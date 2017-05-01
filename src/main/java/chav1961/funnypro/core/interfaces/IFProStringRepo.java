package chav1961.funnypro.core.interfaces;

import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

/**
 * <p>This interface collects all strings</p>
 * @author chav1961
 *
 */
public interface IFProStringRepo {
	/**
	 * <p>Put string into the repo and get it's id</p> 
	 * @param source string to put
	 * @return string id. If the string exists, returns existent id</p>
	 */
	long putString(String source);

	/**
	 * <p>Parse string escaping, put string into the repo and get it's id</p> 
	 * @param source string to parse ant put
	 * @return string id. If the string exists, returns existent id</p>
	 */
	long putEscapedString(String source);

	/**
	 * <p>Put string into the repo and get it's id</p> 
	 * @param source string to put
	 * @param from position from the source
	 * @param to position to the source
	 * @return string id. If the string exists, returns existent id</p>
	 */
	long putString(char[] source, int from, int to);

	/**
	 * <p>Parse string escaping, put string into the repo and get it's id</p> 
	 * @param source string to parse ant put
	 * @param from position from the source
	 * @param to position to the source
	 * @return string id. If the string exists, returns existent id</p>
	 */
	long putEscapedString(char[] source, int from, int to);
	
	String getString(long id);

	String getEscapedString(long id);

	int getString(long id, char[] target, int from);

	int getEscapedString(long id, char[] target, int from);

	SyntaxTreeInterface<?> getStringTree();
	
	
	long putPredicate(String predicate, int arity);

	long putPredicate(char[] predicate, int from, int to, int arity);
	
	IFProPredicate getPredicate(long id);

	SyntaxTreeInterface<?> getPredicateTree();

	
	long putOperator(IFProOperator op);
	
	IFProOperator getOperator(long id);
	
	SyntaxTreeInterface<?> getOperatorTree();

	
}
