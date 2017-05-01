package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes FPro entities</p>
 * @author chav1961
 *
 */
public interface IFProEntity {
	static final int		ANON_ENTITY_ID = -2; 
	static final int		LIST_ENTITY_ID = -3; 
	static final int		OPEN_ENTITY_ID = -4; 
	static final int		PARM_ENTITY_ID = -5; 

	public enum EntityType {
		string, integer, real, anonymous, variable, externalplugin, 
		list, operatordef, operator, predicate
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
	 * @param entityId
	 * @return self
	 */
	IFProEntity setEntityId(long entityId);
	
	/**
	 * <p>Get parent for the given entity</p>
	 * @return parent for the given entity or null if missing. Can be invalid when entity is bounded durung unification, but will be restored to valid state after unbind.
	 */
	IFProEntity getParent();
	
	/**
	 * <p>Set parent for the given entity</p>
	 * @param entity parent for the giveb entity
	 * @return self
	 */
	IFProEntity setParent(IFProEntity entity);
}
