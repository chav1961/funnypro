package chav1961.funnypro.core;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;

import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo;
import chav1961.funnypro.core.interfaces.IFProModule;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.DottedVersion;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

class ExternalPluginsRepo implements IFProExternalPluginsRepo, IFProModule {
	public static final String						PLUGIN_PACKAGES = "pluginPackages";
	
	private final LoggerFacade						log;
	private final SubstitutableProperties			props;
	private final Map<IFProExternalEntity<?,?>,List<PluginItem>>	plugins = new HashMap<>();
	private final List<ExternalEntityDescriptor<?>>	operators = new ArrayList<>();
	private final List<ExternalEntityDescriptor<?>>	predicates = new ArrayList<>();

	public ExternalPluginsRepo(final LoggerFacade log, final SubstitutableProperties prop) throws IOException, NullPointerException {
		if (log == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (prop == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else {
			this.log = log;
			this.props = prop;
			
			final boolean	standardResolverOnly = prop.getProperty(IFProVM.PROP_DONT_LOAD_ALL_PLUGINS, boolean.class, "false"); 
			
			for (FProPluginList 				desc : ServiceLoader.load(FProPluginList.class)) {
				if (!standardResolverOnly || (desc instanceof StandardResolver)) {
					for (PluginDescriptor				item : desc.getPluginDescriptors()) {
						final IFProExternalEntity<?,?>	key = item.getPluginEntity();
						
						if (!plugins.containsKey(key)) {
							plugins.put(key,new ArrayList<PluginItem>());
						}
						plugins.get(key).add(new PluginItemImpl(item, null));
					}
				}
			}
		}
	}

	@Override public LoggerFacade getDebug() {return log;}
	@Override public SubstitutableProperties getParameters() {return props;}		

	@Override 
	public void close() throws RuntimeException {
		for (List<PluginItem> desc : plugins.values()) {
			for (PluginItem item : desc) {
				final Object	global = item.getGlobal();
				
				if (global != null) {
					try{
						item.getDescriptor().getPluginEntity().getResolver().onRemove(global);
					} catch (SyntaxException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void prepare(final IFProEntitiesRepo repo) {
		if (repo == null) {
			throw new IllegalArgumentException("Entites repo can't be null"); 
		}
		else {
			final Set<IFProExternalEntity<?, ?>>	toRemove = new HashSet<>();
			boolean	errosDetected = false;
			boolean	standardResolverDetected = false;
			
			try (final LoggerFacade	logger = getDebug().transaction("externalPlugins")) {
				for (Entry<IFProExternalEntity<?, ?>, List<PluginItem>> desc : plugins.entrySet()) {
					for (PluginItem item : desc.getValue()) {	// Standard resolver must be always prepared first!
						if (StandardResolver.PLUGIN_DESCRIPTION.equals(item.getDescriptor().getPluginDescription())) {
							try{item.setGlobal(item.getDescriptor().getPluginEntity().getResolver().onLoad(getDebug(), getParameters(), repo));
								standardResolverDetected = true;
							} catch (SyntaxException e) {
								logger.message(Severity.warning,e,"Error preparing plugin item [%1$s] for plugin [%2$s]: %3$s", item.getDescriptor().getPluginPredicate(), item.getDescriptor().getPluginDescription(), e.getLocalizedMessage());
								errosDetected = true;
							}
						}
					}
				}
				if (!standardResolverDetected) {
					logger.message(Severity.error,"Standard resolver is missing or inaccessible in the plugins. FPro repository can't be properly prepared without it!");
				}
				for (Entry<IFProExternalEntity<?, ?>, List<PluginItem>> desc : plugins.entrySet()) {
					for (PluginItem item : desc.getValue()) {
						if (!StandardResolver.PLUGIN_DESCRIPTION.equals(item.getDescriptor().getPluginDescription())) {
							try{
								item.setGlobal(item.getDescriptor().getPluginEntity().getResolver().onLoad(getDebug(),getParameters(),repo));
							} catch (PreparationException e) {
								logger.message(Severity.warning,e,"Error preparing plugin item [%1$s] for plugin [%2$s]: %3$s", item.getDescriptor().getPluginPredicate(), item.getDescriptor().getPluginDescription(), e.getLocalizedMessage());
								toRemove.add(desc.getKey());
							} catch (SyntaxException  e) {
								logger.message(Severity.warning,e,"Error preparing plugin item [%1$s] for plugin [%2$s]: %3$s", item.getDescriptor().getPluginPredicate(), item.getDescriptor().getPluginDescription(), e.getLocalizedMessage());
								errosDetected = true;
							}
						}
					}
				}
				for(IFProExternalEntity<?, ?> item : toRemove) {
					plugins.remove(item);
				}
				if (!errosDetected) {
					logger.rollback();
				}
			}
		}
	}
	
	@Override
	public Iterable<PluginItem> seek(final String pluginName, final String pluginProducer, final DottedVersion... pluginVersions) {
		final List<PluginItem>	collection = new ArrayList<PluginItem>();
		
		for (Entry<IFProExternalEntity<?,?>, List<PluginItem>> item : plugins.entrySet()) {
			if (equals(item.getKey().getPluginName(), pluginName) && equals(item.getKey().getPluginProducer(), pluginProducer) && inList(item.getKey().getPluginVersion(), pluginVersions)) {
				for (PluginItem element : item.getValue()) {
					collection.add(element);
				}
			}
		}
		return collection;
	}

	@Override
	public Iterable<PluginItem> allPlugins() {
		return seek(null, null);
	}

	@Override
	public <Global,Local> void registerResolver(final IFProEntity template, final List<IFProVariable> vars, final IResolvable<Global,Local> resolver, final Global global) {
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
			final ExternalEntityDescriptor<?>	desc = new ExternalEntityDescriptorImpl<>(template,vars,resolver,global);
			
			switch (template.getEntityType()) {
				case operatordef	:
					operators.add(desc);
					break;
				case predicate		:
					predicates.add(desc);
					break;
				default:
					throw new UnsupportedOperationException("Only operators and predicates are supported for external plugins"); 
			}
		}
	}

	@Override
	public <Global> ExternalEntityDescriptor<Global> getResolver(final IFProEntity template) {
		if (template == null) {
			throw new IllegalArgumentException("Template can't be null"); 
		}
		else {
			final long			idAwaited = template.getEntityId();
			
			switch (template.getEntityType()) {
				case operator 	:
					for (ExternalEntityDescriptor<?> item : operators) {
						if (item.getTemplate().getEntityId() == idAwaited && ((IFProOperator)template).getOperatorType() == ((IFProOperator)item.getTemplate()).getOperatorType()) {
							return (ExternalEntityDescriptor<Global>) item;
						}
					}
					break;
				case predicate 	:
					for (ExternalEntityDescriptor<?> item : predicates) {
						if (item.getTemplate().getEntityId() == idAwaited && ((IFProPredicate)template).getArity() == ((IFProPredicate)item.getTemplate()).getArity()) {
							return (ExternalEntityDescriptor<Global>) item;
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
	public <G,L> void purgeResolver(final IResolvable<G,L> resolver) {
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

	private static boolean equals(final String left, final String right) {
		return right == null || right.isEmpty() || left.equals(right);
	}
	
	private boolean inList(final DottedVersion pluginVersion, final DottedVersion[] pluginVersions) {
		if (pluginVersions == null || pluginVersions.length == 0) {
			return true;
		}
		else {
			for(DottedVersion item : pluginVersions) {
				if (item.equals(pluginVersion)) {
					return true;
				}
			}
			return false;
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
	
	private static class ExternalEntityDescriptorImpl<Global> implements ExternalEntityDescriptor<Global> {
		final IFProEntity 			template;
		final List<IFProVariable> 	vars;
		final IResolvable<Global,?>	resolver;
		final Global 				global;
		
		public ExternalEntityDescriptorImpl(final IFProEntity template, final List<IFProVariable> vars, final IResolvable<Global,?> resolver, final Global global) {
			this.template = template;
			this.vars = vars;
			this.resolver = resolver;
			this.global = global;
		}

		@Override public IFProEntity getTemplate() {return template;}
		@Override public List<IFProVariable> getVars() {return vars;}
		@Override public <Local> IResolvable<Global,Local> getResolver() {return (IResolvable<Global, Local>) resolver;}
		@Override public Global getGlobal() {return global;}

		@Override
		public String toString() {
			return "ExternalEntityDescriptorImpl [template=" + template + ", vars=" + vars + ", resolver=" + resolver + ", global=" + global + "]";
		}
	}
}
