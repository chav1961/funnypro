package chav1961.funnypro.core.interfaces;

import java.util.List;

/**
 * <p>This interface describes all external plugins in the system</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
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
	 * <p>The interface describes resolver for external entities</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	public interface ExternalEntityDescriptor {
		/**
		 * <p>Get entity template</p>
		 * @return emtoty template
		 */
		IFProEntity getTemplate();
		
		/**
		 * <p>Get variables list</p>
		 * @return variables list. Can be empty but ot null.
		 */
		List<IFProVariable> getVars();
		
		/**
		 * <p>Get resolver associated with the predicate</p>
		 * @return resolver associated
		 */
		<Global,Local> IResolvable<Global,Local> getResolver();
		
		/**
		 * <p>Get logbal object for the given resolver
		 * @return global object for resolver
		 */
		Object getGlobal();
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
	
	/**
	 * <p>GRegister resolver for the given entity</p>
	 * @param template entity template
	 * @param vars variables lost for the entity. Can be empty but not null
	 * @param resolver resolver for the entity
	 * @param global global object associated with the given resolver
	 */
	<Global,Local> void registerResolver(IFProEntity template, List<IFProVariable> vars, IResolvable<Global,Local> resolver, Object global);
	
	/**
	 * <p>Get resolver for the given template.</p>
	 * @param template template t get resolver for
	 * @return resolver associated or null if not exists
	 */
	ExternalEntityDescriptor getResolver(IFProEntity template);
	
	/**
	 * <p>Purge all resolver associations</p>
	 * @param resolver resolver to purge associations for
	 */
	<Global,Local> void purgeResolver(IResolvable<Global,Local> resolver);
}
