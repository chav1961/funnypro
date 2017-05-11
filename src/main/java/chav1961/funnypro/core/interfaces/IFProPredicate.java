package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes predicate definitions</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProPredicate extends IFProEntity, IFProRuledEntity {
	public static final int		MIN_ARITY = 0;
	public static final int		MAX_ARITY = 64;
	
	/**
	 * <p>Get predicate arity</p>
	 * @return predicate arity
	 */
	int getArity();
	
	/**
	 * <p>Get predicate parameters</p>
	 * @return predicate parameters
	 */
	IFProEntity[] getParameters();
	
	/**
	 * <p>Set predicate parameters</p>
	 * @param entity predicate parametetrs
	 */
	void setParameters(final IFProEntity... entity);
}
