package chav1961.funnypro.pluginexample;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.entities.ExternalPluginEntity;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.BoundStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.StackTopType;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.DottedVersion;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

/**
 * <p>This class is a tutorial class for plugin writers. This class implements 'tutorial' predicate:</p>
 * <code>
 * scanlist([list|_],Item).
 * </code>
 * <p>This predicate iterates on the list content by backtracking and returns first list element, second list element, third list element etc.</p>
 * <p>See comments inside Java code for description of the work. Also see content of the META-INF/services/chav1961.funnypro.core.interfaces.FProPluginList file</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class TutorialPlugin implements IResolvable<MyOwnMemory,ListEntityIndex>, FProPluginList {
	public static final String			PLUGIN_NAME = "TutorialPlugin";
	public static final String			PLUGIN_DESCRIPTION = "This is a tutorial plugin for Funny Prolog plugin developers";
	public static final String			PLUGIN_PRODUCER = "Vassya Pupkind";
	public static final DottedVersion	PLUGIN_VERSION = DottedVersion.ZERO;
	public static final char[]			PREDICATE = "scanlist(List,Item).".toCharArray(); 

	private long		scanlistId;
	
	@Override
	public PluginDescriptor[] getPluginDescriptors() {
		return new PluginDescriptor[]{
				new PluginDescriptor(){
					@Override public IFProExternalEntity<?,?> getPluginEntity() {return new ExternalPluginEntity(1, PLUGIN_NAME, PLUGIN_PRODUCER, PLUGIN_VERSION, new TutorialPlugin());}
					@Override public String getPluginPredicate() {return new String(PREDICATE);}
					@Override public String getPluginDescription() {return PLUGIN_DESCRIPTION;}
				}
		};
	}

	@Override
	public String getName() {
		return PLUGIN_NAME;
	}

	@Override
	public DottedVersion getVersion() {
		return PLUGIN_VERSION;
	}

	@Override
	public MyOwnMemory onLoad(final LoggerFacade debug, final SubstitutableProperties parameters, final IFProEntitiesRepo repo) throws SyntaxException {
		try(final LoggerFacade 				actualLog = debug.transaction("TutorialPlugin:onLoad")) {
			final MyOwnMemory				global = new MyOwnMemory(); 	// allocate global memory for my plugin
			final IFProParserAndPrinter 	pap = new ParserAndPrinter(debug, parameters, repo);
			
			global.repo = repo;
			try{
				pap.parseEntities(PREDICATE, 0, (entity, vars) -> {
					scanlistId = entity.getEntityId();	// Can be useful when you process more than one predicate/operaotr.
					repo.pluginsRepo().registerResolver(entity, vars, TutorialPlugin.this, global);	// Register resolver for this external predicate
					return true;
				});
				
				actualLog.message(Severity.info, "Predicate scanlist(List,Item) was registeded successfully");
				actualLog.rollback();	// See Pure library LoggerFacade description about transactional logging
				return global;
			} catch (SyntaxException | IOException exc) {
				actualLog.message(Severity.info,exc, "Predicate registration failed for scanlist(List,Item).: %1$s", exc.getLocalizedMessage());
				throw new IllegalArgumentException("Attempt to register predicate scanlist(List,Item) failed: "+exc.getLocalizedMessage(),exc); 
			}
		}
	}

	@Override
	public void onRemove(final MyOwnMemory global) throws SyntaxException {
		global.collection.clear();
		global.repo.pluginsRepo().purgeResolver(this);
	}

	@Override
	public ListEntityIndex beforeCall(final MyOwnMemory global, final IFProGlobalStack gs, final List<IFProVariable> vars, final IFProCallback callback) throws SyntaxException {
		if (global.collection.size() == 0) {				// Cache to reduce memory requirements
			global.collection.add(new ListEntityIndex());
		}
		final ListEntityIndex	result = global.collection.remove(0);	// Prepare local memory for the given call
		
		result.callback = callback;
		result.stack = gs;
		result.vars = vars;
		return result;
	}

	@Override
	public ResolveRC firstResolve(final MyOwnMemory global, final ListEntityIndex local, final IFProEntity entity) throws SyntaxException {
		if (entity.getEntityType() == EntityType.predicate	// Guard check - entity is predicate 
			&& entity.getEntityId() == scanlistId 			// Entity is 'scanlist' predicate (see onLoad(...) where we got this Id)
			&& ((IFProPredicate)entity).getArity() == 2) {	// Entity has exactly 2 parameters
			
			if (((IFProPredicate)entity).getParameters()[0].getEntityType() == EntityType.list) {	// The first parameter is list - continue
				// Save the same first list item to iterate in the backtracking
				local.currentItem = ((IFProList)((IFProPredicate)entity).getParameters()[0]);
				
				// Unify the same first list item and second parameter
				if (FProUtil.unify(local.currentItem.getChild(), ((IFProPredicate)entity).getParameters()[1], local.list)) {
					if (local.list[0] != null) {	// Unification successful and some variables bounded - save rollback data for backtracking
						local.stack.push(GlobalStack.getBoundStackTop(entity, entity, local.list[0]));
					}
					return ResolveRC.True;
				}
				else {	// Unification failed - rollback changes if they were.
					if (local.list[0] != null) {
						FProUtil.unbind(local.list[0]);
					}
					return ResolveRC.False;
				}
			}
			else { // The first parameter is not list - unification failed
				return ResolveRC.False; 
			}
		}
		else {	// Guard check failed...
			return ResolveRC.False; 
		}
	}

	@Override
	public ResolveRC nextResolve(final MyOwnMemory global, final ListEntityIndex local, final IFProEntity entity) throws SyntaxException {
		if (entity.getEntityType() == EntityType.predicate	// Guard check - entity is predicate 
				&& entity.getEntityId() == scanlistId 			// Entity is 'scanlist' predicate (see onLoad(...) where we got this Id)
				&& ((IFProPredicate)entity).getArity() == 2) {	// Entity has exactly 2 parameters

				// rollback changes form the previous unifications, if they were
				if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop<?>)local.stack.peek()).getMark() == entity) {
					FProUtil.unbind(((BoundStackTop<FProUtil.Change>)local.stack.pop()).getChangeChain());
				}

				// Goto next element in the list
				if (local.currentItem.getTail() != null && local.currentItem.getTail().getEntityType() == EntityType.list) {
					local.currentItem = (IFProList) local.currentItem.getTail();

					// This code duplicates the same one from firstResolve method
					if (FProUtil.unify(local.currentItem.getChild(),((IFProPredicate)entity).getParameters()[1],local.list)) {
						if (local.list[0] != null) {	// Unification successful - save rollback data for backtracking
							local.stack.push(GlobalStack.getBoundStackTop(entity,entity,local.list[0]));
						}
						return ResolveRC.True;
					}
					else {	// Unification failed - rollback changes if they was.
						if (local.list[0] != null) {
							FProUtil.unbind(local.list[0]);
						}
						return ResolveRC.False;
					}
				}
				else { // List is over or contains variable in the tail.
					return ResolveRC.FalseWithoutBacktracking;
				}
			}
			else {	// Guard check failed...
				return ResolveRC.False; 
			}
	}

	@Override
	public void endResolve(final MyOwnMemory global, final ListEntityIndex local, final IFProEntity entity) throws SyntaxException {
		// Free binded variables, if they were.
		if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop<?>)local.stack.peek()).getMark() == entity) {
			FProUtil.unbind(((BoundStackTop<FProUtil.Change>)local.stack.pop()).getChangeChain());
		}
	}

	@Override
	public void afterCall(final MyOwnMemory global, final ListEntityIndex local) throws SyntaxException {
		global.collection.add(local);	// Return unused local memory instance to cache
	}

	@Override
	public String toString() {
		return "TutorialPlugin [getPluginDescriptors()=" + Arrays.toString(getPluginDescriptors()) + ", getName()=" + getName() + ", getVersion()=" + getVersion() + "]";
	}
}
