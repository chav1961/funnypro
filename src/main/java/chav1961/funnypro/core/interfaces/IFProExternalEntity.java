package chav1961.funnypro.core.interfaces;

import chav1961.purelib.basic.DottedVersion;

/**
 * <p>This interface describes FPro external entities</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProExternalEntity<Global,Local> extends IFProEntity {
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
	DottedVersion getPluginVersion();
	
	/**
	 * <p>Get resolver to process plugin rule.
	 * @return resolver to process plugin rule. Can't be null
	 */
	 IResolvable<Global,Local> getResolver();
}
