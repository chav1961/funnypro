package chav1961.funnypro.core;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.entities.EnternalPluginEntity;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public abstract class AbstractExternalPlugin<Global extends GlobalDescriptor,Local extends LocalDescriptor, ProcessingType extends Enum<?>> implements IResolvable<Global,Local>, FProPluginList {
	private final String				pluginName;
	private final String				pluginProducer;
	private final String				pluginDescription;
	private final int[]					pluginVersion;
	private final PluginDescriptor[]	desc;
	
	protected AbstractExternalPlugin(final String pluginName, final String pluginProducer, final String pluginDescription, final int[] pluginVersion) {
		if (pluginName == null || pluginName.isEmpty()) {
			throw new IllegalArgumentException("Plugin name can;t be null or empty"); 
		}
		else if (pluginProducer == null || pluginProducer.isEmpty()) {
			throw new IllegalArgumentException("Plugin producer can't be null or empty"); 
		}
		else if (pluginDescription == null || pluginDescription.isEmpty()) {
			throw new IllegalArgumentException("Plugin description can't be null or empty"); 
		}
		else if (pluginVersion == null || pluginVersion.length != 3) {
			throw new IllegalArgumentException("Plugin version need be 3-number int"); 
		}
		else {
			this.pluginName = pluginName;
			this.pluginProducer = pluginProducer;
			this.pluginDescription = pluginDescription;
			this.pluginVersion = pluginVersion;
			this.desc = new PluginDescriptor[]{
					new PluginDescriptor(){
						@Override public IFProExternalEntity getPluginEntity() {return new EnternalPluginEntity(1,pluginName,pluginProducer,pluginVersion,AbstractExternalPlugin.this);}
						@Override public String getPluginPredicate() {return null;}
						@Override public String getPluginDescription() {return pluginDescription;}
					}
			};
					
		}
	}

	protected abstract Global createGlobalMemory();
	protected abstract void freeGlobalMemory(final Global memory);
	
	protected abstract Local createLocalMemory();
	protected abstract void freeLocalMemory(final Local memory);
	
	@Override public PluginDescriptor[] getPluginDescriptors() {return desc;}
	@Override public String getName() {return pluginName;}
	@Override public int[] getVersion() {return pluginVersion;}

	@Override
	public Global onLoad(final LoggerFacade debug, final Properties parameters, final IFProEntitiesRepo repo) throws SyntaxException {
		return null;
	}

	@Override
	public void onRemove(final Global global) throws SyntaxException {
	}

	@Override
	public Local beforeCall(final Global global, final IFProGlobalStack gs, final List<IFProVariable> vars, final IFProCallback callback) throws SyntaxException {
		return null;
	}

	@Override
	public ResolveRC firstResolve(final Global global, final Local local, final IFProEntity entity) throws SyntaxException {
		return null;
	}

	@Override
	public ResolveRC nextResolve(final Global global, final Local local, final IFProEntity entity) throws SyntaxException {
		return null;
	}

	@Override
	public void endResolve(final Global global, final Local local, final IFProEntity entity) throws SyntaxException {
	}

	@Override
	public void afterCall(final Global global, final Local local) throws SyntaxException {
	}

	protected ResolveRC unify(final IFProEntity first, final IFProEntity second, final Global global, final Local local, final IFProEntity entity) {
		return null;
	}
}
