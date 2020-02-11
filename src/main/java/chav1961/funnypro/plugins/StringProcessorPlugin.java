package chav1961.funnypro.plugins;


import java.io.IOException;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.entities.EnternalPluginEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.StackTopType;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.BoundStackTop;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IResolvable.ResolveRC;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.funnypro.pluginexample.TutorialPlugin;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

public class StringProcessorPlugin implements IResolvable<StringProcessorGlobal,StringProcessorLocal>, FProPluginList {
	public static final String		PLUGIN_NAME = "StringProcessorPlugin";
	public static final String		PLUGIN_DESCRIPTION = "This plugin supports a set of string predicates and operators";
	public static final String		PLUGIN_PRODUCER = "(c) 2017, Alexander V. Chernomyrdin aka chav1961";
	public static final int[]		PLUGIN_VERSION = {1,0};
	public static final char[]		PREDICATE_SPLIT = "split(String,Divizir,Items).".toCharArray(); 
	public static final char[]		PREDICATE_LIST = "list(String,List).".toCharArray(); 
	public static final char[]		OPERATOR_CHARARRAY = ":-op(700,xfx,=>..).".toCharArray();
	
	private long	splitId, listId, charArrayId;
	
	@Override
	public PluginDescriptor[] getPluginDescriptors() {
		return new PluginDescriptor[]{
				new PluginDescriptor(){
					@Override public IFProExternalEntity getPluginEntity() {return new EnternalPluginEntity(1,PLUGIN_NAME,PLUGIN_PRODUCER,PLUGIN_VERSION,new StringProcessorPlugin());}
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
		try(final LoggerFacade 				actualLog = debug.transaction("TutorialPlugin:onLoad")) {
			final StringProcessorGlobal		global = new StringProcessorGlobal(); 
			final IFProParserAndPrinter 	pap = new ParserAndPrinter(debug,parameters,repo);
			
			global.repo = repo;
			try{
				pap.parseEntities(PREDICATE_SPLIT,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							splitId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,StringProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_SPLIT)+" was registeded successfully");
				
				pap.parseEntities(PREDICATE_LIST,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							listId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,StringProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_LIST)+" was registeded successfully");
				
				pap.parseEntities(OPERATOR_CHARARRAY,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							listId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,StringProcessorPlugin.this,global);
							repo.putOperatorDef((IFProOperator)entity);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Operator "+new String(OPERATOR_CHARARRAY)+" was registeded successfully");
			} catch (SyntaxException | IOException exc) {
				actualLog.message(Severity.info,"Predicate registration failed for scanlist(List,Item).: %1$s", exc.getMessage());
				throw new IllegalArgumentException("Attempt to register predicate scanlist(List,Item) failed: "+exc.getMessage(),exc); 
			}
			actualLog.rollback();
			return global;
		}
	}
	
	@Override
	public void onRemove(final StringProcessorGlobal global) throws SyntaxException {
		global.repo.pluginsRepo().purgeResolver(this);
	}
	
	@Override
	public StringProcessorLocal beforeCall(final StringProcessorGlobal global, final IFProGlobalStack gs, final List<IFProVariable> vars, final IFProCallback callback) throws SyntaxException {
		if (global.collection.size() == 0) {		// Cache to reduce memory requirements
			global.collection.add(new StringProcessorLocal());
		}
		final StringProcessorLocal	result = global.collection.remove(0);	// Prepare local memory for the given call
		
		result.callback = callback;
		result.stack = gs;
		result.vars = vars;
		return result;
	}
	
