package chav1961.funnypro.core.interfaces;

import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;

/**
 * <p>This interface describes descriptor of external plugins in the system</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface FProPluginList {
	/**
	 * <p>Get plugin list for the given module</p>
	 * @return plugin list. Can be empty but not null
	 */
	PluginDescriptor[] getPluginDescriptors();
}