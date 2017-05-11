package chav1961.funnypro.core;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo;
import chav1961.funnypro.core.interfaces.IFProModule;
import chav1961.purelib.basic.interfaces.LoggerFacade;

class ExternalPluginsRepo implements IFProExternalPluginsRepo, IFProModule {
	public static final String						PLUGIN_PACKAGES = "pluginPackages";
	
	private final LoggerFacade						log;
	private final Properties						props;
	private final Map<PluginKey,List<PluginItem>>	plugins = new HashMap<PluginKey,List<PluginItem>>();

	public ExternalPluginsRepo(final LoggerFacade log, final Properties prop) throws IOException {
		if (log == null) {
			throw new IllegalArgumentException("Log can't be null"); 
		}
		else if (prop == null) {
			throw new IllegalArgumentException("Properties can't be null"); 
		}
		else {
			this.log = log;				this.props = prop;
			for (String packName : (this.getClass().getPackage().getName()+';'+prop.getProperty(PLUGIN_PACKAGES,"")).split("\\;")) {
				if (packName != null && !packName.isEmpty()) {

					for (FProPluginList 		desc : ServiceLoader.load(FProPluginList.class)) {
						for (PluginDescriptor	item : desc.getPluginDescriptors()) {
							final PluginKey		key = new PluginKey(item.getPluginEntity().getPluginName(),item.getPluginEntity().getPluginProducer(),item.getPluginEntity().getPluginVersion());
							
							if (!plugins.containsKey(key)) {
								plugins.put(key,new ArrayList<PluginItem>());
							}
							plugins.get(key).add(new PluginItemImpl(item,null));
						}
					}
				}
			}
		}
	}

	@Override public LoggerFacade getDebug() {return log;}
	@Override public Properties getParameters() {return props;}		
	
	@Override
	public void close() throws Exception {
		for (List<PluginItem> desc : plugins.values()) {
			for (PluginItem item : desc) {
				item.getDescriptor().getPluginEntity().getResolver().onRemove(item.getGlobal());
			}
		}
	}

	@Override
	public void prepare(final IFProEntitiesRepo repo) {
		if (repo == null) {
			throw new IllegalArgumentException("Entites repo can't be null"); 
		}
		else {
			for (List<PluginItem> desc : plugins.values()) {
				for (PluginItem item : desc) {
					try{item.setGlobal(item.getDescriptor().getPluginEntity().getResolver().onLoad(getDebug(),getParameters(),repo));
					} catch (FProException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@Override
	public Iterable<PluginItem> seek(final String pluginName, final String pluginProducer, final int[] pluginVersion) {
		final List<PluginItem>	collection = new ArrayList<PluginItem>();
		
		for (Entry<PluginKey, List<PluginItem>> item : plugins.entrySet()) {
			if ((pluginName == null || pluginName.isEmpty() || item.getKey().getPluginName().equals(pluginName))
				&& (pluginProducer == null || pluginProducer.isEmpty() || item.getKey().getPluginProducer().equals(pluginProducer))
				&& (pluginVersion == null || pluginVersion.length == 0 || Arrays.equals(item.getKey().getPluginVersion(),pluginVersion))) {
				for (PluginItem element : item.getValue()) {
					collection.add(element);
				}
			}
		}
		return collection;
	}

	@Override
	public Iterable<PluginItem> allPlugins() {
		return seek(null,null,null);
	}
	
	private static class PluginKey {
		private final String	pluginName;
		private final String	pluginProducer;
		private final int[]		pluginVersion;
		
		public PluginKey(final String pluginName, final String pluginProducer, final int[] pluginVersion) {
			this.pluginName = pluginName;
			this.pluginProducer = pluginProducer;
			this.pluginVersion = pluginVersion;
		}

		@Override public String toString() {return "PluginKey [pluginName=" + pluginName + ", pluginProducer=" + pluginProducer + ", pluginVersion=" + Arrays.toString(pluginVersion) + "]";}

		public String getPluginName() {return pluginName;}
		public String getPluginProducer() {return pluginProducer;}
		public int[] getPluginVersion() {return pluginVersion;}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((pluginName == null) ? 0 : pluginName.hashCode());
			result = prime * result + ((pluginProducer == null) ? 0 : pluginProducer.hashCode());
			result = prime * result + Arrays.hashCode(pluginVersion);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PluginKey other = (PluginKey) obj;
			if (pluginName == null) {
				if (other.pluginName != null) return false;
			} else if (!pluginName.equals(other.pluginName)) return false;
			if (pluginProducer == null) {
				if (other.pluginProducer != null) return false;
			} else if (!pluginProducer.equals(other.pluginProducer)) return false;
			if (!Arrays.equals(pluginVersion, other.pluginVersion)) return false;
			return true;
		}

	}
	
	private static class PluginItemImpl implements PluginItem {
		private final PluginDescriptor		desc; 
		private Object 						global;
		
		public PluginItemImpl(final PluginDescriptor desc, final Object global) {
			this.desc = desc;
			this.global = global;
		}

		
		public PluginDescriptor getDescriptor(){return desc;} 
		public Object getGlobal(){return global;}
		public void setGlobal(final Object global){this.global = global;}

		@Override public String toString() {return "PluginItem [desc=" + desc + ", global=" + global + "]";}
	}
}
