package chav1961.funnypro.core;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.entities.EnternalPluginEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.interfaces.FProPluginList;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo.Classification;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.ExternalEntityDescriptor;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.BoundStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.IteratorStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.OrChainStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.StackTopType;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.TemporaryStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.ExternalStackTop;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProRuledEntity;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;

/**
 * <p>This class is a standard resolver for all built-in predicates and operators, described in I.Bratko.
 * Use this class as an example for development your own plugins</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class StandardResolver implements IResolvable<GlobalDescriptor,LocalDescriptor>, FProPluginList {
	public static final String					PLUGIN_NAME	= "StandardResolver";
	public static final String					PLUGIN_PRODUCER	= "internal";
	public static final int[]					PLUGIN_VERSION	= new int[]{0};
	public static final String					PLUGIN_DESCRIPTION	= "Standard resolver for the fpro";

	static final StandardOperators[]			OPS = { new StandardOperators(1200,OperatorType.xfx,":-",RegisteredEntities.Op1200xfxGoal),
														new StandardOperators(1200,OperatorType.fx,":-",RegisteredEntities.Op1200fxGoal),
														new StandardOperators(1200,OperatorType.fx,"?-",RegisteredEntities.Op1200fxQuestion),
														new StandardOperators(1100,OperatorType.xfy,";",RegisteredEntities.Op1100xfyOr),
														new StandardOperators(1100,OperatorType.xfx,"|",RegisteredEntities.Op1100xfxSeparator),
														new StandardOperators(1050,OperatorType.xfy,"->",RegisteredEntities.Op1050xfyArrow),
														new StandardOperators(1000,OperatorType.xfy,",",RegisteredEntities.Op1000xfyAnd),
														new StandardOperators(900,OperatorType.fy,"not",RegisteredEntities.Op900fyNot),
														new StandardOperators(900,OperatorType.fy,"\\+",RegisteredEntities.Op900fyNotPlus),
														new StandardOperators(700,OperatorType.xfx,"=",RegisteredEntities.Op700xfxUnify),
														new StandardOperators(700,OperatorType.xfx,"\\=",RegisteredEntities.Op700xfxNotUnify),
														new StandardOperators(700,OperatorType.xfx,"==",RegisteredEntities.Op700xfxEqual),
														new StandardOperators(700,OperatorType.xfx,"\\==",RegisteredEntities.Op700xfxNotEqual),
														new StandardOperators(700,OperatorType.xfx,"=..",RegisteredEntities.Op700xfx2List),
														new StandardOperators(700,OperatorType.xfx,"is",RegisteredEntities.Op700xfxIs),
														new StandardOperators(700,OperatorType.xfx,"=:=",RegisteredEntities.Op700xfxEqColon),
														new StandardOperators(700,OperatorType.xfx,"=\\=",RegisteredEntities.Op700xfxNotEqual2),
														new StandardOperators(700,OperatorType.xfx,"<",RegisteredEntities.Op700xfxLess),
														new StandardOperators(700,OperatorType.xfx,"=<",RegisteredEntities.Op700xfxLessEqual),
														new StandardOperators(700,OperatorType.xfx,">",RegisteredEntities.Op700xfxGreater),
														new StandardOperators(700,OperatorType.xfx,">=",RegisteredEntities.Op700xfxGreaterEqual),
														new StandardOperators(700,OperatorType.xfx,"@<",RegisteredEntities.Op700xfxDogLess),
														new StandardOperators(700,OperatorType.xfx,"@<=",RegisteredEntities.Op700xfxDogLessEqual),
														new StandardOperators(700,OperatorType.xfx,"@>",RegisteredEntities.Op700xfxDogGreater),
														new StandardOperators(700,OperatorType.xfx,"@>=",RegisteredEntities.Op700xfxDogGreaterEqual),
														new StandardOperators(500,OperatorType.yfx,"+",RegisteredEntities.Op500yfxPlus),
														new StandardOperators(500,OperatorType.yfx,"-",RegisteredEntities.Op500yfxMinus),
														new StandardOperators(400,OperatorType.yfx,"*",RegisteredEntities.Op400yfxMultiply),
														new StandardOperators(400,OperatorType.yfx,"/",RegisteredEntities.Op400yfxDivide),
														new StandardOperators(400,OperatorType.yfx,"//",RegisteredEntities.Op400yfxIntDivide),
														new StandardOperators(400,OperatorType.yfx,"mod",RegisteredEntities.Op400yfxMod),
														new StandardOperators(200,OperatorType.xfx,"**",RegisteredEntities.Op200xfxExponent),
														new StandardOperators(200,OperatorType.xfy,"^",RegisteredEntities.Op200xfyAngle),
														new StandardOperators(200,OperatorType.fy,"-",RegisteredEntities.Op200fyMinus),
													};
	
	static final StandardPredicates[]			PREDS = { new StandardPredicates("trace",RegisteredEntities.PredTrace),
														new StandardPredicates("notrace",RegisteredEntities.PredNoTrace),
														new StandardPredicates("spy",RegisteredEntities.PredSpy),
														new StandardPredicates("!",RegisteredEntities.PredCut),
														new StandardPredicates("fail",RegisteredEntities.PredFail),
														new StandardPredicates("true",RegisteredEntities.PredTrue),
														new StandardPredicates("repeat",RegisteredEntities.PredRepeat),
														new StandardPredicates("asserta(Var)",RegisteredEntities.PredAssertA),
														new StandardPredicates("assertz(Var)",RegisteredEntities.PredAssertZ),
														new StandardPredicates("assert(Var)",RegisteredEntities.PredAssertZ),
														new StandardPredicates("retract(Var)",RegisteredEntities.PredRetract),
														new StandardPredicates("call(Var)",RegisteredEntities.PredCall),
														new StandardPredicates("var(Var)",RegisteredEntities.PredVar),
														new StandardPredicates("nonvar(Var)",RegisteredEntities.PredNonVar),
														new StandardPredicates("atom(Var)",RegisteredEntities.PredAtom),
														new StandardPredicates("integer(Var)",RegisteredEntities.PredInteger),
														new StandardPredicates("float(Var)",RegisteredEntities.PredFloat),
														new StandardPredicates("number(Var)",RegisteredEntities.PredNumber),
														new StandardPredicates("atomic(Var)",RegisteredEntities.PredAtomic),
														new StandardPredicates("compound(Var)",RegisteredEntities.PredCompound),
														new StandardPredicates("functor(Var1,Var2,Var3)",RegisteredEntities.PredFunctor),
														new StandardPredicates("arg(Var1,Var2,Var3)",RegisteredEntities.PredArg),
														new StandardPredicates("name(Var1,Var2)",RegisteredEntities.PredName),
														new StandardPredicates("bagof(Var1,Var2,Var3)",RegisteredEntities.PredBagOf),
														new StandardPredicates("setof(Var1,Var2,Var3)",RegisteredEntities.PredSetOf),
														new StandardPredicates("findall(Var1,Var2,Var3)",RegisteredEntities.PredFindAll),
														new StandardPredicates("memberOf(Var1,Var2)",RegisteredEntities.PredMemberOf),
													};

	private static enum RegisteredEntities {
		Op1200xfxGoal,
		Op1200fxGoal,
		Op1200fxQuestion,
		Op1100xfyOr,
		Op1100xfxSeparator,
		Op1050xfyArrow,
		Op1000xfyAnd,
		Op900fyNot,
		Op900fyNotPlus,
		Op700xfxUnify,
		Op700xfxNotUnify,
		Op700xfxEqual,
		Op700xfxNotEqual,
		Op700xfx2List,
		Op700xfxIs,
		Op700xfxEqColon,
		Op700xfxNotEqual2,
		Op700xfxLess,
		Op700xfxLessEqual,
		Op700xfxGreater,
		Op700xfxGreaterEqual,
		Op700xfxDogLess,
		Op700xfxDogLessEqual,
		Op700xfxDogGreater,
		Op700xfxDogGreaterEqual,
		Op500yfxPlus,
		Op500yfxMinus,
		Op400yfxMultiply,
		Op400yfxDivide,
		Op400yfxIntDivide,
		Op400yfxMod,
		Op200xfxExponent,
		Op200xfyAngle,
		Op200fyMinus,
		
		PredTrace,
		PredNoTrace,
		PredSpy,
		PredCut,
		PredFail,
		PredTrue,
		PredRepeat,
		PredAssertA,
		PredAssertZ,
		PredRetract,
		PredCall,
		PredVar,
		PredNonVar,
		PredAtom,
		PredInteger,
		PredFloat,
		PredNumber,
		PredAtomic,
		PredCompound,		
		PredFunctor,
		PredArg,
		PredName,
		PredBagOf,
		PredSetOf,
		PredFindAll,
		PredMemberOf,
		
		Others
	};

	final long[]	forInteger = new long[2];
	final double[]	forReal = new double[2];
	
	@Override
	public PluginDescriptor[] getPluginDescriptors() {
		return new PluginDescriptor[]{
				new PluginDescriptor(){
					@Override public IFProExternalEntity getPluginEntity() {return new EnternalPluginEntity(1,PLUGIN_NAME,PLUGIN_PRODUCER,PLUGIN_VERSION,new StandardResolver());}
					@Override public String getPluginPredicate() {return null;}
					@Override public String getPluginDescription() {return PLUGIN_DESCRIPTION;}
				}
		};
	}
	
	@Override public String getName() {return PLUGIN_NAME;}
	@Override public int[] getVersion() {return null;}

	@Override
	public GlobalDescriptor onLoad(final LoggerFacade log, final Properties parameters, final IFProEntitiesRepo repo) throws FProException {
		final GlobalDescriptor					desc = new GlobalDescriptor();
		final Set<Long>							ids = new HashSet<>();
		final Map<Long,QuickIds>				registered = new HashMap<>();
		
		desc.log = log;							desc.parameters = parameters;		
		
		try(LoggerFacade 	actualLog = log.transaction("StandardResolver:onLoad")) {
			
			for (StandardOperators item : OPS) {
				actualLog.message(Severity.info,"Register operator %1$s, %2$s...", item.text, item.type);
				final long			itemId = repo.termRepo().placeName(item.text,null);
				IFProOperator[]		op;
			
				if ((op = repo.getOperatorDef(itemId,item.priority,item.priority,item.type)).length == 0) {
					final IFProOperator			def = new OperatorDefEntity(item.priority,item.type,itemId); 
					
					repo.putOperatorDef(def);	ids.add(itemId);
					fillQuickIds(registered,new QuickIds(def,item.action));
				}
				else {
					fillQuickIds(registered,new QuickIds(op[0],item.action));					
				}
				if (repo.classify(itemId) != Classification.operator) {
					actualLog.message(Severity.info,"Operator registration failed for %1$s, %2$s: item classified as %3%s", item.text, item.type, repo.classify(itemId));
					throw new IllegalArgumentException("Attempt to register operator ["+item+"] failed: not classified!"); 
				}
				actualLog.message(Severity.info,"Operator %1$s, %2$s was registered successfully", item.text, item.type);
			}
			
			try{final IFProParserAndPrinter 	pap = new ParserAndPrinter(log,parameters,repo);
				for (StandardPredicates item : PREDS) {
					actualLog.message(Severity.info,"Register predicate %1$s...", item.text);
					try{pap.parseEntities(item.text.toCharArray(),0,new FProParserCallback(){
													@Override
													public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProParsingException, IOException {
														ids.add(entity.getEntityId());
														fillQuickIds(registered,new QuickIds(entity,item.action));
														return true;
													}
												}
						);
					} catch (FProParsingException | IOException exc) {
//						exc.printStackTrace();
						actualLog.message(Severity.info,"Predicate registration failed for %1$s: %2$s", item.text, exc.getMessage());
						throw new IllegalArgumentException("Attempt to register predicate ["+item+"] failed: "+exc.getMessage(),exc); 
					}
					actualLog.message(Severity.info,"Predicate %1$s was registeded successfully", item.text);
				}
			} catch (FProParsingException exc) {
				actualLog.message(Severity.info,"Problem creating predicate parser: %1$s", exc.getMessage());
				throw new IllegalArgumentException("Attempt to register predicates failed: "+exc.getMessage(),exc); 
			}
			actualLog.rollback();
		}
		
		desc.registered = registered.values().toArray(new QuickIds[ids.size()]);
		Arrays.sort(desc.registered);
		desc.prepared = true;
		desc.repo = repo;
		return desc;
	}

	@Override
	public void onRemove(final GlobalDescriptor global) throws FProException {
		if (global == null) {
			throw new IllegalArgumentException("Global object can't be null!");
		}
		else {
			final GlobalDescriptor	data = (GlobalDescriptor)global;
			
			if (!data.prepared) {
				throw new IllegalStateException("Attempt to close non-prepared item! Call prepare(repo) first!");
			}
			else {
				for (int index = 0; index < data.registered.length; index++) {
					QuickIds	start = data.registered[index].next;
					
					while (start != null) {
						data.repo.termRepo().removeName(start.id);
						start = start.next;
					}
				}
			}
		}
	}

	@Override
	public LocalDescriptor beforeCall(final GlobalDescriptor global, final IFProGlobalStack gs, final List<IFProVariable> vars, final IFProCallback callback) throws FProException {
		if (global == null) {
			throw new IllegalArgumentException("Global object can't be null!");
		}
		else if (callback == null) {
			throw new IllegalArgumentException("Callback can't be null!");
		}
		else {
			final LocalDescriptor	desc = new LocalDescriptor();
			
			desc.pap = new ParserAndPrinter(global.log,global.parameters,global.repo);
			desc.callback = callback;
			desc.stack = gs;
			desc.vars = vars;
			return desc;
		}
	}

	@Override
	public ResolveRC firstResolve(final GlobalDescriptor global, final LocalDescriptor local, final IFProEntity entity) throws FProException {
		if (global == null) {
			throw new IllegalArgumentException("Global object can't be null!");
		} 
		else if (local == null) {
			throw new IllegalArgumentException("Local object can't be null!");
		}
		else {
			switch (detect(global.registered,entity)) {
				case Op1200xfxGoal		:
					return (firstResolve(global,local,((IFProOperator)entity).getRight()));
				case Op1200fxGoal		:
				case Op1200fxQuestion	:
					if (local.callback != null) {
						local.callback.beforeFirstCall();
					}					
					if ((firstResolve(global,local,((IFProOperator)entity).getRight())) == ResolveRC.True) {
						if (local.callback != null) {
							return executeCallback(local.callback,local.vars,global.repo) ? ResolveRC.True : ResolveRC.UltimateFalse;
						}
						else {
							return ResolveRC.True;
						}
					}
					else {
						return ResolveRC.False;
					}
				case Op1100xfyOr		:
					ResolveRC		rcOr = firstResolve(global,local,((IFProOperator)entity).getLeft()); 
					
					if (rcOr == ResolveRC.True) {
						local.stack.push(GlobalStack.getOrChainStackTop(entity,true));
						return ResolveRC.True;
					}
					else if (rcOr == ResolveRC.False) {
						rcOr = firstResolve(global,local,((IFProOperator)entity).getRight()); 
						if (rcOr == ResolveRC.True) {
							local.stack.push(GlobalStack.getOrChainStackTop(entity,false));
							return ResolveRC.True;
						}
						else {
							return rcOr;
						}
					}
					else {
						return rcOr;
					}
				case Op1000xfyAnd		:
					ResolveRC		rcAnd = firstResolve(global,local,((IFProOperator)entity).getLeft());
					
					if (rcAnd == ResolveRC.True) {
						do {rcAnd = firstResolve(global,local,((IFProOperator)entity).getRight()); 
							if (rcAnd == ResolveRC.True) {
								return ResolveRC.True;
							}
							else if (rcAnd != ResolveRC.False) {
								break;
							}
						} while ((rcAnd = nextResolve(global,local,((IFProOperator)entity).getLeft())) == ResolveRC.True);
						endResolve(global,local,((IFProOperator)entity).getLeft());
						return rcAnd;
					}
					else {
						return	rcAnd;
					}
				case Op900fyNot			:
					ResolveRC	rcNot = firstResolve(global,local,((IFProOperator)entity).getRight()); 
					
					if (rcNot == ResolveRC.True) {
						return ResolveRC.False;
					}
					else if (rcNot == ResolveRC.False) {
						return ResolveRC.True;
					}
					else {
						return rcNot;
					}
				case Op700xfxUnify		:
					return unify(entity,((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight(),local.stack) ? ResolveRC.True : ResolveRC.False;
				case Op700xfxNotUnify	:
					return !unify(entity,((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight(),local.stack)  ? ResolveRC.True : ResolveRC.False;
				case Op700xfx2List		:
					final IFProEntity	left, right, created;
					if (((IFProOperator)entity).getLeft().getEntityType() == EntityType.variable || ((IFProOperator)entity).getLeft().getEntityType() == EntityType.anonymous) {
						left = ((IFProOperator)entity).getLeft();
						created = right = list2Predicate((IFProList) ((IFProOperator)entity).getRight());
					}
					else {
						created = left = predicate2List((IFProPredicate) ((IFProOperator)entity).getLeft());
						right = ((IFProOperator)entity).getRight();
					}
					return unifyTemporaries(entity, left, right, created, local.stack) ? ResolveRC.True : ResolveRC.False;
				case Op700xfxLess		:
					return compare(global,calculate(global,((IFProOperator)entity).getLeft()),calculate(global,((IFProOperator)entity).getRight())) < 0 ?  ResolveRC.True : ResolveRC.False;
				case Op700xfxLessEqual	:
					return compare(global,calculate(global,((IFProOperator)entity).getLeft()),calculate(global,((IFProOperator)entity).getRight())) <= 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxEqColon	:
					return compare(global,calculate(global,((IFProOperator)entity).getLeft()),calculate(global,((IFProOperator)entity).getRight())) == 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxNotEqual2	:
					return compare(global,calculate(global,((IFProOperator)entity).getLeft()),calculate(global,((IFProOperator)entity).getRight())) != 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxGreater	:
					return compare(global,calculate(global,((IFProOperator)entity).getLeft()),calculate(global,((IFProOperator)entity).getRight())) > 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxGreaterEqual	:
					return compare(global,calculate(global,((IFProOperator)entity).getLeft()),calculate(global,((IFProOperator)entity).getRight())) >= 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxDogLess	:
					return lexicalCompare(global.repo,((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight()) < 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxDogLessEqual	:
					return lexicalCompare(global.repo,((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight()) <= 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxDogGreater	:
					return lexicalCompare(global.repo,((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight()) > 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxDogGreaterEqual	:
					return lexicalCompare(global.repo,((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight()) >= 0 ? ResolveRC.True : ResolveRC.False;
				case Op700xfxEqual		:
					return isIdentical(((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight()) ? ResolveRC.True : ResolveRC.False;
				case Op700xfxNotEqual	:
					return !isIdentical(((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight()) ? ResolveRC.True : ResolveRC.False;
				case Op700xfxIs			:
					return unify(entity,((IFProOperator)entity).getLeft(),calculate(global,((IFProOperator)entity).getRight()),local.stack) ? ResolveRC.True : ResolveRC.False;
				case Op400yfxDivide		:
					return iterate((GlobalDescriptor)global,(LocalDescriptor)local,entity,entity,new IterablesCollection.IterableNameAndArity(global.repo,(IFProOperator)entity));
				case PredTrace			:
					local.trace = true;
					return ResolveRC.True;
				case PredNoTrace		:
					local.trace = false;
					return ResolveRC.True;
				case PredSpy			:
					return ResolveRC.True;
				case PredCut			:
					return ResolveRC.True;
				case PredFail			:
					return ResolveRC.False;
				case PredTrue			:
					return ResolveRC.True;
				case PredRepeat			:
					return ResolveRC.True;
				case PredAssertA		:
					global.repo.predicateRepo().assertA(FProUtil.duplicate(((IFProPredicate)entity).getParameters()[0]));
					return ResolveRC.True;
				case PredAssertZ		:
					global.repo.predicateRepo().assertZ(FProUtil.duplicate(((IFProPredicate)entity).getParameters()[0]));
					return ResolveRC.True;
				case PredRetract		:
					return global.repo.predicateRepo().retractFirst(((IFProPredicate)entity).getParameters()[0]) ? ResolveRC.True : ResolveRC.False;
				case PredCall			:
					return iterate((GlobalDescriptor)global,(LocalDescriptor)local,entity,((IFProPredicate)entity).getParameters()[0],new IterablesCollection.IterableCall(global.repo,(IFProPredicate)entity));
				case PredVar			:
					return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0],FProUtil.ContentType.Var) ? ResolveRC.True : ResolveRC.False;
				case PredNonVar			:
					return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0],FProUtil.ContentType.NonVar) ? ResolveRC.True : ResolveRC.False;
				case PredAtom			:
					return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0],FProUtil.ContentType.Atom) ? ResolveRC.True : ResolveRC.False;
				case PredInteger		:
					return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0],FProUtil.ContentType.Integer) ? ResolveRC.True : ResolveRC.False;
				case PredFloat			:
					return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0],FProUtil.ContentType.Float) ? ResolveRC.True : ResolveRC.False;
				case PredNumber			:
					return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0],FProUtil.ContentType.Number) ? ResolveRC.True : ResolveRC.False;
				case PredAtomic			:
					return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0],FProUtil.ContentType.Atomic) ? ResolveRC.True : ResolveRC.False;
				case PredCompound		:
					return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0],FProUtil.ContentType.Compound) ? ResolveRC.True : ResolveRC.False;
				case PredFunctor		:
					final IFProEntity	left1, right1, created1;
					
					if (((IFProPredicate)entity).getParameters()[0].getEntityType() == EntityType.predicate) {
						created = left = new PredicateEntity(((IFProPredicate)entity).getParameters()[0].getEntityId());
						right = ((IFProPredicate)entity).getParameters()[1];
						
						if (unifyTemporaries(entity, left, right, created, local.stack)) {
							created1 = left1 = new IntegerEntity(((IFProPredicate)((IFProPredicate)entity).getParameters()[0]).getArity());
							right1 = ((IFProPredicate)entity).getParameters()[2];
							
							if (unifyTemporaries(entity, left1, right1, created1, local.stack)) {
								return ResolveRC.True;
							}
							releaseTemporaries(left,local.stack);
						}
					}
					return ResolveRC.False;
				case PredArg			:
					if (((IFProPredicate)entity).getParameters()[0].getEntityType() == EntityType.predicate 
						&& ((IFProPredicate)entity).getParameters()[1].getEntityType() == EntityType.integer
						&& ((IFProPredicate)entity).getParameters()[1].getEntityId() > 0 
						&& ((IFProPredicate)entity).getParameters()[1].getEntityId() <= ((IFProPredicate)((IFProPredicate)entity).getParameters()[0]).getArity()) {
						
						created = left = FProUtil.duplicate(((IFProPredicate)((IFProPredicate)entity).getParameters()[0]).getParameters()[(int)((IFProPredicate)entity).getParameters()[1].getEntityId()-1]);
						right = ((IFProPredicate)entity).getParameters()[2];
						return unifyTemporaries(entity, left, right, created, local.stack) ? ResolveRC.True : ResolveRC.False;
					}
					else {
						return ResolveRC.False;
					}
				case PredName			:
					if (((IFProPredicate)entity).getParameters()[0].getEntityType() == EntityType.predicate) {
						created = left = new PredicateEntity(((IFProPredicate)entity).getParameters()[0].getEntityId());
						right = ((IFProPredicate)entity).getParameters()[1];
					}
					else {
						left = ((IFProPredicate)entity).getParameters()[0];
						created = right = new PredicateEntity(((IFProPredicate)entity).getParameters()[1].getEntityId());
					}
					return unifyTemporaries(entity, left, right, created, local.stack) ? ResolveRC.True : ResolveRC.False;
				case PredBagOf			:
					IFProList	bagofList = getBagof(global.repo,(IFProPredicate)entity);
					if (bagofList.getChild() == null && bagofList.getTail() == null) {
						return ResolveRC.False;
					}
					else {
						return unify(entity, ((IFProPredicate)entity).getParameters()[2],bagofList,local.stack) ? ResolveRC.True : ResolveRC.False;
					}
				case PredSetOf			:
					IFProList	setofList = getBagof(global.repo,(IFProPredicate)entity);
					if (setofList.getChild() == null && setofList.getTail() == null) {
						return ResolveRC.False;
					}
					else {
						return unify(entity, ((IFProPredicate)entity).getParameters()[2],orderSetofList(global.repo,setofList),local.stack) ? ResolveRC.True : ResolveRC.False;
					}
				case PredFindAll		:
					return unify(entity, ((IFProPredicate)entity).getParameters()[2],getBagof(global.repo,(IFProPredicate)entity),local.stack) ? ResolveRC.True : ResolveRC.False;
				case PredMemberOf		:
					return iterate((GlobalDescriptor)global,(LocalDescriptor)local,entity,((IFProPredicate)entity).getParameters()[1],new IterablesCollection.IterableList((IFProPredicate) entity));
				default :
					final ExternalEntityDescriptor	eed = global.repo.pluginsRepo().getResolver(entity);
					
					if (eed != null) {
						final Object	localData = eed.getResolver().beforeCall(eed.getGlobal(),local.stack,local.vars,local.callback);
						
						if (eed.getResolver().firstResolve(eed.getGlobal(),localData,entity) == ResolveRC.True) {
							local.stack.push(GlobalStack.getExternalStackTop(eed,localData));
							return ResolveRC.True;
						}
						else {
							eed.getResolver().endResolve(eed.getGlobal(),localData,entity);
							eed.getResolver().afterCall(eed.getGlobal(),localData);
							return ResolveRC.False;
						}
					}
					else {
						return iterate((GlobalDescriptor)global,(LocalDescriptor)local,entity,entity,global.repo.predicateRepo().call(entity));
					}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ResolveRC nextResolve(final GlobalDescriptor global, final LocalDescriptor local, IFProEntity entity) throws FProException {
		if (global == null) {
			throw new IllegalArgumentException("Global object can't be null!");
		} 
		else if (local == null) {
			throw new IllegalArgumentException("Local object can't be null!");
		}
		else {
			switch (detect(global.registered,entity)) {
				case Op1200xfxGoal		:
					ResolveRC	rc = nextResolve(global,local,((IFProOperator)entity).getRight());
					
					return rc == ResolveRC.FalseWithoutBacktracking ? ResolveRC.False : rc;	// Processing cut(!) retcode
				case Op1200fxGoal		:
				case Op1200fxQuestion	:
					rc = (nextResolve(global,local,((IFProOperator)entity).getRight()));
					if (rc == ResolveRC.True) {
						if (local.callback != null) {
							if (!executeCallback(local.callback,local.vars,global.repo)) {
								return ResolveRC.UltimateFalse;
							}
							else {
								return rc;
							}
						}
						else {
							return rc;
						}
					}
					else {
						return rc;
					}
				case Op1100xfyOr		:
					if (local.stack.peek().getTopType() == StackTopType.orChain) {
						if (((OrChainStackTop)local.stack.pop()).isFirst()) {
							ResolveRC	rcOr = nextResolve(global,local,((IFProOperator)entity).getLeft());
							
							if (rcOr == ResolveRC.False) {
								endResolve(global,local,((IFProOperator)entity).getLeft());
								
								rcOr = firstResolve(global,local,((IFProOperator)entity).getRight()); 
								local.stack.push(GlobalStack.getOrChainStackTop(entity,false));
								return rcOr;
							}
							else {
								local.stack.push(GlobalStack.getOrChainStackTop(entity,true));
								return rcOr;
							}
						}
						else {
							ResolveRC	rcOr = nextResolve(global,local,((IFProOperator)entity).getRight());
							
							local.stack.push(GlobalStack.getOrChainStackTop(entity,false));
							return rcOr;
						}
					}
					else {
						throw new IllegalStateException("OR record is missing in the stack!");
					}
				case Op1000xfyAnd		:
					ResolveRC	rcAnd = nextResolve(global,local,((IFProOperator)entity).getRight());
					
					if (rcAnd != ResolveRC.True) {
						endResolve(global,local,((IFProOperator)entity).getRight());
						
						if (rcAnd == ResolveRC.False) {
							while((rcAnd = nextResolve(global,local,((IFProOperator)entity).getLeft())) == ResolveRC.True) {
								rcAnd = firstResolve(global,local,((IFProOperator)entity).getRight()); 
								
								if (rcAnd == ResolveRC.True) {
									return ResolveRC.True;
								}
								else if (rcAnd != ResolveRC.False) {
									break;
								}
							}
						}
						endResolve(global,local,((IFProOperator)entity).getLeft());
						return rcAnd;
					}
					else {
						return rcAnd;
					}
				case Op900fyNot			:
				case Op700xfxUnify		:
				case Op700xfxNotUnify	:
				case Op700xfx2List		:
				case Op700xfxLess		:
				case Op700xfxLessEqual	:
				case Op700xfxEqColon	:
				case Op700xfxNotEqual2	:
				case Op700xfxGreater	:
				case Op700xfxGreaterEqual	:
				case Op700xfxDogLess	:
				case Op700xfxDogLessEqual	:
				case Op700xfxDogGreater	:
				case Op700xfxDogGreaterEqual	:
				case Op700xfxEqual		:
				case Op700xfxNotEqual	:
				case Op700xfxIs			:
					return ResolveRC.False;
				case Op400yfxDivide		:
					return continueIterate((GlobalDescriptor)global,(LocalDescriptor)local,entity,entity);
				case PredTrace			:
				case PredNoTrace		:
				case PredSpy			:
				case PredFail			:
				case PredTrue			:
					return ResolveRC.False;
				case PredCut			:
					return ResolveRC.FalseWithoutBacktracking;
				case PredRepeat			:
					return ResolveRC.True;
				case PredAssertA		:
				case PredAssertZ		:
					return ResolveRC.False;
				case PredRetract		:
					if (global.repo.predicateRepo().retractFirst(((IFProPredicate)entity).getParameters()[0])) {
						return ResolveRC.True;
					}
					else {
						return ResolveRC.False;
					}
				case PredCall			:
					return continueIterate((GlobalDescriptor)global,(LocalDescriptor)local,entity,((IFProPredicate)entity).getParameters()[0]);
				case PredVar			:
				case PredNonVar			:
				case PredAtom			:
				case PredInteger		:
				case PredFloat			:
				case PredNumber			:
				case PredAtomic			:
				case PredCompound		:
					return ResolveRC.False;
				case PredFunctor		:
				case PredArg			:
				case PredName			:
					return ResolveRC.False;
				case PredBagOf			:
				case PredSetOf			:
				case PredFindAll		:
					return ResolveRC.False;
				case PredMemberOf		:
					if (!local.stack.isEmpty() 
						&& local.stack.peek().getTopType() == StackTopType.bounds 
						&& ((BoundStackTop)local.stack.peek()).getMark() == entity) {	// Need to unbind second parameter, if was unified
						FProUtil.unbind(((BoundStackTop<FProUtil.Change>)local.stack.pop()).getChangeChain());
					}
					return continueIterate((GlobalDescriptor)global,(LocalDescriptor)local,entity,((IFProPredicate)entity).getParameters()[1]);
				default :
					if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.external) {
						final ExternalStackTop	est = (ExternalStackTop) local.stack.pop();
						
						if (est.getDescriptor().getResolver().nextResolve(est.getDescriptor().getGlobal(),est.getLocalData(),entity) == ResolveRC.True) {
							local.stack.push(est);
							return ResolveRC.True;
						}
						else {
							est.getDescriptor().getResolver().endResolve(est.getDescriptor().getGlobal(),est.getLocalData(),entity);
							est.getDescriptor().getResolver().afterCall(est.getDescriptor().getGlobal(),est.getLocalData());
							return ResolveRC.False;
						}
					}
					return continueIterate((GlobalDescriptor)global,(LocalDescriptor)local,entity,entity);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void endResolve(final GlobalDescriptor global, final LocalDescriptor local, final IFProEntity entity) throws FProException {
		if (global == null) {
			throw new IllegalArgumentException("Global object can't be null!");
		} 
		else if (local == null) {
			throw new IllegalArgumentException("Local object can't be null!");
		}
		else {
			switch (detect(global.registered,entity)) {
				case Op1200xfxGoal		:
					break;
				case Op1200fxGoal		:
				case Op1200fxQuestion	:
					endResolve(global,local,((IFProOperator)entity).getRight());
					if (local.callback != null) {
						local.callback.afterLastCall();
					}					
					break;
				case Op1100xfyOr		:
					if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.orChain) {
						if (((OrChainStackTop)local.stack.pop()).isFirst()) {
							endResolve(global,local,((IFProOperator)entity).getLeft());
						}
						else {
							endResolve(global,local,((IFProOperator)entity).getRight());
						}
					}
					else {
						throw new IllegalStateException("OR record is missing in the stack!");
					}
					break;
				case Op1000xfyAnd		:
					break;
				case Op900fyNot			:
				case Op700xfxUnify		:
				case Op700xfxNotUnify	:
					if (!local.stack.isEmpty() 
						&& local.stack.peek().getTopType() == StackTopType.bounds 
						&& ((BoundStackTop)local.stack.peek()).getMark() == entity) {
						FProUtil.unbind(((BoundStackTop<Change>)local.stack.pop()).getChangeChain());
					}
					break;
				case Op700xfx2List		:
					releaseTemporaries(entity,local.stack);
					break;
				case Op700xfxLess		:
				case Op700xfxLessEqual	:
				case Op700xfxEqColon	:
				case Op700xfxNotEqual2	:
				case Op700xfxGreater	:
				case Op700xfxGreaterEqual	:
				case Op700xfxDogLess	:
				case Op700xfxDogLessEqual	:
				case Op700xfxDogGreater	:
				case Op700xfxDogGreaterEqual	:
				case Op700xfxEqual		:
				case Op700xfxNotEqual	:
				case Op700xfxIs			:
					if (!local.stack.isEmpty() 
						&& local.stack.peek().getTopType() == StackTopType.bounds
						&& ((BoundStackTop)local.stack.peek()).getMark() == entity) {
						FProUtil.unbind(((BoundStackTop<Change>)local.stack.pop()).getChangeChain());
					}
					break;
				case Op400yfxDivide		:
					endIterate(entity,local.stack);
					break;
				case PredTrace			:
				case PredNoTrace		:
				case PredSpy			:
				case PredFail			:
				case PredTrue			:
					break;
				case PredCut			:
					break;
				case PredRepeat			:
					break;
				case PredAssertA		:
				case PredAssertZ		:
				case PredRetract		:
					break;
				case PredCall			:
					endIterate(entity,local.stack);
				case PredVar			:
				case PredNonVar			:
				case PredAtom			:
				case PredInteger		:
				case PredFloat			:
				case PredNumber			:
				case PredAtomic			:
				case PredCompound		:
					break;
				case PredFunctor		:	// Missing break is a RIGHT!
					releaseTemporaries(entity,local.stack);
				case PredArg			:
				case PredName			:
					releaseTemporaries(entity,local.stack);
					break;
				case PredBagOf			:
				case PredSetOf			:
				case PredFindAll		:
					if (!local.stack.isEmpty() 
						&& local.stack.peek().getTopType() == StackTopType.bounds
						&& ((BoundStackTop)local.stack.peek()).getMark() == entity) {
						FProUtil.unbind(((BoundStackTop<Change>)local.stack.pop()).getChangeChain());
					}
					break;
				case PredMemberOf		:
					endIterate(entity,local.stack);
					break;
				default :
					if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.external) {
						final ExternalStackTop	est = (ExternalStackTop) local.stack.pop();
						
						est.getDescriptor().getResolver().endResolve(est.getDescriptor().getGlobal(),est.getLocalData(),entity);
						est.getDescriptor().getResolver().afterCall(est.getDescriptor().getGlobal(),est.getLocalData());
						return;
					}
					endIterate(entity,local.stack);
			}
		}
	}

	@Override
	public void afterCall(final GlobalDescriptor global, final LocalDescriptor local) throws FProException {
		local.vars = null;		
		local.pap = null;
		local.callback = null;
		local.stack = null;		
	}

	@Override
	public String toString() {
		return "StandardResolver [getPluginDescriptors()=" + Arrays.toString(getPluginDescriptors()) + ", getName()=" + getName() + ", getVersion()=" + Arrays.toString(getVersion())+ "]";
	}

	private RegisteredEntities detect(final QuickIds[] repo, final IFProEntity entity) {
		if (entity == null) {
			return RegisteredEntities.Others; 
		}
		else {
			int	found = binarySearch(repo,entity.getEntityId());
			
			if (found >= 0) {
				QuickIds	start = repo[found];
				
				while (start != null) {
					switch (entity.getEntityType()) {
						case operator	:
							if (((IFProOperator)entity).getPriority() == ((IFProOperator)start.def).getPriority() && ((IFProOperator)entity).getOperatorType() == ((IFProOperator)start.def).getOperatorType()) {
								return start.action;
							}
							break;
						case predicate	:
							if (((IFProPredicate)entity).getArity() == ((IFProPredicate)start.def).getArity()) {
								return start.action;
							}
							break;
						default :
					}
					start = start.next;
				}
				return RegisteredEntities.Others; 
			}
			else {
				return RegisteredEntities.Others; 
			}
		}
	}
	
	private IFProEntity calculate(final GlobalDescriptor global, final IFProEntity value) {
		try{return calculate(global,global.registered,value);
		} catch (UnsupportedOperationException exc) {
			return null;
		}
	}

	private IFProEntity calculate(final GlobalDescriptor global, final QuickIds[] repo, final IFProEntity value) {
		if (value == null) {
			throw new UnsupportedOperationException();
		}
		else {
			IFProEntity		right;
			
			switch (value.getEntityType()) {
				case integer	:
				case real		:
					return value;
				case operator	:
					switch (detect(global.registered,value)) {
						case Op500yfxPlus		:
							if (convert2Real(calculate(global,repo,((IFProOperator)value).getLeft()),calculate(global,repo,((IFProOperator)value).getRight()),forInteger,forReal)) {
								return new RealEntity(forReal[0]+forReal[1]);
							}
							else {
								return new IntegerEntity(forInteger[0]+forInteger[1]);
							}
						case Op500yfxMinus		:
							if (convert2Real(calculate(global,repo,((IFProOperator)value).getLeft()),calculate(global,repo,((IFProOperator)value).getRight()),forInteger,forReal)) {
								return new RealEntity(forReal[0]-forReal[1]);
							}
							else {
								return new IntegerEntity(forInteger[0]-forInteger[1]);
							}
						case Op400yfxMultiply	:
							if (convert2Real(calculate(global,repo,((IFProOperator)value).getLeft()),calculate(global,repo,((IFProOperator)value).getRight()),forInteger,forReal)) {
								return new RealEntity(forReal[0]*forReal[1]);
							}
							else {
								return new IntegerEntity(forInteger[0]*forInteger[1]);
							}
						case Op400yfxDivide		:
							if (convert2Real(calculate(global,repo,((IFProOperator)value).getLeft()),calculate(global,repo,((IFProOperator)value).getRight()),forInteger,forReal)) {
								return new RealEntity(forReal[0]/forReal[1]);
							}
							else {
								return new RealEntity(1.00*forInteger[0]/forInteger[1]);
							}
						case Op400yfxIntDivide	:
							if (convert2Real(calculate(global,repo,((IFProOperator)value).getLeft()),calculate(global,repo,((IFProOperator)value).getRight()),forInteger,forReal)) {
								return new RealEntity((long)(forReal[0]/forReal[1]));
							}
							else {
								return new IntegerEntity(forInteger[0]/forInteger[1]);
							}
						case Op400yfxMod		:
							if (convert2Real(calculate(global,repo,((IFProOperator)value).getLeft()),calculate(global,repo,((IFProOperator)value).getRight()),forInteger,forReal)) {
								return new RealEntity(forReal[0]%forReal[1]);
							}
							else {
								return new IntegerEntity(forInteger[0]%forInteger[1]);
							}
						case Op200xfxExponent	:
							if (convert2Real(calculate(global,repo,((IFProOperator)value).getLeft()),calculate(global,repo,((IFProOperator)value).getRight()),forInteger,forReal)) {
								return new RealEntity(Math.pow(forReal[0],forReal[1]));
							}
							else {
								return new IntegerEntity((long)Math.pow(1.00*forInteger[0],1.00*forInteger[1]));
							}
						case Op200fyMinus		:
							right = calculate(global,((IFProOperator)value).getRight());
							if (right != null) {
								if (right.getEntityType() == EntityType.real) {
									return new RealEntity(-Double.longBitsToDouble(right.getEntityId()));
								}
								else {
									return new IntegerEntity(-right.getEntityId()); 
								}
							}
							else {
								return null;
							}
						default : 
							throw new UnsupportedOperationException();
					}
				default :
					throw new UnsupportedOperationException();
			}
		}
	}

	private boolean convert2Real(final IFProEntity left, final IFProEntity right, final long[] forInteger, final double[] forReal) {
		if (left != null && right != null) {
			if (left.getEntityType() == right.getEntityType()) {
				if (left.getEntityType() == EntityType.integer) {
					forInteger[0] = left.getEntityId();		
					forInteger[1] = right.getEntityId();
					return false;
				}
				else {
					forReal[0] = Double.longBitsToDouble(left.getEntityId());		
					forReal[1] = Double.longBitsToDouble(right.getEntityId());
					return true;
				}
			}
			else {
				forReal[0] = left.getEntityType() == EntityType.integer ? left.getEntityId() : Double.longBitsToDouble(left.getEntityId());		
				forReal[1] = right.getEntityType() == EntityType.integer ? right.getEntityId() : Double.longBitsToDouble(right.getEntityId());
				return true;
			}
		}
		else {
			throw new UnsupportedOperationException();
		}
	}
	
	private boolean unify(final IFProEntity mark, final IFProEntity left, final IFProEntity right, final IFProGlobalStack stack) {
		final Change[]	list = new Change[1];
		final boolean	result = FProUtil.unify(left, right, list);
		
		if (result) {
			if (list[0] != null) {
				stack.push(GlobalStack.getBoundStackTop(mark,list[0]));
			}
		}
		else if (list[0] != null) {
			FProUtil.unbind(list[0]);
		}
		return result;
	}

	private boolean unifyTemporaries(final IFProEntity mark, final IFProEntity left, final IFProEntity right, final IFProEntity created, final IFProGlobalStack stack) {
		if (!unify(mark,left,right,stack)) {
			FProUtil.removeEntity(created);
			return false;
		}
		else {
			stack.push(GlobalStack.getTemporaryStackTop(created));
			return true;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void releaseTemporaries(final IFProEntity mark, final IFProGlobalStack stack) {
		if (!stack.isEmpty() && stack.peek().getTopType() == StackTopType.temporary) {
			FProUtil.removeEntity(((TemporaryStackTop)stack.pop()).getEntity());
			
			if (!stack.isEmpty() && stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)stack.peek()).getMark() == mark) {
				FProUtil.unbind(((BoundStackTop<Change>)stack.pop()).getChangeChain());
			}
		}
	}
	
	private int compare(final GlobalDescriptor global,final IFProEntity left, final IFProEntity right) {
		final IFProEntity	leftVal = calculate(global,left), rightVal = calculate(global,right);
		
		if (leftVal == rightVal) {
			return 0;
		}
		else if (leftVal == null) {
			return 1;
		}
		else if (rightVal == null) {
			return -1;
		}
		else {
//			final long[]	forInteger = new long[2];
//			final double[]	forReal = new double[2];
			
			if (convert2Real(leftVal,rightVal,forInteger,forReal)) {
				return forReal[0] < forReal[1] ? -1 : (forReal[0] > forReal[1] ? 1 : 0);
			}
			else {
				return forInteger[0] < forInteger[1] ? -1 : (forInteger[0] > forInteger[1] ? 1 : 0);
			}
		}
	}

	private int lexicalCompare(final IFProEntitiesRepo repo, final IFProEntity left, final IFProEntity right) throws FProPrintingException {
		if (left == right) {
			return 0;
		}
		else if (left == null) {
			return -1;
		}
		else if (right == null) {
			return 1;
		}
		else if (left.getEntityType() != right.getEntityType()) {
			return left.getEntityType().ordinal() - right.getEntityType().ordinal(); 
		}
		else {
			int compare;
			
			switch (left.getEntityType()) {
				case string		:
					return repo.stringRepo().compareNames(left.getEntityId(),right.getEntityId());
				case integer	:
					return left.getEntityId() < right.getEntityId() ? -1 : (left.getEntityId() > right.getEntityId() ? 1 : 0);
				case real		:
					return left.getEntityId() < right.getEntityId() ? -1 : (left.getEntityId() > right.getEntityId() ? 1 : 0);
				case anonymous	:
					return 0;
				case variable	:
					return repo.termRepo().compareNames(left.getEntityId(),right.getEntityId());
				case list		:
					if ((compare = lexicalCompare(repo,((IFProList)left).getChild(),((IFProList)right).getChild())) == 0) {
						return lexicalCompare(repo,((IFProList)left).getTail(),((IFProList)right).getTail());
					}
					else {
						return compare;
					}
				case operator	:
					if ((compare = repo.termRepo().compareNames(left.getEntityId(),right.getEntityId())) == 0) {
						if ((compare = lexicalCompare(repo,((IFProOperator)left).getLeft(),((IFProOperator)right).getLeft())) == 0) {
							return lexicalCompare(repo,((IFProOperator)left).getRight(),((IFProOperator)right).getRight());							
						}
						else {
							return compare;
						}
					}
					else {
						return compare;
					}
				case predicate	:
					if ((compare = repo.termRepo().compareNames(left.getEntityId(),right.getEntityId())) == 0) {
						if (((IFProPredicate)left).getArity() == ((IFProPredicate)right).getArity()) {
							for (int index = 0; index < ((IFProPredicate)left).getArity(); index++) {
								if ((compare = lexicalCompare(repo,((IFProPredicate)left).getParameters()[index],((IFProPredicate)right).getParameters()[index])) != 0) {
									return compare;
								}
							}
							return 0;
						}
						else {
							return ((IFProPredicate)left).getArity() - ((IFProPredicate)right).getArity();
						}
					}
					else {
						return compare;
					}
				default :
					throw new IllegalArgumentException();
			}
		}
	}

	private boolean isIdentical(final IFProEntity left, final IFProEntity right) {
		if (left == right) {
			return true;
		}
		else if (left == null || right == null) {
			return false;
		}
		else if (left.getEntityId() != right.getEntityId() || left.getEntityType() != right.getEntityType()) {
			return false;
		}
		else {
			switch (left.getEntityType()) {
				case string		:
				case integer	:
				case real		:
				case anonymous	:
				case variable	:
					return true;
				case list		:
					return isIdentical(((IFProList)left).getChild(),((IFProList)right).getChild()) && isIdentical(((IFProList)left).getTail(),((IFProList)right).getTail()); 
				case operator	:
					if (((IFProOperator)left).getPriority() == ((IFProOperator)right).getPriority() && ((IFProOperator)left).getOperatorType() == ((IFProOperator)right).getOperatorType()) {
						return isIdentical(((IFProOperator)left).getLeft(),((IFProOperator)right).getLeft()) && isIdentical(((IFProOperator)left).getRight(),((IFProOperator)right).getRight()); 
					}
					else {
						return false;
					}						
				case predicate	:
					if (((IFProPredicate)left).getArity() == ((IFProPredicate)right).getArity()) {
						for (int index = 0; index < ((IFProPredicate)left).getArity(); index++) {
							if (!isIdentical(((IFProPredicate)left).getParameters()[index],((IFProPredicate)right).getParameters()[index])) {
								return false;
							}
						}
						return true;
					}
					else {
						return false;
					}
				default :
					throw new IllegalArgumentException();
			}
		}
	}

	private IFProPredicate list2Predicate(final IFProList source) {
		if (source.getChild().getEntityType() == EntityType.predicate) {
			IFProPredicate	result = new PredicateEntity(source.getChild().getEntityId()); 
			IFProList		actual = (IFProList) source.getTail();
			int				count = 0;
			
			while (actual != null && actual instanceof IFProList) {
				actual = (IFProList) actual.getTail();
				count++;
			}
			
			final IFProEntity[]	parameters = new IFProEntity[count];
			
			actual = (IFProList) source.getTail();
			for (int index = 0; index < parameters.length; index++, actual = (IFProList) actual.getTail()) {
				parameters[index] = FProUtil.duplicate(actual.getChild());
			}
			result.setParameters(parameters);
			return result;
		}
		else {
			return null;
		}
		
	}
	
	private IFProList predicate2List(final IFProPredicate source) {
		IFProList	actual = new ListEntity(new PredicateEntity(source.getEntityId()),null), result = actual;
		
		for (int index = 0; index < source.getArity(); index++) {
			actual.setTail(new ListEntity(FProUtil.duplicate(source.getParameters()[index]),null).setParent(actual));
		}
		return result;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ResolveRC iterate(final GlobalDescriptor global, final LocalDescriptor local, final IFProEntity mark, final IFProEntity entity, final Iterable<IFProEntity> iterable) throws FProException {
		local.stack.push(GlobalStack.getIteratorStackTop(iterable,IFProEntity.class));
		while ((((Iterable)((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator()).iterator().hasNext())) {
			final IFProEntity	candidate = ((Iterable<IFProEntity>)((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator()).iterator().next(); 

			if (unify(mark,entity,candidate,local.stack)) {
				if (candidate instanceof IFProRuledEntity && ((IFProRuledEntity)candidate).getRule() != null) {	// Process ruled entities
					ResolveRC	rcRule = firstResolve(global,local,((IFProRuledEntity)candidate).getRule());
					
					if (rcRule == ResolveRC.True) {
						local.stack.push(GlobalStack.getTemporaryStackTop(((IFProRuledEntity)candidate).getRule()));
						return ResolveRC.True;
					}
					else if (rcRule == ResolveRC.False) {
						continue;
					}
					else {
						return rcRule;
					}
				}
				else {
					return ResolveRC.True;
				}
			}
		}
		local.stack.pop();
		return ResolveRC.False;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ResolveRC continueIterate(final GlobalDescriptor global, final LocalDescriptor local, final IFProEntity mark, final IFProEntity entity) throws FProException {
		if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.temporary) {
			IFProEntity		rule = ((TemporaryStackTop)local.stack.pop()).getEntity();
			
			if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)local.stack.peek()).getMark() == rule) {
				FProUtil.unbind(((BoundStackTop<FProUtil.Change>)local.stack.pop()).getChangeChain());
			}
			ResolveRC	rcRule = nextResolve(global,local,rule);
			
			if (rcRule == ResolveRC.True) {
				local.stack.push(GlobalStack.getTemporaryStackTop(rule));
				return ResolveRC.True;
			}
			else {
				endResolve(global,local,rule);
				return rcRule;
			}
		}
		if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)local.stack.peek()).getMark() == mark) {
			FProUtil.unbind(((BoundStackTop<FProUtil.Change>)local.stack.pop()).getChangeChain());
		}
		if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.iterator) {
			while ((((Iterable<IFProEntity>)((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator()).iterator().hasNext())) {
				final IFProEntity	candidate = ((Iterable<IFProEntity>)((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator()).iterator().next(); 

				if (unify(mark,entity,candidate,local.stack)) {
					if (candidate instanceof IFProRuledEntity && ((IFProRuledEntity)candidate).getRule() != null) {	// Process ruled entities
						ResolveRC	rcRule = firstResolve(global,local,((IFProRuledEntity)candidate).getRule());
						
						if (rcRule == ResolveRC.True) {
							local.stack.push(GlobalStack.getTemporaryStackTop(((IFProRuledEntity)candidate).getRule()));
							return ResolveRC.True;
						}
						else if (rcRule == ResolveRC.False) {
							local.stack.pop();
							continue;
						}
						else {
							local.stack.pop();
							return rcRule;
						}
					}
					else {
						return ResolveRC.True;
					}
				}
			}
			return ResolveRC.False;
		}
		else {
			throw new IllegalStateException("Awaited iterator missing in the stack");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void endIterate(final IFProEntity entity, final IFProGlobalStack stack) {
		if (!stack.isEmpty() && stack.peek().getTopType() == StackTopType.temporary) {
			stack.pop();
		}
		if (!stack.isEmpty() && stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)stack.peek()).getMark() == entity) {
			FProUtil.unbind(((BoundStackTop<FProUtil.Change>)stack.pop()).getChangeChain());
		}
		if (!stack.isEmpty() && stack.peek().getTopType() == StackTopType.iterator) {
			stack.pop();
		}
//		else {
//			throw new IllegalStateException();
//		}
	}

	private IFProList getBagof(final IFProEntitiesRepo repo, final IFProPredicate bagof) {
		IFProList	start = new ListEntity(null,null), actual = start, pred = null; 
		
		for (IFProEntity item : new IterablesCollection.IterableCallBagof(repo,bagof)) {
			actual.setChild(item);		item.setParent(actual);		
			actual.setTail(new ListEntity(null,null).setParent(actual));
			pred = actual;
			actual = (IFProList) actual.getTail();
		}
		if (pred != null) {	// The same last element in the list is always empty
			pred.setTail(null);
		}
		return start;
	}

	private IFProEntity orderSetofList(final IFProEntitiesRepo repo, final IFProList setofList) throws FProPrintingException {
		if (setofList.getChild() == null || setofList.getTail() == null || ((IFProList)setofList.getTail()).getChild() == null) {
			return setofList;
		}
		else {
			boolean 	wasChanged;
			IFProList 	start, last, temp;
			IFProEntity	temp1, temp2;
			
			do {start = setofList;		// Bubble sort (directly in the list)
				wasChanged = false;
				
				while (((IFProList)start.getTail()).getTail() != null) {
					if (lexicalCompare(repo,temp1 = start.getChild(),temp2 = ((IFProList)start.getTail()).getChild()) > 0) {
						temp1.setParent(start.getTail());	// Swap data in the neighbour records
						temp2.setParent(start);
						start.setChild(temp2);
						((IFProList)start.getTail()).setChild(temp1);
						wasChanged = true;
					}
					start = (IFProList) start.getTail();
				}				
			} while (wasChanged);
			
			start = setofList;
			
			while (((IFProList)start.getTail()).getTail() != null) {
				if (lexicalCompare(repo,start.getChild(),((last = (IFProList)start.getTail())).getChild()) == 0) {
					last.getChild().setParent(null);		// Remove duplicate element and it's child from the list
					last.setChild(null).setParent(null);
					temp = start;
					start = (IFProList) last.getTail();
					temp.setTail(start);
					start.setParent(temp);
					last.setTail(null);
				}
				else {
					last = start;
					start = (IFProList) start.getTail();
					start.setParent(last);
				}
			}
			
			return setofList;
		}
	}
	

	private void fillQuickIds(final Map<Long,QuickIds> repo, final QuickIds data) {
		if (!repo.containsKey(data.id)) {
			repo.put(data.id,data);
		}
		else {
			data.next = repo.get(data.id);
			repo.put(data.id,data);
		}
	}	

	private boolean executeCallback(IFProCallback callback, List<IFProVariable> vars, final IFProEntitiesRepo repo) throws FProParsingException, FProPrintingException {
		final Map<String,Object>	resolved = new HashMap<>();
		
		for (IFProVariable var : vars) {
			resolved.put(repo.termRepo().getName(var.getEntityId()),var.getParent() == null ? var : var.getParent());
		}
		return callback.onResolution(resolved);
	}

	private static int binarySearch(final QuickIds[] content, final long id) {
		int 	low = 0, high = content.length - 1, mid;
		long	midVal;

        while (low <= high) {
            mid = (low + high) >>> 1;
            midVal = content[mid].id;

            if (midVal < id) {
                low = mid + 1;
            }
            else if (midVal > id) {
                high = mid - 1;
            }
            else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
	}
	
	
	static class StandardOperators {
		public final int				priority;
		public final OperatorType		type;
		public final String				text;
		public final RegisteredEntities	action;
		
		public StandardOperators(final int priority, final OperatorType type, final String text, final RegisteredEntities action) {
			this.priority = priority;	this.type  = type;
			this.text = text;			this.action = action;
		}

		@Override public String toString() {return "StandardOperators [priority=" + priority + ", type=" + type + ", text=" + text + ", action = " + action + "]";}
	}

	static class StandardPredicates {
		public final String				text;		
		public final RegisteredEntities	action;
		
		public StandardPredicates(String text, RegisteredEntities action) {
			this.text = text;
			this.action = action;
		}

		@Override public String toString() {return "StandardPredicates [text=" + text + ", action=" + action + "]";}
	}
	
	static class QuickIds implements Comparable<QuickIds>{
		public long 				id;
		public IFProEntity			def;
		public RegisteredEntities 	action;
		public QuickIds				next = null;
		
		public QuickIds(final IFProEntity def, final RegisteredEntities action) {
			this.id = def.getEntityId();
			this.def = def;			this.action = action;
		}

		@Override
		public int compareTo(final QuickIds o) {
			return o.id < id ? 1 : (o.id > id ? -1 : 0);
		}

		@Override public String toString() {return "QuickIds [id=" + id + ", def=" + def + ", action=" + action + "]";}
	}
}
