package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes FProVM external plugin manager</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProPluginManager {
	/**
	 * <p>Install external plugin module to process operator</p>
	 * @param operator operator code
	 * @param type operator type
	 * @param prioprity operator priority
	 * @param pred java class to implements operator
	 */
	<Global,Local> void installOperator(String operator, IFProOperator.OperatorType type, int prioprity, IResolvable<Global,Local> pred);
	
	/**
	 * <p>Uninstall external plugin module to process operator</p>
	 * @param operator operator code (same as previous call)
	 * @param type operator type
	 * @param prioprity operator priority
	 */
	void uninstallOperator(String operator, IFProOperator.OperatorType type, int prioprity);
	
	/**
	 * <p>Install external plugin module to process predicate</p>
	 * @param predicate predicate header (for example pred(X,Y,_) )
	 * @param pred java class to implements predicate
	 */
	<Global,Local> void installPredicate(String predicate, IResolvable<Global,Local> pred);
	
	/**
	 * <p>Uninstall external plugin module to process predicate</p>
	 * @param predicate pedicate header (same as previous call)
	 * @param arity arity of the predicate
	 */
	void uninstallPredicate(String predicate, int arity);
	
	/**
	 * <p>Get external plugin module list</p>
	 * @return module list. Can be empty but not null
	 */
	<Global,Local> Iterable<IResolvable<Global,Local>> list();
}
