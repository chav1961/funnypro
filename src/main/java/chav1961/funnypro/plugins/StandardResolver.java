package chav1961.funnypro.plugins;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.FProUtil.ContentType;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.QuickIds;
import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.ExternalPluginEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.StringEntity;
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
import chav1961.funnypro.core.interfaces.IFProGlobalStack.ExternalStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.GlobalStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.IteratorStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.OrChainStackTop;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.StackTopType;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.TemporaryStackTop;
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
import chav1961.purelib.basic.DottedVersion;
import chav1961.purelib.basic.ReusableInstances;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.charsource.SyntaxTreeCharacterSource;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.interfaces.CharacterTarget;


/**
 * <p>This class is a standard resolver for all built-in predicates and operators, described in I.Bratko.
 * Use this class as an example for development your own plugins</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.2
 */

public class StandardResolver implements IResolvable<StandardResolverGlobal,StandardResolverLocal>, FProPluginList {
	public static final String			PLUGIN_NAME	= "StandardResolver";
	public static final String			PLUGIN_PRODUCER	= "internal";
	public static final DottedVersion	PLUGIN_VERSION = DottedVersion.ZERO;
	public static final String			PLUGIN_DESCRIPTION	= "Standard resolver for the Funny Prolog";

	@SuppressWarnings("rawtypes")
	static final RegisteredOperators[]			OPS = { new RegisteredOperators<RegisteredEntities>(1200,OperatorType.xfx,":-",RegisteredEntities.Op1200xfxGoal),
														new RegisteredOperators<RegisteredEntities>(1200,OperatorType.fx,":-",RegisteredEntities.Op1200fxGoal),
														new RegisteredOperators<RegisteredEntities>(1200,OperatorType.fx,"?-",RegisteredEntities.Op1200fxQuestion),
														new RegisteredOperators<RegisteredEntities>(1100,OperatorType.xfy,";",RegisteredEntities.Op1100xfyOr),
														new RegisteredOperators<RegisteredEntities>(1100,OperatorType.xfx,"|",RegisteredEntities.Op1100xfxSeparator),
							 							new RegisteredOperators<RegisteredEntities>(1050,OperatorType.xfy,"->",RegisteredEntities.Op1050xfyArrow),
														new RegisteredOperators<RegisteredEntities>(1000,OperatorType.xfy,",",RegisteredEntities.Op1000xfyAnd),
									 					new RegisteredOperators<RegisteredEntities>(900,OperatorType.fy,"not",RegisteredEntities.Op900fyNot),
														new RegisteredOperators<RegisteredEntities>(900,OperatorType.fy,"\\+",RegisteredEntities.Op900fyNotPlus),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"=",RegisteredEntities.Op700xfxUnify),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"\\=",RegisteredEntities.Op700xfxNotUnify),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"==",RegisteredEntities.Op700xfxEqual),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"\\==",RegisteredEntities.Op700xfxNotEqual),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"=..",RegisteredEntities.Op700xfx2List),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"is",RegisteredEntities.Op700xfxIs),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"=:=",RegisteredEntities.Op700xfxEqColon),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"=\\=",RegisteredEntities.Op700xfxNotEqual2),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"<",RegisteredEntities.Op700xfxLess),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"=<",RegisteredEntities.Op700xfxLessEqual),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,">",RegisteredEntities.Op700xfxGreater),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,">=",RegisteredEntities.Op700xfxGreaterEqual),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"@<",RegisteredEntities.Op700xfxDogLess),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"@<=",RegisteredEntities.Op700xfxDogLessEqual),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"@>",RegisteredEntities.Op700xfxDogGreater),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"@>=",RegisteredEntities.Op700xfxDogGreaterEqual),
														new RegisteredOperators<RegisteredEntities>(500,OperatorType.yfx,"+",RegisteredEntities.Op500yfxPlus),
														new RegisteredOperators<RegisteredEntities>(500,OperatorType.yfx,"-",RegisteredEntities.Op500yfxMinus),
														new RegisteredOperators<RegisteredEntities>(400,OperatorType.yfx,"*",RegisteredEntities.Op400yfxMultiply),
														new RegisteredOperators<RegisteredEntities>(400,OperatorType.yfx,"/",RegisteredEntities.Op400yfxDivide),
														new RegisteredOperators<RegisteredEntities>(400,OperatorType.yfx,"//",RegisteredEntities.Op400yfxIntDivide),
														new RegisteredOperators<RegisteredEntities>(400,OperatorType.yfx,"mod",RegisteredEntities.Op400yfxMod),
														new RegisteredOperators<RegisteredEntities>(200,OperatorType.xfx,"**",RegisteredEntities.Op200xfxExponent),
														new RegisteredOperators<RegisteredEntities>(200,OperatorType.xfy,"^",RegisteredEntities.Op200xfyAngle),
														new RegisteredOperators<RegisteredEntities>(200,OperatorType.fy,"-",RegisteredEntities.Op200fyMinus),
													};
	
	@SuppressWarnings("rawtypes")
	static final RegisteredPredicates[]			PREDS = { new RegisteredPredicates<RegisteredEntities>("trace",RegisteredEntities.PredTrace),
														new RegisteredPredicates<RegisteredEntities>("notrace",RegisteredEntities.PredNoTrace),
														new RegisteredPredicates<RegisteredEntities>("spy",RegisteredEntities.PredSpy),
														new RegisteredPredicates<RegisteredEntities>("!",RegisteredEntities.PredCut),
														new RegisteredPredicates<RegisteredEntities>("fail",RegisteredEntities.PredFail),
														new RegisteredPredicates<RegisteredEntities>("true",RegisteredEntities.PredTrue),
														new RegisteredPredicates<RegisteredEntities>("repeat",RegisteredEntities.PredRepeat),
														new RegisteredPredicates<RegisteredEntities>("asserta(Var)",RegisteredEntities.PredAssertA),
														new RegisteredPredicates<RegisteredEntities>("assertz(Var)",RegisteredEntities.PredAssertZ),
														new RegisteredPredicates<RegisteredEntities>("assert(Var)",RegisteredEntities.PredAssertZ),
														new RegisteredPredicates<RegisteredEntities>("retract(Var)",RegisteredEntities.PredRetract),
														new RegisteredPredicates<RegisteredEntities>("call(Var)",RegisteredEntities.PredCall),
														new RegisteredPredicates<RegisteredEntities>("var(Var)",RegisteredEntities.PredVar),
														new RegisteredPredicates<RegisteredEntities>("nonvar(Var)",RegisteredEntities.PredNonVar),
														new RegisteredPredicates<RegisteredEntities>("atom(Var)",RegisteredEntities.PredAtom),
														new RegisteredPredicates<RegisteredEntities>("integer(Var)",RegisteredEntities.PredInteger),
														new RegisteredPredicates<RegisteredEntities>("float(Var)",RegisteredEntities.PredFloat),
														new RegisteredPredicates<RegisteredEntities>("number(Var)",RegisteredEntities.PredNumber),
														new RegisteredPredicates<RegisteredEntities>("atomic(Var)",RegisteredEntities.PredAtomic),
														new RegisteredPredicates<RegisteredEntities>("compound(Var)",RegisteredEntities.PredCompound),
														new RegisteredPredicates<RegisteredEntities>("functor(Var1,Var2,Var3)",RegisteredEntities.PredFunctor),
														new RegisteredPredicates<RegisteredEntities>("arg(Var1,Var2,Var3)",RegisteredEntities.PredArg),
														new RegisteredPredicates<RegisteredEntities>("name(Var1,Var2)",RegisteredEntities.PredName),
														new RegisteredPredicates<RegisteredEntities>("bagof(Var1,Var2,Var3)",RegisteredEntities.PredBagOf),
														new RegisteredPredicates<RegisteredEntities>("setof(Var1,Var2,Var3)",RegisteredEntities.PredSetOf),
														new RegisteredPredicates<RegisteredEntities>("findall(Var1,Var2,Var3)",RegisteredEntities.PredFindAll),
														new RegisteredPredicates<RegisteredEntities>("memberOf(Var1,Var2)",RegisteredEntities.PredMemberOf),
													};

	public static enum RegisteredEntities {
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

	
	
	@FunctionalInterface
	private static interface EntityGetter {
		IFProEntity get();
	}
	
	private static interface NumericFunction {
	}	
	
	@FunctionalInterface
	private static interface NumericFunction1 extends NumericFunction {
		double process(final double value);
	}

	@FunctionalInterface
	private static interface NumericFunction2 extends NumericFunction {
		double process(final double value1, final double value2);
	}

	private static final char[]				FUNC_SQRT = "sqrt".toCharArray(); 
	private static final char[]				FUNC_SIN = "sin".toCharArray(); 
	private static final char[]				FUNC_COS = "cos".toCharArray(); 
	private static final char[]				FUNC_TAN = "tan".toCharArray(); 
	private static final char[]				FUNC_ASIN = "asin".toCharArray(); 
	private static final char[]				FUNC_ACOS = "acos".toCharArray(); 
	private static final char[]				FUNC_ATAN = "atan".toCharArray(); 
	private static final char[]				FUNC_ATAN2 = "atan2".toCharArray(); 
	private static final char[]				FUNC_SINH = "sinh".toCharArray(); 
	private static final char[]				FUNC_COSH = "cosh".toCharArray(); 
	private static final char[]				FUNC_TANH = "tanh".toCharArray(); 
	private static final char[]				FUNC_LOG = "log".toCharArray(); 
	private static final char[]				FUNC_LOG10 = "log10".toCharArray(); 
	private static final char[]				FUNC_EXP = "exp".toCharArray(); 
	private static final char[][]			FUNC_LIST = {FUNC_SQRT, FUNC_SIN, FUNC_COS, FUNC_TAN, FUNC_ASIN, FUNC_ACOS, FUNC_ATAN, FUNC_ATAN2, FUNC_SINH, FUNC_COSH, FUNC_TANH, FUNC_LOG, FUNC_LOG10, FUNC_EXP};
	private static final NumericFunction[]	FUNC_CALLBACK = {
												nf1((p1)->Math.sqrt(p1)), 
												nf1((p1)->Math.sin(p1)), 
												nf1((p1)->Math.cos(p1)), 
												nf1((p1)->Math.tan(p1)), 
												nf1((p1)->Math.asin(p1)), 
												nf1((p1)->Math.acos(p1)), 
												nf1((p1)->Math.atan(p1)), 
												nf2((p1,p2)->Math.atan2(p1,p2)), 
												nf1((p1)->Math.sinh(p1)), 
												nf1((p1)->Math.cosh(p1)), 
												nf1((p1)->Math.tanh(p1)), 
												nf1((p1)->Math.log(p1)), 
												nf1((p1)->Math.log10(p1)), 
												nf1((p1)->Math.exp(p1)) 
											};
	
	final long[]	forInteger = new long[2];
	final double[]	forReal = new double[2];
	final Change[]	forChange = new Change[1];
	final ReusableInstances<StandardResolverLocal>	ri = new ReusableInstances<StandardResolverLocal>(()->new StandardResolverLocal());
	private final PluginDescriptor				DESC = new PluginDescriptor(){
													@SuppressWarnings("unchecked")
													@Override public IFProExternalEntity<StandardResolverGlobal,StandardResolverLocal> getPluginEntity() {return new ExternalPluginEntity<StandardResolverGlobal,StandardResolverLocal>(1, PLUGIN_NAME, PLUGIN_PRODUCER, PLUGIN_VERSION, new StandardResolver());}
													@Override public String getPluginPredicate() {return null;}
													@Override public String getPluginDescription() {return PLUGIN_DESCRIPTION;}
												};
	private final FunctionCall[]				FUNCS = new FunctionCall[FUNC_LIST.length];
												
	public StandardResolver() {
		
	}
												
	@Override
	public PluginDescriptor[] getPluginDescriptors() {
		return new PluginDescriptor[]{DESC};
	}
	
	@Override public String getName() {return PLUGIN_NAME;}
	@Override public DottedVersion getVersion() {return null;}

	@Override
	public StandardResolverGlobal onLoad(final LoggerFacade log, final SubstitutableProperties parameters, final IFProEntitiesRepo repo) throws SyntaxException  {
		final StandardResolverGlobal							desc = new StandardResolverGlobal();
		final Set<Long>									ids = new HashSet<>();
		final Map<Long,QuickIds<RegisteredEntities>>	registered = new HashMap<>();
		
		desc.log = log;			
		desc.parameters = parameters;		

		for(int index = 0; index < FUNCS.length; index++) {
			FUNCS[index] = new FunctionCall(repo.termRepo().placeName(FUNC_LIST[index], 0, FUNC_LIST[index].length, null), FUNC_CALLBACK[index]);
		}
		Arrays.sort(FUNCS);
		
		try(LoggerFacade 	actualLog = log.transaction("StandardResolver:onLoad")) {
			
			for (RegisteredOperators<RegisteredEntities> item : OPS) {
				actualLog.message(Severity.info,"Register operator %1$s, %2$s...", item.text, item.type);
				final long			itemId = repo.termRepo().placeName((CharSequence)item.text, null);
				IFProOperator[]		op;
			
				if ((op = repo.getOperatorDef(itemId,item.priority,item.priority,item.type.getSort())).length == 0) {
					final IFProOperator			def = new OperatorDefEntity(item.priority, item.type, new long[] {itemId}); 
					
					repo.putOperatorDef(def);	
					ids.add(itemId);
					FProUtil.fillQuickIds(registered, new QuickIds<RegisteredEntities>(def,item.action));
				}
				else {
					FProUtil.fillQuickIds(registered,new QuickIds<RegisteredEntities>(op[0],item.action));					
				}
				if (repo.classify(itemId) != Classification.operator) {
					actualLog.message(Severity.info,"Operator registration failed for %1$s, %2$s: item classified as %3%s", item.text, item.type, repo.classify(itemId));
					throw new IllegalArgumentException("Attempt to register operator ["+item+"] failed: not classified!"); 
				}
				actualLog.message(Severity.info,"Operator %1$s, %2$s was registered successfully", item.text, item.type);
			}
			
			try{final IFProParserAndPrinter 	pap = new ParserAndPrinter(log,parameters,repo);
				for (RegisteredPredicates<RegisteredEntities> item : PREDS) {
					actualLog.message(Severity.info,"Register predicate %1$s...", item.text);
					try{pap.parseEntities(item.text.toCharArray(), 0, new FProParserCallback(){
													@Override
													public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
														ids.add(entity.getEntityId());
														FProUtil.fillQuickIds(registered, new QuickIds<RegisteredEntities>(entity, item.action));
														return true;
													}
												}
						);
					} catch (SyntaxException | IOException exc) {
						actualLog.message(Severity.info,"Predicate registration failed for %1$s: %2$s", item.text, exc.getLocalizedMessage());
						throw new IllegalArgumentException("Attempt to register predicate ["+item+"] failed: "+exc.getLocalizedMessage(),exc); 
					}
					actualLog.message(Severity.info,"Predicate %1$s was registeded successfully", item.text);
				}
			} catch (SyntaxException exc) {
				actualLog.message(Severity.info,"Problem creating predicate parser: %1$s", exc.getLocalizedMessage());
				throw new IllegalArgumentException("Attempt to register predicates failed: "+exc.getLocalizedMessage(),exc); 
			}
			actualLog.rollback();
		}
		for (Entry<Long, QuickIds<RegisteredEntities>> item : registered.entrySet()) {
			desc.registered.put(item.getKey(), item.getValue());
			desc.registeredIds.add(item.getKey());
		}
		desc.prepared = true;
		desc.repo = repo;
		desc.pap = new ParserAndPrinter(desc.log, desc.parameters, desc.repo);
		return desc;
	}

	@Override
	public void onRemove(final StandardResolverGlobal global) throws SyntaxException, NullPointerException {
		if (global == null) {
			throw new NullPointerException("Global object can't be null!");
		}
		else {
			final StandardResolverGlobal	data = (StandardResolverGlobal)global;
			
			if (!data.prepared) {
				throw new IllegalStateException("Attempt to close non-prepared item! Call prepare(repo) first!");
			}
			else {
				for (long item : data.registeredIds) {
					QuickIds<?>	start = data.registered.get(item);
					
					while (start != null) {
						data.repo.termRepo().removeName(start.id);
						start = start.next;
					}
				}
			}
		}
	}

	@Override
	public StandardResolverLocal beforeCall(final StandardResolverGlobal global, final IFProGlobalStack gs, final List<IFProVariable> vars, final IFProCallback callback) throws SyntaxException, NullPointerException {
		if (global == null) {
			throw new NullPointerException("Global object can't be null!");
		}
		else if (gs == null) {
			throw new NullPointerException("Global stack descriptor can't be null!");
		}
		else if (vars == null) {
			throw new NullPointerException("Resolved variables list can't be null!");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null!");
		}
		else {
			final StandardResolverLocal	desc = ri.allocate();
			
			desc.pap = global.pap;
			desc.callback = callback;
			desc.stack = gs;
			desc.vars = vars;
			return desc;
		}
	}

	@Override
	public ResolveRC firstResolve(final StandardResolverGlobal global, final StandardResolverLocal local, final IFProEntity entity) throws SyntaxException, NullPointerException {
		if (global == null) {
			throw new NullPointerException("Global object can't be null!");
		} 
		else if (local == null) {
			throw new NullPointerException("Local object can't be null!");
		}
		else if (entity == null) {
			throw new NullPointerException("Entity to resolve can't be null!");
		}
		else {
			return firstResolveInternal(global, local, entity);
		}
	}

	@Override
	public ResolveRC nextResolve(final StandardResolverGlobal global, final StandardResolverLocal local, IFProEntity entity) throws SyntaxException, NullPointerException {
		if (global == null) {
			throw new NullPointerException("Global object can't be null!");
		} 
		else if (local == null) {
			throw new NullPointerException("Local object can't be null!");
		}
		else if (entity == null) {
			throw new NullPointerException("Entity to resolve can't be null!");
		}
		else {
			try{
				return nextResolveInternal(global, local, entity);
			} catch (PrintingException e) {
				throw new SyntaxException(0, 0,e.getLocalizedMessage(),e);
			} 
		}
	}

	@Override
	public void endResolve(final StandardResolverGlobal global, final StandardResolverLocal local, final IFProEntity entity) throws SyntaxException, NullPointerException {
		if (global == null) {
			throw new NullPointerException("Global object can't be null!");
		} 
		else if (local == null) {
			throw new NullPointerException("Local object can't be null!");
		}
		else if (entity == null) {
			throw new NullPointerException("Entity to resolve can't be null!");
		}
		else {
			endResolveInternal(global, local, entity);
		}
	}

	@Override
	public void afterCall(final StandardResolverGlobal global, final StandardResolverLocal local) throws SyntaxException, NullPointerException {
		if (global == null) {
			throw new NullPointerException("Global descriptor can't be null");
		}
		else if (local == null) {
			throw new NullPointerException("Local descriptor can't be null");
		}
		else {
			local.vars = null;		
			local.pap = null;
			local.callback = null;
			local.stack = null;
			ri.free(local);
		}
	}

	private ResolveRC resolveRule(final StandardResolverGlobal global, final StandardResolverLocal local, final IFProEntity entity) throws SyntaxException {
		final StandardResolverLocal	newLocal = beforeCall(global, local.stack, local.vars, local.callback);
		ResolveRC 	rc;
		
		try{if ((rc = firstResolveInternal(global,newLocal,entity)) == ResolveRC.True) {
				do {if (local.varNames == null) {
						int index = 0;
						
						local.varNames = new String[local.vars.size()];
						for (IFProVariable var : local.vars) {
							local.varNames[index++] = global.repo.termRepo().getName(var.getEntityId()); 
						}
					}
					rc = executeCallback(local.callback,local.vars,local.varNames,global.repo,local.pap) ? ResolveRC.True : ResolveRC.UltimateFalse;
				} while (rc != ResolveRC.UltimateFalse && (rc = nextResolveInternal(global, newLocal, entity)) == ResolveRC.True);
				
				endResolve(global,newLocal,entity);
				if (rc == ResolveRC.False) {
					return ResolveRC.True;
				}
				else if (rc == ResolveRC.FalseWithoutBacktracking) {
					return ResolveRC.TrueWithoutBacktracking;
				}
			}
		} catch (PrintingException e) {
			throw new SyntaxException(0, 0,e.getLocalizedMessage());
		} finally {
			afterCall(global, newLocal);
		}
		return rc;
	}
	
	private ResolveRC firstResolveInternal(final StandardResolverGlobal global, final StandardResolverLocal local, final IFProEntity entity) throws SyntaxException {
		if (global.trace) {
			printResolution("first", global, local, entity);
		}
		
		switch (FProUtil.detect(global.registered, entity, RegisteredEntities.Others)) {
			case Op1200xfxGoal		:
				return (firstResolveInternal(global,local,((IFProOperator)entity).getRight()));
			case Op1200fxGoal		:
			case Op1200fxQuestion	:
				if (local.callback != null) {
					local.callback.beforeFirstCall();
				}					
				if ((firstResolveInternal(global,local,((IFProOperator)entity).getRight())) == ResolveRC.True) {
					if (local.callback != null) {
						if (local.varNames == null) {
							int index = 0;
							
							local.varNames = new String[local.vars.size()];
							for (IFProVariable var : local.vars) {
								local.varNames[index++] = global.repo.termRepo().getName(var.getEntityId()); 
							}
						}
						return executeCallback(local.callback,local.vars,local.varNames,global.repo,local.pap) ? ResolveRC.True : ResolveRC.UltimateFalse;
					}
					else {
						return ResolveRC.True;
					}
				}
				else {
					return ResolveRC.False;
				}
			case Op1100xfyOr		:
				ResolveRC		rcOr = firstResolveInternal(global,local,((IFProOperator)entity).getLeft()); 
				
				if (rcOr == ResolveRC.True) {
					local.stack.push(GlobalStack.getOrChainStackTop(entity,true));
					return ResolveRC.True;
				}
				else if (rcOr == ResolveRC.False) {
					rcOr = firstResolveInternal(global,local,((IFProOperator)entity).getRight()); 
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
				ResolveRC	rcAnd = firstResolveInternal(global,local,((IFProOperator)entity).getLeft());
				
				if (rcAnd == ResolveRC.True) {
					do {rcAnd = firstResolveInternal(global,local,((IFProOperator)entity).getRight()); 
						if (rcAnd == ResolveRC.True) {
							return ResolveRC.True;
						}
						else if (rcAnd != ResolveRC.False) {
							break;
						}
					} while ((rcAnd = nextResolve(global,local,((IFProOperator)entity).getLeft())) == ResolveRC.True);
					endResolveInternal(global,local,((IFProOperator)entity).getLeft());
					return rcAnd;
				}
				else {
					return	rcAnd;
				}
			case Op900fyNot			:
				ResolveRC	rcNot = firstResolveInternal(global,local,((IFProOperator)entity).getRight()); 
				
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
				return FProUtil.unify(entity,((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight(),local.stack,forChange) ? ResolveRC.True : ResolveRC.False;
			case Op700xfxNotUnify	:
				return !FProUtil.unify(entity,((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight(),local.stack,forChange)  ? ResolveRC.True : ResolveRC.False;
			case Op700xfx2List		:
				final IFProEntity	left, right, created;
				
				if (containsVars(((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight())) {
					return ResolveRC.False;
				}
				else if (!FProUtil.isEntityA(((IFProOperator)entity).getLeft(),ContentType.NonVar) && ((IFProOperator)entity).getRight().getEntityType() == EntityType.list) {
					left = ((IFProOperator)entity).getLeft();
					created = right = list2Predicate((IFProList) ((IFProOperator)entity).getRight());
				} 
				else if (((IFProOperator)entity).getLeft().getEntityType() == EntityType.predicate) {
					created = left = predicate2List((IFProPredicate) ((IFProOperator)entity).getLeft());
					right = ((IFProOperator)entity).getRight();
				}
				else {
					return ResolveRC.False;
				}
				return FProUtil.unifyTemporaries(entity, left, right, created, global.repo.stringRepo(), local.stack, forChange) ? ResolveRC.True : ResolveRC.False;
			case Op700xfxLess		:
				return compare(global,entity) < 0 ?  ResolveRC.True : ResolveRC.False;
			case Op700xfxLessEqual	:
				return compare(global,entity) <= 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxEqColon	:
				return compare(global,entity) == 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxNotEqual2	:
				return compare(global,entity) != 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxGreater	:
				return compare(global,entity) > 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxGreaterEqual	:
				return compare(global,entity) >= 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxDogLess	:
				return lexicalCompare(global.repo, (IFProOperator)entity) < 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxDogLessEqual	:
				return lexicalCompare(global.repo, (IFProOperator)entity) <= 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxDogGreater	:
				return lexicalCompare(global.repo, (IFProOperator)entity) > 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxDogGreaterEqual	:
				return lexicalCompare(global.repo, (IFProOperator)entity) >= 0 ? ResolveRC.True : ResolveRC.False;
			case Op700xfxEqual		:
				return FProUtil.isIdentical(((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight()) ? ResolveRC.True : ResolveRC.False;
			case Op700xfxNotEqual	:
				return !FProUtil.isIdentical(((IFProOperator)entity).getLeft(),((IFProOperator)entity).getRight()) ? ResolveRC.True : ResolveRC.False;
			case Op700xfxIs			:
				return FProUtil.unify(entity,((IFProOperator)entity).getLeft(),calculate(global,((IFProOperator)entity).getRight()),local.stack,forChange) ? ResolveRC.True : ResolveRC.False;
			case Op400yfxDivide		:
				return iterate((StandardResolverGlobal)global,(StandardResolverLocal)local,entity,entity,new IterablesCollection.IterableNameAndArity(global.repo,(IFProOperator)entity));
			case PredTrace			:
				global.trace = true;
				return ResolveRC.True;
			case PredNoTrace		:
				global.trace = false;
				return ResolveRC.True;
			case PredSpy			:
			case PredCut			:
			case PredTrue			:
			case PredRepeat			:
				return ResolveRC.True;
			case PredFail			:
				return ResolveRC.False;
			case PredAssertA		:
				global.repo.predicateRepo().assertA(FProUtil.duplicate(((IFProPredicate)entity).getParameters()[0]));
				return ResolveRC.True;
			case PredAssertZ		:
				global.repo.predicateRepo().assertZ(FProUtil.duplicate(((IFProPredicate)entity).getParameters()[0]));
				return ResolveRC.True;
			case PredRetract		:
				return global.repo.predicateRepo().retractFirst(((IFProPredicate)entity).getParameters()[0]) ? ResolveRC.True : ResolveRC.False;
			case PredCall			:
	 			return iterate((StandardResolverGlobal)global,(StandardResolverLocal)local,entity,((IFProPredicate)entity).getParameters()[0],new IterablesCollection.IterableCall(global.repo,(IFProPredicate)entity));
			case PredVar			:
				return isEntityA(entity, FProUtil.ContentType.Var);
			case PredNonVar			:
				return isEntityA(entity, FProUtil.ContentType.NonVar);
			case PredAtom			:
				return isEntityA(entity, FProUtil.ContentType.Atom);
			case PredInteger		:
				return isEntityA(entity, FProUtil.ContentType.Integer);
			case PredFloat			:
				return isEntityA(entity, FProUtil.ContentType.Float);
			case PredNumber			:
				return isEntityA(entity, FProUtil.ContentType.Number);
			case PredAtomic			:
				return isEntityA(entity, FProUtil.ContentType.Atomic);
			case PredCompound		:
				return isEntityA(entity, FProUtil.ContentType.Compound);
			case PredFunctor		:
				final IFProEntity	left1, right1, created1;
				
				if (containsVars(((IFProPredicate)entity).getParameters()) || containsVars(((IFProPredicate)entity).getParameters()[0],((IFProPredicate)entity).getParameters()[1])) {
					return ResolveRC.False;
				}
				else if (((IFProPredicate)entity).getParameters()[0].getEntityType() == EntityType.predicate) {
					created = left = new PredicateEntity(((IFProPredicate)entity).getParameters()[0].getEntityId());
					right = ((IFProPredicate)entity).getParameters()[1];
					
					if (FProUtil.unifyTemporaries(entity, left, right, created, global.repo.stringRepo(), local.stack, forChange)) {
						created1 = left1 = new IntegerEntity(((IFProPredicate)((IFProPredicate)entity).getParameters()[0]).getArity());
						right1 = ((IFProPredicate)entity).getParameters()[2];
						
						if (FProUtil.unifyTemporaries(entity, left1, right1, created1, global.repo.stringRepo(), local.stack, forChange)) {
							return ResolveRC.True;
						}
					}
					FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
					return ResolveRC.False;
				}
				else if (containsVars(((IFProPredicate)entity).getParameters()[0]) && ((IFProPredicate)entity).getParameters()[1].getEntityType() == EntityType.predicate && ((IFProPredicate)entity).getParameters()[2].getEntityType() == EntityType.integer) {
					final IFProEntity[]		content = new IFProEntity[(int) ((IFProPredicate)entity).getParameters()[2].getEntityId()];
					
					for (int index = 0, maxIndex = content.length; index < maxIndex; index++) {
						content[index] = new AnonymousEntity();
					}
					left = ((IFProPredicate)entity).getParameters()[0];
					right = created = new PredicateEntity(((IFProPredicate)entity).getParameters()[1].getEntityId(),content);
					
					return FProUtil.unifyTemporaries(entity, left, right, created, global.repo.stringRepo(), local.stack, forChange) ? ResolveRC.True : ResolveRC.False;
				}
				else {
					return ResolveRC.False;
				}
			case PredArg			:
				if (((IFProPredicate)entity).getParameters()[0].getEntityType() == EntityType.predicate 
					&& ((IFProPredicate)entity).getParameters()[1].getEntityType() == EntityType.integer
					&& ((IFProPredicate)entity).getParameters()[1].getEntityId() > 0 
					&& ((IFProPredicate)entity).getParameters()[1].getEntityId() <= ((IFProPredicate)((IFProPredicate)entity).getParameters()[0]).getArity()) {
					
					created = left = FProUtil.duplicate(((IFProPredicate)((IFProPredicate)entity).getParameters()[0]).getParameters()[(int)((IFProPredicate)entity).getParameters()[1].getEntityId()-1]);
					right = ((IFProPredicate)entity).getParameters()[2];
					return FProUtil.unifyTemporaries(entity, left, right, created, global.repo.stringRepo(), local.stack, forChange) ? ResolveRC.True : ResolveRC.False;
				}
				else {
					return ResolveRC.False;
				}
			case PredName			:
				if (containsVars(((IFProPredicate)entity).getParameters())) {
					return ResolveRC.False;
				}
				else if (containsVars(((IFProPredicate)entity).getParameters()[1])) {
					final StringBuilder	sb = new StringBuilder();

					try{global.pap.putEntity(((IFProPredicate)entity).getParameters()[0],new StringBuilderCharTarget(sb));
						final long	newId = global.repo.stringRepo().placeName((CharSequence)sb.toString(),null);

						left = created = new StringEntity(newId);
						right = ((IFProPredicate)entity).getParameters()[1];
						
						return FProUtil.unifyTemporaries(entity, left, right, created, global.repo.stringRepo(), local.stack, forChange) ? ResolveRC.True : ResolveRC.False;
					} catch (ContentException | IOException e) {
						return ResolveRC.False;
					}
				}
				else {
					if (((IFProPredicate)entity).getParameters()[1].getEntityType() == EntityType.string) {
						final IFProEntity[]	result = new IFProEntity[1];
						
						try{global.pap.parseEntities(new SyntaxTreeCharacterSource<>(global.repo.stringRepo(),((IFProPredicate)entity).getParameters()[1].getEntityId()),(e,v)->{result[0] = e; return false;});
							
							left = ((IFProPredicate)entity).getParameters()[0];
							right = created = result[0];
							
							return FProUtil.unifyTemporaries(entity, left, right, created, global.repo.stringRepo(), local.stack, forChange) ? ResolveRC.True : ResolveRC.False;
						} catch (ContentException | IOException e) {
							return ResolveRC.False;
						}
					}
					else {
						return ResolveRC.False;
					}
				}
			case PredBagOf			:
				IFProList	bagofList = getBagof(global.repo,(IFProPredicate)entity);
				
				if (bagofList.getChild() == null && bagofList.getTail() == null) {
					return ResolveRC.False;
				}
				else {
					return FProUtil.unify(entity, ((IFProPredicate)entity).getParameters()[2],bagofList,local.stack,forChange) ? ResolveRC.True : ResolveRC.False;
				}
			case PredSetOf			:
				IFProList	setofList = getBagof(global.repo,(IFProPredicate)entity);
				
				if (setofList.getChild() == null && setofList.getTail() == null) {
					return ResolveRC.False;
				}
				else {
					return FProUtil.unify(entity, ((IFProPredicate)entity).getParameters()[2],orderSetofList(global.repo,setofList),local.stack,forChange) ? ResolveRC.True : ResolveRC.False;
				}
			case PredFindAll		:
				if (FProUtil.unify(entity, ((IFProPredicate)entity).getParameters()[2],getBagof(global.repo,(IFProPredicate)entity),local.stack,forChange)) {
					return  ResolveRC.True;
				}
				else {
					return FProUtil.unify(entity, ((IFProPredicate)entity).getParameters()[2],new ListEntity(null,null),local.stack,forChange) ? ResolveRC.True : ResolveRC.False;
				}
			case PredMemberOf		:
				return iterate((StandardResolverGlobal)global,(StandardResolverLocal)local,entity,((IFProPredicate)entity).getParameters()[0],new IterablesCollection.IterableList((IFProPredicate) entity));
			default :
				final ExternalEntityDescriptor	eed = global.repo.pluginsRepo().getResolver(entity);
				
				if (eed != null) {
					final IResolvable 	resolver = eed.getResolver();
					final Object		localData = resolver.beforeCall(eed.getGlobal(), local.stack, local.vars, local.callback);
					
					if (resolver.firstResolve(eed.getGlobal(), localData, entity) == ResolveRC.True) {
						local.stack.push(GlobalStack.getExternalStackTop(entity, eed, localData));
						return ResolveRC.True;
					}
					else {
						resolver.endResolve(eed.getGlobal(), localData, entity);
						resolver.afterCall(eed.getGlobal(), localData);
						return ResolveRC.False;
					}
				}
				else {
					return iterate((StandardResolverGlobal)global, (StandardResolverLocal)local, entity, entity, global.repo.predicateRepo().call(entity));
				}
		}
	}	
	
	private ResolveRC nextResolveInternal(final StandardResolverGlobal global, final StandardResolverLocal local, IFProEntity entity) throws SyntaxException, PrintingException {
		if (global.trace) {
			printResolution("next", global, local, entity);
		}
		switch (FProUtil.detect(global.registered,entity,RegisteredEntities.Others)) {
			case Op1200xfxGoal		:
				ResolveRC	rc = nextResolveInternal(global,local,((IFProOperator)entity).getRight());
				
				return rc == ResolveRC.FalseWithoutBacktracking ? ResolveRC.False : rc;	// Processing cut(!) retcode
			case Op1200fxGoal		:
			case Op1200fxQuestion	:
				rc = (nextResolveInternal(global,local,((IFProOperator)entity).getRight()));
				if (rc == ResolveRC.True) {
					if (local.callback != null) {
						if (!executeCallback(local.callback,local.vars,local.varNames,global.repo,local.pap)) {
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
				if (local.stack.getTopType() == StackTopType.orChain) {
					if (((OrChainStackTop)local.stack.pop()).isFirst()) {
						ResolveRC	rcOr = nextResolveInternal(global,local,((IFProOperator)entity).getLeft());
						
						if (rcOr == ResolveRC.False) {
							endResolveInternal(global,local,((IFProOperator)entity).getLeft());
							
							rcOr = firstResolveInternal(global,local,((IFProOperator)entity).getRight()); 
							local.stack.push(GlobalStack.getOrChainStackTop(entity,false));
							return rcOr;
						}
						else {
							local.stack.push(GlobalStack.getOrChainStackTop(entity,true));
							return rcOr;
						}
					}
					else {
						ResolveRC	rcOr = nextResolveInternal(global,local,((IFProOperator)entity).getRight());
						
						local.stack.push(GlobalStack.getOrChainStackTop(entity,false));
						return rcOr;
					}
				}
				else {
					throw new IllegalStateException("OR record is missing in the stack!");
				}
			case Op1000xfyAnd		:
				ResolveRC	rcAnd = nextResolveInternal(global,local,((IFProOperator)entity).getRight());
				
				if (rcAnd != ResolveRC.True) {
					endResolveInternal(global,local,((IFProOperator)entity).getRight());
					
					if (rcAnd == ResolveRC.False) {
						while((rcAnd = nextResolveInternal(global,local,((IFProOperator)entity).getLeft())) == ResolveRC.True) {
							rcAnd = firstResolve(global,local,((IFProOperator)entity).getRight()); 
							
							if (rcAnd == ResolveRC.True) {
								return ResolveRC.True;
							}
							else if (rcAnd != ResolveRC.False) {
								break;
							}
						}
					}
					endResolveInternal(global,local,((IFProOperator)entity).getLeft());
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
				return continueIterate((StandardResolverGlobal)global,(StandardResolverLocal)local,entity,()->entity);
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
				return continueIterate((StandardResolverGlobal)global,(StandardResolverLocal)local,entity,()->((IFProPredicate)entity).getParameters()[0]);
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
				return continueIterate((StandardResolverGlobal)global,(StandardResolverLocal)local,entity,()->((IFProPredicate)entity).getParameters()[0]);
			default :
				if (local.stack.getTopType() == StackTopType.external) {
					final ExternalStackTop				est = (ExternalStackTop) local.stack.pop();
					final IResolvable<Object, Object>	resolver = est.getDescriptor().getResolver();
					
					if (resolver.nextResolve(est.getDescriptor().getGlobal(), est.getLocalData(), entity) == ResolveRC.True) {
						local.stack.push(est);
						return ResolveRC.True;
					}
					else {
						local.stack.push(est);
						return ResolveRC.False;
					}
				}
				else {
					return continueIterate((StandardResolverGlobal)global, (StandardResolverLocal)local, entity, ()->entity);
				}
		}
	}
	
	private void endResolveInternal(final StandardResolverGlobal global, final StandardResolverLocal local, final IFProEntity entity) throws SyntaxException {
		if (global.trace) {
			printResolution("end", global, local, entity);
		}
		switch (FProUtil.detect(global.registered,entity,RegisteredEntities.Others)) {
			case Op1200xfxGoal		:
				break;
			case Op1200fxGoal		:
			case Op1200fxQuestion	:
				endResolveInternal(global,local,((IFProOperator)entity).getRight());
				if (local.callback != null) {
					local.callback.afterLastCall();
					local.varNames = null;
				}					
				break;
			case Op1100xfyOr		:
				if (local.stack.getTopType() == StackTopType.orChain) {
					if (((OrChainStackTop)local.stack.pop()).isFirst()) {
						endResolveInternal(global,local,((IFProOperator)entity).getLeft());
					}
					else {
						endResolveInternal(global,local,((IFProOperator)entity).getRight());
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
				FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
				break;
			case Op700xfx2List		:
				FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
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
				FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
				break;
			case Op400yfxDivide		:
				endIterate(entity, global.repo.stringRepo(), local.stack);
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
				endIterate(entity, global.repo.stringRepo(), local.stack);
				break;
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
				FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
			case PredArg			:
			case PredName			:
				FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
				break;
			case PredBagOf			:
			case PredSetOf			:
			case PredFindAll		:
				if (local.stack.getTopType() == StackTopType.bounds
					&& ((BoundStackTop<Change>)local.stack.peek()).getMark() == entity) {
					FProUtil.unbind(((BoundStackTop<Change>)local.stack.pop()).getChangeChain());
				}
				break;
			case PredMemberOf		:
				endIterate(entity, global.repo.stringRepo(), local.stack);
				break;
			default :
				if (local.stack.getTopType() == StackTopType.external) {
					final ExternalStackTop	est = (ExternalStackTop) local.stack.pop();
					final IResolvable<Object, Object> resolver = est.getDescriptor().getResolver();
					
					resolver.endResolve(est.getDescriptor().getGlobal(), est.getLocalData(), entity);
					resolver.afterCall(est.getDescriptor().getGlobal(), est.getLocalData());
					return;
				}
				endIterate(entity, global.repo.stringRepo(), local.stack);
		}
	}	

	private static void printResolution(final String prefix, final StandardResolverGlobal global, final StandardResolverLocal local, final IFProEntity entity) {
		final StringBuilder		sb = new StringBuilder();
		final CharacterTarget	ct = new StringBuilderCharTarget(sb);
		
		try{local.pap.putEntity(entity,ct);
			System.err.println(">"+prefix+": "+sb);
			System.err.flush();
		} catch (PrintingException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "StandardResolver [getPluginDescriptors()=" + Arrays.toString(getPluginDescriptors()) + ", getName()=" + getName() + ", getVersion()=" + getVersion() + "]";
	}

	private boolean containsVars(final IFProEntity entity) {
		return !FProUtil.isEntityA(entity, ContentType.NonVar);
	}
	
	private boolean containsVars(final IFProEntity entity1, final IFProEntity entity2) {
		return !FProUtil.isEntityA(entity1,ContentType.NonVar) && !FProUtil.isEntityA(entity2,ContentType.NonVar);
	}

	private boolean containsVars(final IFProEntity... entities) {
		for (IFProEntity item : entities) {
			if (FProUtil.isEntityA(item,ContentType.NonVar)) {
				return false;
			}
		}
		return true;
	}
	
	private IFProEntity calculate(final StandardResolverGlobal global, final IFProEntity value) {
		try{
			return calculate(global, value, FUNCS, forInteger, forReal);
		} catch (UnsupportedOperationException exc) {
			return null;
		}
	}

	static IFProEntity calculate(final StandardResolverGlobal global, final IFProEntity value, final FunctionCall[] funcs, final long[] forInteger, final double[] forReal) throws NullPointerException {
		if (value == null) {
			throw new NullPointerException("Null entity to calculate value");
		}
		else {
			IFProEntity		right;
			
			switch (value.getEntityType()) {
				case integer	:
				case real		:
					return value;
				case predicate	:
					for(FunctionCall item : funcs) {
						if (item.callId == value.getEntityId() && item.arity == ((IFProPredicate)value).getArity()) {
							IFProEntity	first, second;
							
							switch (item.arity) {
								case 1	:
									first = calculate(global, getPredicateParameter(value, 0), funcs, forInteger, forReal);
									
									return new RealEntity(item.number1.process(getDoubleValue(first)));
								case 2	:
									first = calculate(global, getPredicateParameter(value, 0), funcs, forInteger, forReal);
									second = calculate(global, getPredicateParameter(value, 1), funcs, forInteger, forReal);
									
									return new RealEntity(item.number2.process(getDoubleValue(first), getDoubleValue(second)));
								default :
									return new RealEntity(Double.NaN);
							}
						}
					}
					return new RealEntity(Double.NaN);
				case operator	:
					switch (FProUtil.detect(global.registered,value,RegisteredEntities.Others)) {
						case Op500yfxPlus		:
							if (convert2Real(calculate(global,((IFProOperator)value).getLeft(),funcs,forInteger,forReal),calculate(global,((IFProOperator)value).getRight(),funcs,forInteger,forReal),forInteger,forReal)) {
								return new RealEntity(forReal[0] + forReal[1]);
							}
							else {
								return new IntegerEntity(forInteger[0] + forInteger[1]);
							}
						case Op500yfxMinus		:
							if (convert2Real(calculate(global,((IFProOperator)value).getLeft(),funcs,forInteger,forReal),calculate(global,((IFProOperator)value).getRight(),funcs,forInteger,forReal),forInteger,forReal)) {
								return new RealEntity(forReal[0] - forReal[1]);
							}
							else {
								return new IntegerEntity(forInteger[0] - forInteger[1]);
							}
						case Op400yfxMultiply	:
							if (convert2Real(calculate(global,((IFProOperator)value).getLeft(),funcs,forInteger,forReal),calculate(global,((IFProOperator)value).getRight(),funcs,forInteger,forReal),forInteger,forReal)) {
								return new RealEntity(forReal[0] * forReal[1]);
							}
							else {
								return new IntegerEntity(forInteger[0] * forInteger[1]);
							}
						case Op400yfxDivide		:
							if (convert2Real(calculate(global,((IFProOperator)value).getLeft(),funcs,forInteger,forReal),calculate(global,((IFProOperator)value).getRight(),funcs,forInteger,forReal),forInteger,forReal)) {
								return new RealEntity(forReal[0] / forReal[1]);
							}
							else {
								return new RealEntity(1.00*forInteger[0] / forInteger[1]);
							}
						case Op400yfxIntDivide	:
							if (convert2Real(calculate(global,((IFProOperator)value).getLeft(),funcs,forInteger,forReal),calculate(global,((IFProOperator)value).getRight(),funcs,forInteger,forReal),forInteger,forReal)) {
								return new RealEntity((long)(forReal[0] / forReal[1]));
							}
							else {
								return new IntegerEntity(forInteger[0] / forInteger[1]);
							}
						case Op400yfxMod		:
							if (convert2Real(calculate(global,((IFProOperator)value).getLeft(),funcs,forInteger,forReal),calculate(global,((IFProOperator)value).getRight(),funcs,forInteger,forReal),forInteger,forReal)) {
								return new RealEntity(forReal[0] % forReal[1]);
							}
							else {
								return new IntegerEntity(forInteger[0] % forInteger[1]);
							}
						case Op200xfxExponent	:
							if (convert2Real(calculate(global,((IFProOperator)value).getLeft(),funcs,forInteger,forReal),calculate(global,((IFProOperator)value).getRight(),funcs,forInteger,forReal),forInteger,forReal)) {
								return new RealEntity(Math.pow(forReal[0], forReal[1]));
							}
							else {
								return new IntegerEntity((long)Math.pow(1.00*forInteger[0], 1.00*forInteger[1]));
							}
						case Op200fyMinus		:
							right = calculate(global,((IFProOperator)value).getRight(),funcs,forInteger,forReal);
							if (right != null) {
								if (right.getEntityType() == EntityType.real) {
									return new RealEntity(-Double.longBitsToDouble(right.getEntityId()));
								}
								else {
									return new IntegerEntity(-right.getEntityId()); 
								}
							}
							else {
								throw new NullPointerException("Prefixed Unary minus doesn't have operand");
							}
						default : 
							final IFProOperator	op = (IFProOperator)value;
							
							throw new UnsupportedOperationException("Unregistered operator op("+op.getPriority()+","+op.getOperatorType()+",id["+op.getEntityId()+"]) is used in arthmetical expressions");
					}
				default :
					throw new UnsupportedOperationException("Neither number nor operator/function is used in in arthmetical expressions (id="+value.getEntityId()+",type="+value.getEntityType()+")");
			}
		}
	}

	private static boolean convert2Real(final IFProEntity left, final IFProEntity right, final long[] forInteger, final double[] forReal) {
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
			throw new IllegalArgumentException("Only integer and real operands can be used in expression");
		}
	}

	private int compare(final StandardResolverGlobal global, final IFProEntity entity) {
		return compare(global, calculate(global, ((IFProOperator)entity).getLeft()), calculate(global,((IFProOperator)entity).getRight()));
	}
	
	private int compare(final StandardResolverGlobal global, final IFProEntity left, final IFProEntity right) {
		return compare(global, left, right, FUNCS, forInteger, forReal);
	}
	
	static int compare(final StandardResolverGlobal global,final IFProEntity left, final IFProEntity right, final FunctionCall[] funcs, final long[] forInteger, final double[] forReal) {
		final IFProEntity	leftVal = calculate(global, left, funcs, forInteger, forReal),
							rightVal = calculate(global, right, funcs, forInteger, forReal);
		
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
			if (convert2Real(leftVal, rightVal, forInteger, forReal)) {
				return forReal[0] < forReal[1] ? -1 : (forReal[0] > forReal[1] ? 1 : 0);
			}
			else {
				return forInteger[0] < forInteger[1] ? -1 : (forInteger[0] > forInteger[1] ? 1 : 0);
			}
		}
	}

	static int lexicalCompare(final IFProEntitiesRepo repo, final IFProOperator entity) throws SyntaxException {
		return lexicalCompare(repo, entity.getLeft(),entity.getRight());
	}	
	
	static int lexicalCompare(final IFProEntitiesRepo repo, final IFProEntity left, final IFProEntity right) throws SyntaxException {
		int compare;
		
		if (left == right) {
			return 0;
		}
		else if (left == null) {
			return -1;
		}
		else if (right == null) {
			return 1;
		}
		else if ((compare = left.getEntityType().compareTo(right.getEntityType())) != 0) {
			return compare; 
		}
		else {
			switch (left.getEntityType()) {
				case string		:
					return repo.stringRepo().compareNames(left.getEntityId(), right.getEntityId());
				case integer	:
					final long	leftLong = left.getEntityId(), rightLong = right.getEntityId();  
					
					return leftLong < rightLong ? -1 : (leftLong > rightLong ? 1 : 0);
				case real		:
					final double	leftDouble = Double.longBitsToDouble(left.getEntityId()), rightDouble = Double.longBitsToDouble(right.getEntityId());
					
					return leftDouble < rightDouble ? -1 : (leftDouble > rightDouble ? 1 : 0);
				case anonymous	:
					return 0;
				case variable	:
					return repo.termRepo().compareNames(left.getEntityId(), right.getEntityId());
				case list		:
					final IFProList	leftList = (IFProList)left, rightList = (IFProList)right;
					
					if ((compare = lexicalCompare(repo,leftList.getChild(),rightList.getChild())) == 0) {
						return lexicalCompare(repo,leftList.getTail(),rightList.getTail());
					}
					else {
						return compare;
					}
				case operator	:
					if ((compare = repo.termRepo().compareNames(left.getEntityId(),right.getEntityId())) == 0) {
						final IFProOperator	leftOp = (IFProOperator)left, rightOp = (IFProOperator)right;
						
						if ((compare = lexicalCompare(repo,leftOp.getLeft(),rightOp.getLeft())) == 0) {
							return lexicalCompare(repo,leftOp.getRight(),rightOp.getRight());							
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
						final IFProPredicate	leftP = (IFProPredicate)left; 
						final IFProPredicate	rightP = (IFProPredicate)right; 
						
						if (leftP.getArity() == rightP.getArity()) {
							for (int index = 0, maxIndex = leftP.getArity(); index < maxIndex; index++) {
								if ((compare = lexicalCompare(repo,leftP.getParameters()[index],rightP.getParameters()[index])) != 0) {
									return compare;
								}
							}
							return 0;
						}
						else {
							return leftP.getArity() - rightP.getArity();
						}
					}
					else {
						return compare;
					}
				default :
					throw new UnsupportedOperationException("Entity type to compare ["+left.getEntityType()+"] is not supported yet");					
			}
		}
	}

	static IFProPredicate list2Predicate(final IFProList source) {
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
	
	static IFProList predicate2List(final IFProPredicate source) {
		IFProList	actual = new ListEntity(new PredicateEntity(source.getEntityId()),null), result = actual, next;
		
		for (int index = 0; index < source.getArity(); index++) {
			next = new ListEntity(FProUtil.duplicate(source.getParameters()[index]),null);
			next.setParent(actual);
			actual.setTail(next);
			actual = next;
		}
		return result;
	}

	private ResolveRC isEntityA(final IFProEntity entity, final ContentType type) {
		return FProUtil.isEntityA(((IFProPredicate)entity).getParameters()[0], type) ? ResolveRC.True : ResolveRC.False;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ResolveRC iterate(final StandardResolverGlobal global, final StandardResolverLocal local, final IFProEntity mark, final IFProEntity entity, final Iterable<IFProEntity> iterable) throws SyntaxException {
		local.stack.push(GlobalStack.getIteratorStackTop(mark,iterable,IFProEntity.class));
		
		while ((((Iterable)((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator()).iterator().hasNext())) {
			final IFProEntity	candidate = ((Iterable<IFProEntity>)((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator()).iterator().next(); 

			if (FProUtil.unify(mark,entity,candidate,local.stack,forChange)) {
				if (candidate.isRuled()) {	// Process ruled entities
					final IFProEntity	rule = ((IFProRuledEntity)candidate).getRule();
					
					ResolveRC			rcRule = resolveRule(global,local,rule);
					
					if (rcRule == ResolveRC.True) {
						return ResolveRC.True;
					}
					else if (rcRule == ResolveRC.False) {
						FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
						continue;
					}
					else if (rcRule == ResolveRC.FalseWithoutBacktracking) {
						FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
						scrollIterator((((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator()));
//						for (IFProEntity tmp : (((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator())) {
//						}
						break;
					}
					else if (rcRule == ResolveRC.TrueWithoutBacktracking) {
						FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
						scrollIterator(((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator());
//						for (IFProEntity tmp : (((IteratorStackTop<IFProEntity>)local.stack.peek()).getIterator())) {
//						}
						return ResolveRC.True;
					}
					else {
						FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
						break;
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

	@SuppressWarnings({ "unchecked" })
	private ResolveRC continueIterate(final StandardResolverGlobal global, final StandardResolverLocal local, final IFProEntity mark, final EntityGetter entityGetter) throws SyntaxException, PrintingException {
		IFProGlobalStack	stack = local.stack;

		while (!stack.isEmpty() && stack.peek().getEntityAssicated() == mark) {
			final GlobalStackTop item = stack.peek();
			
			switch (item.getTopType()) {
				case andChain	:
					break;
				case bounds		:
					FProUtil.unbind(((BoundStackTop<Change>)item).getChangeChain());
					stack.pop();
					break;
				case external	:
					break;
				case iterator	:
					while ((((IteratorStackTop<IFProEntity>)item).getIterator()).iterator().hasNext()) {
						final IFProEntity	candidate = ((Iterable<IFProEntity>)(((IteratorStackTop<IFProEntity>)item)).getIterator()).iterator().next(); 
						final IFProEntity	current = entityGetter.get(); 
						
						if (FProUtil.unify(mark,current,candidate,local.stack,forChange)) {
							if (candidate.isRuled()) {	// Process ruled entities
								final IFProEntity	rule = ((IFProRuledEntity)candidate).getRule();
								
								ResolveRC			rcRule = resolveRule(global,local,rule);
								
								if (rcRule == ResolveRC.True) {
									return ResolveRC.True;
								}
								else if (rcRule == ResolveRC.False) {
									FProUtil.releaseTemporaries(current, global.repo.stringRepo(), stack);
									continue;
								}
								else if (rcRule == ResolveRC.FalseWithoutBacktracking) {
									FProUtil.releaseTemporaries(current, global.repo.stringRepo(), stack);
									scrollIterator((((IteratorStackTop<IFProEntity>)item).getIterator()));
//									for (IFProEntity tmp : (((IteratorStackTop<IFProEntity>)item).getIterator())) {
//									}
									break;
								}
								else if (rcRule == ResolveRC.TrueWithoutBacktracking) {
									FProUtil.releaseTemporaries(current, global.repo.stringRepo(), stack);
									scrollIterator((((IteratorStackTop<IFProEntity>)item).getIterator()));
//									for (IFProEntity tmp : (((IteratorStackTop<IFProEntity>)item).getIterator())) {
//									}
									return ResolveRC.True;
								}
								else {
									FProUtil.releaseTemporaries(current, global.repo.stringRepo(), stack);
									break;
								}
							}
							else {
								return ResolveRC.True;
							}
						}
					}
					stack.pop();
					return ResolveRC.False;
				case orChain	:
					break;
				case temporary	:
					FProUtil.removeEntity(global.repo.stringRepo(), ((TemporaryStackTop)item).getEntity());
					stack.pop();
					break;
				default	:
					throw new UnsupportedOperationException();
			}
		}
		throw new IllegalStateException("Awaited iterator missing in the stack");
	}

	@SuppressWarnings("unused")
	public static void scrollIterator(final Iterable<?> anyIterator) {
		for(Object item : anyIterator) {
		}
	}
	
	private static void endIterate(final IFProEntity entity, final SyntaxTreeInterface<?> repo, final IFProGlobalStack stack) {
		FProUtil.releaseTemporaries(entity, repo, stack);
		if (stack.getTopType() == StackTopType.iterator && stack.peek().getEntityAssicated() == entity) {
			stack.pop();
		}
	}

	static IFProList getBagof(final IFProEntitiesRepo repo, final IFProPredicate bagof) {
		IFProList	start = new ListEntity(null,null), actual = start, pred = null; 
		
		for (IFProEntity item : new IterablesCollection.IterableCallBagof(repo,bagof)) {
			actual.setChild(item);		
			item.setParent(actual);		
			actual.setTail(new ListEntity(null,null).setParent(actual));
			pred = actual;
			actual = (IFProList) actual.getTail();
		}
		if (pred != null) {	// The same last element in the list is always empty
			pred.setTail(null);
		}
		return start;
	}

	static IFProEntity orderSetofList(final IFProEntitiesRepo repo, final IFProList setofList) throws SyntaxException {
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

	private static boolean executeCallback(final IFProCallback callback, final List<IFProVariable> vars, final String[] names, final IFProEntitiesRepo repo, final IFProParserAndPrinter pap) throws SyntaxException {
		final IFProEntity[]		resolved = new IFProEntity[names.length];
		final String[]			printedValues = new String[names.length];
		final StringBuilder		sb = new StringBuilder();
		final CharacterTarget	ct = new StringBuilderCharTarget(sb);
		int						index = 0;
		
		for (IFProVariable var : vars) {
			resolved[index] = var.getParent() == null ? var : var.getParent();
			
			try{pap.putEntity(resolved[index], ct);
				printedValues[index++] = sb.toString();
			} catch (PrintingException | IOException e) {
				printedValues[index++] = e.getLocalizedMessage();
			}
			sb.setLength(0);			
		}
		
		try{
			return callback.onResolution(names, resolved, printedValues);
		} catch (PrintingException e) {
			throw new SyntaxException(0,0,e.getLocalizedMessage(),e);
		}
	}

	private static NumericFunction nf1(final NumericFunction1 func) {
		return func;
	}

	private static NumericFunction nf2(final NumericFunction2 func) {
		return func;
	}

	private static IFProEntity getPredicateParameter(final IFProEntity value, final int index) {
		return ((IFProPredicate)value).getParameters()[index];
	}

	private static double getDoubleValue(final IFProEntity entity) {
		switch (entity.getEntityType()) {
			case integer	:
				return (double)entity.getEntityId();
			case real		:
				return Double.longBitsToDouble(entity.getEntityId());
			case anonymous : case any : case externalplugin : case list : case operator : case operatordef : case predicate : case string : case variable:
				return Double.NaN;
			default:
				throw new UnsupportedOperationException("Entity type ["+entity.getEntityType()+"] is not supported yet");
		}
	}
	
	static class FunctionCall implements Comparable<FunctionCall> {
		final long	callId;
		final int	arity;
		final NumericFunction1	number1;
		final NumericFunction2	number2;
		
		public FunctionCall(final long callId, final NumericFunction number) {
			super();
			this.callId = callId;
			if (number instanceof NumericFunction1) {
				this.arity = 1;
				this.number1 = (NumericFunction1)number;
				this.number2 = null;
			}
			else {
				this.arity = 2;
				this.number1 = null;
				this.number2 = (NumericFunction2)number;
			}
		}
		
		@Override
		public String toString() {
			return "FunctionCall [callId=" + callId + ", arity=" + arity + ", number1=" + number1 + ", number2=" + number2 + "]";
		}

		@Override
		public int compareTo(final FunctionCall o) {
			final int	delta = o.arity - this.arity;
			
			if (delta != 0) {
				return delta;
			}
			else {
				return (int) (o.callId - this.callId);
			}
		}
	}
}
