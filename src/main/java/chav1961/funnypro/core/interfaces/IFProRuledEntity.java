package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes any ruled entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProRuledEntity {
	/**
	 * <p>Get rule associated with the entity</p>
	 * @return rule associated or null if missing
	 */
	IFProEntity getRule();
	
	/**
	 * <p>Associate rule with the entity</p>
	 * @param rule rule description for the given entity
	 */
	void setRule(IFProEntity rule);
}
