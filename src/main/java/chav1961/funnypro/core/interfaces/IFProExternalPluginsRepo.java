package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes all external plugins in the system</p> 
 * @author chav1961
 *
 */
public interface IFProExternalPluginsRepo extends AutoCloseable {
	/**
	 * <p>This interface describes one external plugin in the system</p>
	 * @author chav1961
	 */
	public interface PluginDescriptor {
		/**
		 * <p>Get plugin entity to call it</p>
		 * @return plugin entity
		 */
		IFProExternalEntity getPluginEntity();
		
		/**
		 * <p>Get plugin predicate form to install the plugin</p>
		 * @return syntactically corect plugin predicate. Can't be null
		 */
		String getPluginPredicate();
		
		/**
		 * <p>Get plugin description.
		 * @return Plugin description. Can be null
		 */
		String getPluginDescription();
	}
	
	/**
	 * <p>This interface describes one external plugin item in the system</p>
	 * @author chav1961
	 */
	public interface PluginItem {
		/**
		 * <p>Get plugin descriptor</p>
		 * @return plugin descriptor
		 */
		PluginDescriptor getDescriptor();
		
		/**
		 * <p>Get global object associated with the plugin</p>
		 * @return Global object associated. Can be null
		 */
		Object getGlobal();
		
		/**
		 * <p>Associate global object with the plugin</p>
		 * @param global global object associated
		 */
		void setGlobal(Object global);
	}
	
	/**
	 * <p>Prepare all plugins to work with repo</p>
	 * @param repo entites repo to use in the plugins
	 */
	void prepare(IFProEntitiesRepo repo);
	
	/**
	 * <p>Seek plugin description by it's parameters</p> 
	 * @param pluginName plugin name to seek. Null or empty means 'any'
	 * @param pluginProducer plugin produces to seek. Null or empty means 'any'
	 * @param pluginVersion plugin version to seek. Null or empty array means 'any'.
	 * @return plugin descriptors was found. Can be empty but not null
	 */
	Iterable<PluginItem> seek(String pluginName, String pluginProducer, int[] pluginVersion);

	/**
	 * <p>Get all plugins in the system</p>
	 * @return list of all plugins in the system. Can be empty but not null 
	 */
	Iterable<PluginItem> allPlugins();
}
