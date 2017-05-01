package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes any ruled entity</p>
 * @author chav1961
 */
public interface IFProRuledEntity {
	/**
	 * <p>Get rule associated with the entity</p>
	 * @return rule associated or null if missing
	 */
	IFProEntity getRule();
	
	/**
	 * <p>Associate rule with the entity</p>
	 * @param rule
	 */
	void setRule(IFProEntity rule);
}
