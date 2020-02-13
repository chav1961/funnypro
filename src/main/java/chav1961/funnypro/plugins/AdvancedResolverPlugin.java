package chav1961.funnypro.plugins;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.entities.EnternalPluginEntity;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public class AdvancedResolverPlugin implements IResolvable<StringProcessorGlobal,StringProcessorLocal>, FProPluginList {
	public static final String		PLUGIN_NAME = "AdvancedResolverPlugin";
	public static final String		PLUGIN_DESCRIPTION = "This plugin supports advanced set of predicates to manage Funny prolog fact/rule base";
	public static final String		PLUGIN_PRODUCER = "(c) 2020, Alexander V. Chernomyrdin aka chav1961";
	public static final int[]		PLUGIN_VERSION = {1,0};

	public static final char[]		PREDICATE_SPLIT = "split(String,Divizir,Items).".toCharArray(); 
	public static final char[]		PREDICATE_LIST = "list(String,List).".toCharArray(); 
	public static final char[]		OPERATOR_CHARARRAY = ":-op(700,xfx,=>..).".toCharArray();

	// abolish(Name/Arity) - remove all
	// list(Name/Arity) - list base content
	// arg(
	
	private long	splitId, listId, charArrayId;
	
	@Override
	public PluginDescriptor[] getPluginDescriptors() {
		return new PluginDescriptor[]{
				new PluginDescriptor(){
					@Override public IFProExternalEntity getPluginEntity() {return new EnternalPluginEntity(2,PLUGIN_NAME,PLUGIN_PRODUCER,PLUGIN_VERSION,new StringProcessorPlugin());}
					@Override public String getPluginPredicate() {return null;}
					@Override public String getPluginDescription() {return PLUGIN_DESCRIPTION;}
				}
		};
	}
	
	@Override
	public String getName() {
		return PLUGIN_NAME;
	}
	
	@Override
	public int[] getVersion() {
		return PLUGIN_VERSION;
	}
	
	@Override
	public StringProcessorGlobal onLoad(final LoggerFacade debug, final Properties parameters, final IFProEntitiesRepo repo) throws SyntaxException {
		// TODO Auto-generated method stub
		try(final LoggerFacade 				actualLog = debug.transaction("TutorialPlugin:onLoad")) {
			final StringProcessorGlobal		global = new StringProcessorGlobal(); 
			final IFProParserAndPrinter 	pap = new ParserAndPrinter(debug,parameters,repo);
			
			global.repo = repo;
			try{
				pap.parseEntities(PREDICATE_SPLIT,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							splitId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,AdvancedResolverPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_SPLIT)+" was registeded successfully");
				
			} catch (SyntaxException | IOException exc) {
				actualLog.message(Severity.info,"Predicate registration failed for scanlist(List,Item).: %1$s", exc.getMessage());
				throw new IllegalArgumentException("Attempt to register predicate scanlist(List,Item) failed: "+exc.getMessage(),exc); 
			}
			actualLog.rollback();
			return global;
		}
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
