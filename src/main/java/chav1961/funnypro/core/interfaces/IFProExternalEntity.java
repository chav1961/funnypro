package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes FPro external entities</p>
 * @author chav1961
 *
 */
public interface IFProExternalEntity extends IFProEntity {
	/**
	 * <p>Get plugin name for the given entity</p>
	 * @return plugin name. Can't be null
	 */
	String getPluginName();
	
	/**
	 * <p>Get plugin producer for the given entity</p>
	 * @return plugin producer. Can't be null
	 */
	String getPluginProducer();
	
	/**
	 * <p>Get plugin version for the given entity</p>
	 * @return plugin version for the given entity. Can't be null
	 */
	int[] getPluginVersion();
	
	/**
	 * <p>Get resolver to process plugin rule.
	 * @return resolver to process plugin rule. Can't be null
	 */
	IResolvable getResolver();
}
