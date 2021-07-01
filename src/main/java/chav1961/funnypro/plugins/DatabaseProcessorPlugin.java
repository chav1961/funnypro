package chav1961.funnypro.plugins;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.FProUtil.ContentType;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.entities.ExternalPluginEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.BoundStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.StackTopType;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.TemporaryStackTop;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;

public class DatabaseProcessorPlugin implements IResolvable<DatabaseProcessorGlobal,DatabaseProcessorLocal>, FProPluginList {
	public static final String		PLUGIN_NAME = "DatabaseProcessorPlugin";
	public static final String		PLUGIN_DESCRIPTION = "This plugin supports a set of database predicates and operators";
	public static final String		PLUGIN_PRODUCER = "(c) 2021, Alexander V. Chernomyrdin aka chav1961";
	public static final int[]		PLUGIN_VERSION = {1,0};
	public static final char[]		PREDICATE_SPLIT = "split(String,Divizor,List).".toCharArray(); 
	public static final char[]		PREDICATE_LIST = "inList(String,List).".toCharArray(); 
	public static final char[]		OPERATOR_CHARARRAY = ":-op(700,xfx,=>..).".toCharArray();
	
	private long	splitId, listId, charArrayId;
	
	@Override
	public PluginDescriptor[] getPluginDescriptors() {
		return new PluginDescriptor[]{
				new PluginDescriptor(){
					@Override public IFProExternalEntity getPluginEntity() {return new ExternalPluginEntity(1,PLUGIN_NAME,PLUGIN_PRODUCER,PLUGIN_VERSION,DatabaseProcessorPlugin.this);}
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
	public DatabaseProcessorGlobal onLoad(final LoggerFacade debug, final Properties parameters, final IFProEntitiesRepo repo) throws SyntaxException {
		try(final LoggerFacade 				actualLog = debug.transaction("StringProcesorPlugin:onLoad")) {
			final DatabaseProcessorGlobal	global = new DatabaseProcessorGlobal(); 
			final IFProParserAndPrinter 	pap = new ParserAndPrinter(debug,parameters,repo);
			
			global.repo = repo;
			global.pap = pap;
			try{
				pap.parseEntities(PREDICATE_SPLIT,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							splitId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,DatabaseProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_SPLIT)+" was registeded successfully");
				
				pap.parseEntities(PREDICATE_LIST,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							listId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,DatabaseProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_LIST)+" was registeded successfully");
				
				pap.parseEntities(OPERATOR_CHARARRAY,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							final IFProOperator	op = ((IFProOperator)((IFProOperator)entity).getRight());
							
							charArrayId = op.getEntityId();
							repo.pluginsRepo().registerResolver(op,vars,DatabaseProcessorPlugin.this,global);
							repo.putOperatorDef(op);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Operator "+new String(OPERATOR_CHARARRAY)+" was registeded successfully");
			} catch (SyntaxException | IOException exc) {
				exc.printStackTrace();
				actualLog.message(Severity.info,"Predicate registration failed: %1$s", exc.getMessage());
				throw new IllegalArgumentException("Attempt to register predicate/operator failed: "+exc.getMessage(),exc); 
			}
			actualLog.rollback();
			return global;
		}
	}
	
	@Override
	public void onRemove(final DatabaseProcessorGlobal global) throws SyntaxException {
		global.repo.pluginsRepo().purgeResolver(this);
	}
	
	@Override
	public DatabaseProcessorLocal beforeCall(final DatabaseProcessorGlobal global, final IFProGlobalStack gs, final List<IFProVariable> vars, final IFProCallback callback) throws SyntaxException {
		if (global.collection.size() == 0) {		// Cache to reduce memory requirements
			global.collection.add(new DatabaseProcessorLocal());
		}
		final DatabaseProcessorLocal	result = global.collection.remove(0);	// Prepare local memory for the given call
		
		result.callback = callback;
		result.stack = gs;
		result.vars = vars;
		return result;
	}
	
	@Override
	public ResolveRC firstResolve(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		return ResolveRC.False;
	}
	
	@Override
	public ResolveRC nextResolve(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		return ResolveRC.False;
	}
	
	@Override
	public void endResolve(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity entity) throws SyntaxException {
	}
	
	@Override
	public void afterCall(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local) throws SyntaxException {
		global.collection.add(local);
	} 
}