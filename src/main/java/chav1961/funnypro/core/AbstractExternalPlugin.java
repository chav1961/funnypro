package chav1961.funnypro.core;

import java.util.List;

import chav1961.funnypro.core.entities.ExternalPluginEntity;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.DottedVersion;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public abstract class AbstractExternalPlugin<Global,Local> implements IResolvable<Global,Local>, FProPluginList {
	private final String				pluginName;
	private final DottedVersion			pluginVersion;
	private final PluginDescriptor[]	desc;
	
	protected AbstractExternalPlugin(final String pluginName, final String pluginProducer, final String pluginDescription, final DottedVersion pluginVersion) {
		if (Utils.checkEmptyOrNullString(pluginName)) {
			throw new IllegalArgumentException("Plugin name can;t be null or empty"); 
		}
		else if (Utils.checkEmptyOrNullString(pluginProducer)) {
			throw new IllegalArgumentException("Plugin producer can't be null or empty"); 
		}
		else if (Utils.checkEmptyOrNullString(pluginDescription)) {
			throw new IllegalArgumentException("Plugin description can't be null or empty"); 
		}
		else if (pluginVersion == null) {
			throw new NullPointerException("Plugin version can't be null"); 
		}
		else {
			final ExternalPluginEntity<Global, Local>	entity = new ExternalPluginEntity<Global, Local>(1, pluginName, pluginProducer, pluginVersion, AbstractExternalPlugin.this); 
			
			this.pluginName = pluginName;
			this.pluginVersion = pluginVersion;
			this.desc = new PluginDescriptor[]{
					new PluginDescriptor(){
						@SuppressWarnings("unchecked")
						@Override public IFProExternalEntity<Global, Local> getPluginEntity() {return entity;}
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
	@Override public DottedVersion getVersion() {return pluginVersion;}

	@Override
	public Global onLoad(final LoggerFacade debug, final SubstitutableProperties parameters, final IFProEntitiesRepo repo) throws SyntaxException {
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
		return ResolveRC.FalseWithoutBacktracking;
	}

	@Override
	public ResolveRC nextResolve(final Global global, final Local local, final IFProEntity entity) throws SyntaxException {
		return ResolveRC.False;
	}

	@Override
	public void endResolve(final Global global, final Local local, final IFProEntity entity) throws SyntaxException {
	}

	@Override
	public void afterCall(final Global global, final Local local) throws SyntaxException {
	}

	protected ResolveRC unify(final IFProEntity first, final IFProEntity second, final Global global, final Local local, final IFProEntity entity) {
		return ResolveRC.False;
	}
}