	@Override
	public ResolveRC firstResolve(final StringProcessorGlobal global, final StringProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		if (entity.getEntityType() == EntityType.predicate) {
			if (entity.getEntityId() == splitId && ((IFProPredicate)entity).getArity() == 3) {
				
				
			}
			else if (entity.getEntityId() == listId && ((IFProPredicate)entity).getArity() == 3) {
				
				
			}
			else {
				return ResolveRC.False;
			}
		}
		else if (entity.getEntityType() == EntityType.operator && entity.getEntityId() == charArrayId && ((IFProOperator)entity).getOperatorType() == OperatorType.xfx) {
			final IFProOperator	temp = (IFProOperator) entity;
			
			if (temp.getLeft().getEntityType() == EntityType.anonymous || temp.getRight().getEntityType() == EntityType.anonymous) {
				return ResolveRC.True;
			}
			else if (temp.getRight().getEntityType() == EntityType.variable) {
				if (temp.getLeft().getEntityType() == EntityType.string) {
					final char[]	convert = new char[global.repo.stringRepo().getNameLength(temp.getLeft().getEntityId())];
					IFProList		list = new ListEntity(null, null);
					
					global.repo.stringRepo().getName(temp.getLeft().getEntityId(),convert,0);
					for (int index = convert.length-1; index >= 0; index--) {
						list = new ListEntity(new IntegerEntity(convert[index]),list);
					}
					if (FProUtil.unify(list,temp.getRight(),local.list)) {
						if (local.list[0] != null) {
							local.stack.push(GlobalStack.getBoundStackTop(entity,local.list[0]));
						}
						return ResolveRC.True;
					}
					else {
						if (local.list[0] != null) {
							FProUtil.unbind(local.list[0]);
						}
						return ResolveRC.False;
					}
				}
				else {
					return ResolveRC.False;
				}
			}
			else if (temp.getRight().getEntityType() == EntityType.list) {
				IFProEntity		cursor = temp.getRight();
				int				amount = 0;
				
				while (cursor != null && cursor.getEntityType() == EntityType.list) {
					if (((IFProList)cursor).getChild() != null) {
						if (((IFProList)cursor).getChild().getEntityType() == EntityType.integer) {
							amount++;
						}
						else {
							return ResolveRC.False;
						}
					}
					cursor = ((IFProList)cursor).getTail();
				}
				if (cursor != null) {
					return ResolveRC.False;
				}
				else {
					final char[]	data = new char[amount];
					
					cursor = temp.getRight(); 
					for (int index = 0; index < data.length; index++, cursor = ((IFProList)cursor).getTail()) {
						data[index] = (char) ((IFProList)cursor).getChild().getEntityId();
					}
					
					final StringEntity	se = new StringEntity(global.repo.stringRepo().placeName(data,0,data.length,null));
					
					if (FProUtil.unify(temp.getLeft(),se,local.list)) {
						if (local.list[0] != null) {
							local.stack.push(GlobalStack.getBoundStackTop(entity,local.list[0]));
						}
						return ResolveRC.True;
					}
					else {
						if (local.list[0] != null) {
							FProUtil.unbind(local.list[0]);
						}
						return ResolveRC.False;
					}
				}
			}
			else {
				return ResolveRC.False;
			}
		}
		return ResolveRC.False;
	}
	
	@Override
	public ResolveRC nextResolve(final StringProcessorGlobal global, final StringProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		if (entity.getEntityType() == EntityType.predicate) {
			if (entity.getEntityId() == splitId && ((IFProPredicate)entity).getArity() == 3) {
				
				
			}
			else if (entity.getEntityId() == listId && ((IFProPredicate)entity).getArity() == 3) {
				
				
			}
			else {
				return ResolveRC.False;
			}
		}
		else if (entity.getEntityType() == EntityType.operator && entity.getEntityId() == charArrayId && ((IFProOperator)entity).getOperatorType() == OperatorType.xfx) {
			if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)local.stack.peek()).getMark() == entity) {
				FProUtil.unbind(((BoundStackTop<FProUtil.Change>)local.stack.pop()).getChangeChain());
			}
			return ResolveRC.False;
		}
		return ResolveRC.False;
	}
	
	@Override
	public void endResolve(final StringProcessorGlobal global, final StringProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)local.stack.peek()).getMark() == entity) {
			FProUtil.unbind(((BoundStackTop<FProUtil.Change>)local.stack.pop()).getChangeChain());
		}
	}
	
	@Override
	public void afterCall(final StringProcessorGlobal global, final StringProcessorLocal local) throws SyntaxException {
		global.collection.add(local);
	} 
}