package chav1961.funnypro.plugins;

import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class AdvancedResolverPlugin implements IResolvable<StringProcessorGlobal,StringProcessorLocal>, FProPluginList {
	public static final String		PLUGIN_NAME = "AdvancedResolverPlugin";
	public static final String		PLUGIN_DESCRIPTION = "This plugin supports advanced set of predicates to manage Funny prolog fact/rule base";
	public static final String		PLUGIN_PRODUCER = "(c) 2017, Alexander V. Chernomyrdin aka chav1961";
	public static final int[]		PLUGIN_VERSION = {1,0};

	@Override
	public PluginDescriptor[] getPluginDescriptors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringProcessorGlobal onLoad(LoggerFacade debug, Properties parameters, IFProEntitiesRepo repo) throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onRemove(StringProcessorGlobal global) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StringProcessorLocal beforeCall(StringProcessorGlobal global, IFProGlobalStack gs, List<IFProVariable> vars, IFProCallback callback) throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResolveRC firstResolve(StringProcessorGlobal global, StringProcessorLocal local, IFProEntity entity) throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResolveRC nextResolve(StringProcessorGlobal global, StringProcessorLocal local, IFProEntity entity) throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void endResolve(StringProcessorGlobal global, StringProcessorLocal local, IFProEntity entity) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCall(StringProcessorGlobal global, StringProcessorLocal local) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}

}
