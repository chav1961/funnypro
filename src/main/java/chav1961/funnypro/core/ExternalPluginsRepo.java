package chav1961.funnypro.core;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo;
import chav1961.funnypro.core.interfaces.IFProModule;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.interfaces.LoggerFacade;

class ExternalPluginsRepo implements IFProExternalPluginsRepo, IFProModule {
	public static final String						PLUGIN_PACKAGES = "pluginPackages";
	
	private final LoggerFacade						log;
	private final Properties						props;
	private final Map<PluginKey,List<PluginItem>>	plugins = new HashMap<>();
	private final List<ExternalEntityDescriptor>	operators = new ArrayList<>();
	private final List<ExternalEntityDescriptor>	predicates = new ArrayList<>();

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

	@Override
	public <Global,Local> void registerResolver(final IFProEntity template, final List<IFProVariable> vars, final IResolvable<Global,Local> resolver, final Object global) {
		if (template == null) {
			throw new IllegalArgumentException("Template can't be null"); 
		}
		else if (vars == null) {
			throw new IllegalArgumentException("Variables list can't be null"); 
		}
		else if (resolver == null) {
			throw new IllegalArgumentException("resolver can't be null"); 
		}
		else {
			switch (template.getEntityType()) {
				case operator	:
					operators.add(new ExternalEntityDescriptorImpl(template,vars,resolver,global));
					break;
				case predicate	:
					predicates.add(new ExternalEntityDescriptorImpl(template,vars,resolver,global));
					break;
				default:
					throw new UnsupportedOperationException("Only operators and predicates are supported for external plugins"); 
			}
		}
	}

	@Override
	public ExternalEntityDescriptor getResolver(final IFProEntity template) {
		if (template == null) {
			throw new IllegalArgumentException("Template can't be null"); 
		}
		else {
			final long			idAwaited = template.getEntityId();
			
			switch (template.getEntityType()) {
				case operator 	:
					for (ExternalEntityDescriptor item : operators) {
						if (item.getTemplate().getEntityId() == idAwaited) {
							if (((IFProOperator)template).getOperatorType() == ((IFProOperator)item.getTemplate()).getOperatorType()) {
								return item;
							}
						}
					}
					break;
				case predicate 	:
					for (ExternalEntityDescriptor item : predicates) {
						if (item.getTemplate().getEntityId() == idAwaited && ((IFProPredicate)template).getArity() == ((IFProPredicate)item.getTemplate()).getArity()) {
							return item;
						}
					}
					break;
				default:
					throw new UnsupportedOperationException("Only operators and predicates are supported for external plugins"); 
			}
			return null;
		}
	}

	@Override
	public void purgeResolver(final IResolvable resolver) {
		if (resolver == null) {
			throw new IllegalArgumentException("resolver can't be null"); 
		}
		else {
			for (int index = operators.size()-1; index >= 0; index--) {
				if (operators.get(index).getResolver() == resolver) {
					operators.remove(index);
				}
			}
			for (int index = predicates.size()-1; index >= 0; index--) {
				if (predicates.get(index).getResolver() == resolver) {
					predicates.remove(index);
				}
			}
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
	
	private static class ExternalEntityDescriptorImpl implements ExternalEntityDescriptor {
		final IFProEntity 			template;
		final List<IFProVariable> 	vars;
		final IResolvable 			resolver;
		final Object 				global;
		
		public ExternalEntityDescriptorImpl(final IFProEntity template, final List<IFProVariable> vars, final IResolvable resolver, final Object global) {
			this.template = template;
			this.vars = vars;
			this.resolver = resolver;
			this.global = global;
		}

		@Override public IFProEntity getTemplate() {return template;}
		@Override public List<IFProVariable> getVars() {return vars;}
		@Override public IResolvable getResolver() {return resolver;}
		@Override public Object getGlobal() {return global;}

		@Override
		public String toString() {
			return "ExternalEntityDescriptorImpl [template=" + template + ", vars=" + vars + ", resolver=" + resolver + ", global=" + global + "]";
		}
	}
}
