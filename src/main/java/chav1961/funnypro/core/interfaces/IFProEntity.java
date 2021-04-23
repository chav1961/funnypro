package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes FPro entities</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProEntity extends Cloneable {
	static final int		ANON_ENTITY_ID = -2; 
	static final int		LIST_ENTITY_ID = -3; 
	static final int		OPEN_ENTITY_ID = -4; 
	static final int		PARM_ENTITY_ID = -5; 

	/**
	 * <p>This enumerations describes FPro entity type:</p>
	 * <ul>
	 * <li>string - string term</li>
	 * <li>integer - integer term</li>
	 * <li>real - real term</li>
	 * <li>anonymous - anonymous variable</li>
	 * <li>variable - variable</li>
	 * <li>externalplugin - external plugin description</li>
	 * <li>list - list item</li>
	 * <li>operatordef - :- op/3</li>
	 * <li>operator - any operator</li>
	 * <li>predicate - any predicate</li>
	 * <li>any - any type. Used with {@link Predicate} annotation only</li>
	 * </ul>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	public enum EntityType {
		string, integer, real, anonymous, variable, externalplugin, 
		list, operatordef, operator, predicate, any
	}
	
	/**
	 * <p>Get entity type</p>
	 * @return entity type
	 */
	EntityType getEntityType();
	
	/**
	 * <p>Get entity id for the given entity type</p>
	 * @return entity id. Id semantic depends on the entity type
	 */
	long getEntityId();
	
	/**
	 * <p>Set entity id for the given entity type</p>
	 * @param entityId new id for the entity
	 * @return self
	 */
	IFProEntity setEntityId(long entityId);
	
	/**
	 * <p>Get parent for the given entity</p>
	 * @return parent for the given entity or null if missing. Can be invalid when entity is bounded during unification, but will be restored to valid state after unbind.
	 */
	IFProEntity getParent();
	
	/**
	 * <p>Set parent for the given entity</p>
	 * @param entity parent for the giveb entity
	 * @return self
	 */
	IFProEntity setParent(IFProEntity entity);
	
	/**
	 * <p>Is the entity ruled</p>
	 * @return true if yes
	 */
	default boolean isRuled() {
		return false;
	}
}
