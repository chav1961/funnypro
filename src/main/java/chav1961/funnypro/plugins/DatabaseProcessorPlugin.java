package chav1961.funnypro.plugins;


import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.FProUtil.ContentType;
import chav1961.funnypro.core.GlobalDescriptor;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.QuickIds;
import chav1961.funnypro.core.RegisteredOperators;
import chav1961.funnypro.core.RegisteredPredicates;
import chav1961.funnypro.core.ResolvableAndGlobal;
import chav1961.funnypro.core.StandardResolver;
import chav1961.funnypro.core.entities.ExternalPluginEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
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
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo.Classification;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.enumerations.ContinueMode;

public class DatabaseProcessorPlugin implements IResolvable<DatabaseProcessorGlobal,DatabaseProcessorLocal>, FProPluginList {
	public static final String		PLUGIN_NAME = "DatabaseProcessorPlugin";
	public static final String		PLUGIN_DESCRIPTION = "This plugin supports a set of database predicates and operators";
	public static final String		PLUGIN_PRODUCER = "(c) 2021, Alexander V. Chernomyrdin aka chav1961";
	public static final int[]		PLUGIN_VERSION = {1,0};
	public static final String		PROP_CONN_STRING = "fpro.connection.string";
	public static final String		PROP_CONN_USER = "fpro.connection.user";
	public static final String		PROP_CONN_PASSWD = "fpro.connection.passwd";
	
	public enum RegisteredEntities {
		Op600xfxRange(DatabaseProcessorGlobal.OperatorType.OpBetween), 
		Op700xfxSimilar(DatabaseProcessorGlobal.OperatorType.OpLike),
		Op700xfxIn(DatabaseProcessorGlobal.OperatorType.OpIn),
		PredAssert, PredAssert2,
		PredRetract, PredRetract2,
		PredCall, PredCall2,
		Others;
		
		private final DatabaseProcessorGlobal.OperatorType	opType;
		
		RegisteredEntities() {
			this.opType = DatabaseProcessorGlobal.OperatorType.OpUnknown;
		}

		RegisteredEntities(final DatabaseProcessorGlobal.OperatorType type) {
			this.opType = type;
		}
		
		public DatabaseProcessorGlobal.OperatorType getOperatorType() {
			return opType;
		}
	}
	
	static final RegisteredOperators[]		OPS = { 	new RegisteredOperators<RegisteredEntities>(600,OperatorType.xfx,"..",RegisteredEntities.Op600xfxRange),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"~",RegisteredEntities.Op700xfxSimilar),
														new RegisteredOperators<RegisteredEntities>(700,OperatorType.xfx,"in",RegisteredEntities.Op700xfxIn)
													};
	static final RegisteredPredicates[]		PREDS = {	new RegisteredPredicates<RegisteredEntities>("assertDb(Table)",RegisteredEntities.PredAssert),
														new RegisteredPredicates<RegisteredEntities>("assertDb(Table,Content)",RegisteredEntities.PredAssert2),
														new RegisteredPredicates<RegisteredEntities>("retractDb(Table)",RegisteredEntities.PredRetract),
														new RegisteredPredicates<RegisteredEntities>("retractDb(Table,Cond)",RegisteredEntities.PredRetract),
														new RegisteredPredicates<RegisteredEntities>("callDb(Table)",RegisteredEntities.PredCall),
														new RegisteredPredicates<RegisteredEntities>("callDb(Table,Cond)",RegisteredEntities.PredCall2)
													};
	
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
	public DatabaseProcessorGlobal onLoad(final LoggerFacade debug, final SubstitutableProperties parameters, final IFProEntitiesRepo repo) throws SyntaxException {
		try(final LoggerFacade 				actualLog = debug.transaction("DatabaseProcesorPlugin:onLoad")) {
			final DatabaseProcessorGlobal	global = new DatabaseProcessorGlobal(DriverManager.getConnection(
													parameters.getProperty(PROP_CONN_STRING),
													parameters.getProperty(PROP_CONN_USER),
													parameters.getProperty(PROP_CONN_PASSWD))); 
			final IFProParserAndPrinter 	pap = new ParserAndPrinter(debug,parameters,repo);
			final Set<Long>									ids = new HashSet<>();
			final Map<Long,QuickIds<RegisteredEntities>>	registered = new HashMap<>();
			
			for (RegisteredOperators<RegisteredEntities> item : OPS) {
				actualLog.message(Severity.info,"Register operator %1$s, %2$s...", item.text, item.type);
				final long			itemId = repo.termRepo().placeName(item.text,null);
				IFProOperator[]		op;
			
				if ((op = repo.getOperatorDef(itemId,item.priority,item.priority,item.type.getSort())).length == 0) {
					final IFProOperator			def = new OperatorDefEntity(item.priority,item.type,itemId); 
					
					repo.putOperatorDef(def);	
					ids.add(itemId);
					FProUtil.fillQuickIds(registered,new QuickIds<RegisteredEntities>(def,item.action));
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

			for (RegisteredPredicates<RegisteredEntities> item : PREDS) {
				actualLog.message(Severity.info,"Register predicate %1$s...", item.text);
				try{pap.parseEntities(item.text.toCharArray(),0,new FProParserCallback(){
												@Override
												public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
													ids.add(entity.getEntityId());
													FProUtil.fillQuickIds(registered,new QuickIds<RegisteredEntities>(entity,item.action));
													repo.pluginsRepo().registerResolver(entity, vars, DatabaseProcessorPlugin.this, global);
													return true;
												}
											}
					);
				} catch (SyntaxException | IOException exc) {
//						exc.printStackTrace();
					actualLog.message(Severity.info,"Predicate registration failed for %1$s: %2$s", item.text, exc.getMessage());
					throw new IllegalArgumentException("Attempt to register predicate ["+item+"] failed: "+exc.getMessage(),exc); 
				}
				actualLog.message(Severity.info,"Predicate %1$s was registeded successfully", item.text);
			}
				
			for (Entry<Long, QuickIds<RegisteredEntities>> item : registered.entrySet()) {
				global.registered.put(item.getKey(),item.getValue());
				global.registeredIds.add(item.getKey());
			}
			global.repo = repo;
			global.pap = pap;
			
			actualLog.rollback();
			return global;
		} catch (SQLException exc) {
			exc.printStackTrace();
			throw new IllegalArgumentException("Attempt to register plugin failed: "+exc.getMessage(),exc); 
		}
	}
	
	@Override
	public void onRemove(final DatabaseProcessorGlobal global) throws SyntaxException {
		try{
			global.conn.close();
		} catch (SQLException e) {
		}
		global.repo.pluginsRepo().purgeResolver(this);
	}
	
	@Override
	public DatabaseProcessorLocal beforeCall(final DatabaseProcessorGlobal global, final IFProGlobalStack gs, final List<IFProVariable> vars, final IFProCallback callback) throws SyntaxException {
		final DatabaseProcessorLocal	result = global.collection.allocate();	// Prepare local memory for the given call
		
		result.callback = callback;
		result.stack = gs;
		result.vars = vars;
		return result;
	}
	
	@Override
	public ResolveRC firstResolve(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		final StringBuilder	sql = new StringBuilder();
		
		switch (FProUtil.detect(global.registered,entity,RegisteredEntities.Others)) {
			case PredAssert		:
				break;
			case PredAssert2	:
				break;
			case PredCall		:
				final IFProEntity	callParam = ((IFProPredicate)entity).getParameters()[0];
				
				if (callParam.getEntityType() == EntityType.predicate && buildSelectString(global, local, (IFProPredicate)callParam, sql)) {
					try{
						local.stmt = global.conn.createStatement();
						local.rs = local.stmt.executeQuery(sql.toString());
						
						while (local.rs.next()) {
							local.rsmd = local.rs.getMetaData();
							final IFProPredicate	record = buildRecordPredicate(local.rs, local.rsmd, global.repo.stringRepo(), (IFProPredicate)((IFProPredicate)entity).getParameters()[0]);
							
							if (FProUtil.unify(((IFProPredicate)entity).getParameters()[0], record, local.list)) {
								local.stack.push(GlobalStack.getTemporaryStackTop(entity, record));
								local.stack.push(GlobalStack.getBoundStackTop(((IFProPredicate)entity).getParameters()[0], entity, local.list[0]));
								return ResolveRC.True;
							}
							else {
								FProUtil.unbind(local.list[0]);
								FProUtil.removeEntity(global.repo.stringRepo(), record);
							}
						}
						return ResolveRC.False;
					} catch (SQLException e) {
						return ResolveRC.False;
					}
				}
				else {
					return ResolveRC.False;
				}
			case PredCall2		:
				final IFProEntity	callParam21 = ((IFProPredicate)entity).getParameters()[0];
				final IFProEntity	callParam22 = ((IFProPredicate)entity).getParameters()[1];
				
				try{
					if (callParam21.getEntityType() == EntityType.predicate && buildSelectString(global, local, (IFProPredicate)callParam21, sql) && buildWhereString(global, local, callParam22, sql.append(" where "))) {
						local.stmt = global.conn.createStatement();
						local.rs = local.stmt.executeQuery(sql.toString());
						
						while (local.rs.next()) {
							local.rsmd = local.rs.getMetaData();
							final IFProPredicate	record = buildRecordPredicate(local.rs, local.rsmd, global.repo.stringRepo(), (IFProPredicate)((IFProPredicate)entity).getParameters()[0]);
							
							if (FProUtil.unify(((IFProPredicate)entity).getParameters()[0], record, local.list)) {
								local.stack.push(GlobalStack.getTemporaryStackTop(entity, record));
								local.stack.push(GlobalStack.getBoundStackTop(((IFProPredicate)entity).getParameters()[0], entity, local.list[0]));
								return ResolveRC.True;
							}
							else {
								FProUtil.unbind(local.list[0]);
								FProUtil.removeEntity(global.repo.stringRepo(), record);
							}
						}
						return ResolveRC.False;
					}
					else {
						return ResolveRC.False;
					}
				} catch (SQLException | ContentException | IOException e) {
					return ResolveRC.False;
				}
			case PredRetract	:
				final IFProEntity	retractParam1 = ((IFProPredicate)entity).getParameters()[0];
				
				if (retractParam1.getEntityType() == EntityType.predicate && buildDeleteString(global, local, (IFProPredicate)retractParam1, sql)) {
					try{
						try(final Statement	stmt = global.conn.createStatement()) {
							stmt.executeUpdate(sql.toString());
							return stmt.getLargeUpdateCount() > 0 ? ResolveRC.True : ResolveRC.False;
						}
					} catch (SQLException e) {
						return ResolveRC.False;
					}
				}
				else {
					return ResolveRC.False;
				}
			case PredRetract2	:
				final IFProEntity	retractParam21 = ((IFProPredicate)entity).getParameters()[0];
				final IFProEntity	retractParam22 = ((IFProPredicate)entity).getParameters()[1];

				try {
					if (retractParam21.getEntityType() == EntityType.predicate && buildDeleteString(global, local, (IFProPredicate)retractParam21, sql) && buildWhereString(global, local, retractParam22, sql.append(" where "))) {
						try(final Statement	stmt = global.conn.createStatement()) {
							stmt.executeUpdate(sql.toString());
							return stmt.getLargeUpdateCount() > 0 ? ResolveRC.True : ResolveRC.False;
						}
					}
					else {
						return ResolveRC.False;
					}
				} catch (SQLException | ContentException | IOException e) {
					return ResolveRC.False;
				}
			case Others			:
				return ResolveRC.False;
			default:
				throw new UnsupportedOperationException("Entity type ["+FProUtil.detect(global.registered,entity,RegisteredEntities.Others)+"] is not supported yet");
		}
		return ResolveRC.False;
	}
	
	@Override
	public ResolveRC nextResolve(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		IFProEntity	record = null;
		
		switch (FProUtil.detect(global.registered,entity,RegisteredEntities.Others)) {
			case PredAssert		:
				break;
			case PredAssert2	:
				break;
			case PredCall : case PredCall2	:
				if (local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)local.stack.peek()).getMark() == entity) {
					FProUtil.unbind((Change)((BoundStackTop)local.stack.pop()).getChangeChain());
				}
				if (local.stack.peek().getTopType() == StackTopType.temporary && ((TemporaryStackTop)local.stack.peek()).getEntityAssicated() == entity) {
					FProUtil.removeEntity(global.repo.stringRepo(), ((TemporaryStackTop)local.stack.pop()).getEntity());
				}
				
				try{while (local.rs.next()) {
						record = buildRecordPredicate(local.rs, local.rsmd, global.repo.stringRepo(), (IFProPredicate)((IFProPredicate)entity).getParameters()[0]);
						
						if (FProUtil.unify(((IFProPredicate)entity).getParameters()[0], record, local.list)) {
							local.stack.push(GlobalStack.getTemporaryStackTop(entity, record));
							local.stack.push(GlobalStack.getBoundStackTop(((IFProPredicate)entity).getParameters()[0], entity, local.list[0]));
							return ResolveRC.True;
						}
						else {
							FProUtil.unbind(local.list[0]);
							FProUtil.removeEntity(global.repo.stringRepo(), record);
						}
					}
					return ResolveRC.False;
				} catch (NullPointerException | SQLException e) {
					return ResolveRC.False;
				}
			case PredRetract : case PredRetract2 :
				return ResolveRC.False;
			case Others			:
				return ResolveRC.False;
			default:
				throw new UnsupportedOperationException("Entity type ["+FProUtil.detect(global.registered,entity,RegisteredEntities.Others)+"] is not supported yet");
		}
		return ResolveRC.False;
	}
	
	@Override
	public void endResolve(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		switch (FProUtil.detect(global.registered,entity,RegisteredEntities.Others)) {
			case PredAssert		:
				break;
			case PredAssert2	:
				break;
			case PredCall : case PredCall2 :
				if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)local.stack.peek()).getMark() == entity) {
					FProUtil.unbind((Change)((BoundStackTop)local.stack.pop()).getChangeChain());
				}
				if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.temporary && ((TemporaryStackTop)local.stack.peek()).getEntityAssicated() == entity) {
					FProUtil.removeEntity(global.repo.stringRepo(), ((TemporaryStackTop)local.stack.pop()).getEntity());
				}
				break;
			case PredRetract : case PredRetract2 :
				break;
			case Others			:
				break;
			default:
				throw new UnsupportedOperationException("Entity type ["+FProUtil.detect(global.registered,entity,RegisteredEntities.Others)+"] is not supported yet");
		}
	}
	
	@Override
	public void afterCall(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local) throws SyntaxException {
		if (local.rs != null) {
			try{local.rs.close();} catch (SQLException ne) {}
			local.rs = null;
		}
		if (local.stmt != null) {
			try{local.stmt.close();} catch (SQLException ne) {}
			local.stmt = null;
		}
		global.collection.free(local);
	} 

	// parse predicate TableName(FieldName(Something),...)
	private static boolean buildSelectString(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProPredicate entity, final StringBuilder sql) {
		final SyntaxTreeInterface<?>	sti = global.repo.termRepo();
		String 	prefix = "select ";
		
		for (IFProEntity item : entity.getParameters()) {
			if (item.getEntityType() == EntityType.predicate && ((IFProPredicate)item).getArity() == 1) {
				sql.append(prefix).append(sti.getName(item.getEntityId()));
				prefix = ",";
			}
			else {
				return false;
			}
		}
		sql.append(" from ").append(sti.getName(entity.getEntityId()));
		
		return true;
	}

	private static IFProPredicate buildRecordPredicate(final ResultSet rs, final ResultSetMetaData rsmd, final SyntaxTreeInterface<?> tree, final IFProPredicate entity) throws SQLException {
		final IFProEntity[]		parameters = new IFProEntity[entity.getArity()];
		
		for (int index = 0, maxIndex = parameters.length; index < maxIndex; index++) {
			switch (rsmd.getColumnType(index+1)) {
				case Types.SMALLINT : case Types.INTEGER : case Types.BIGINT :
					parameters[index] = new PredicateEntity(entity.getParameters()[index].getEntityId(), new IntegerEntity(rs.getLong(index+1)));
					break;
				case Types.REAL : case Types.DOUBLE :
					parameters[index] = new PredicateEntity(entity.getParameters()[index].getEntityId(), new RealEntity(rs.getDouble(index+1)));
					break;
				default :
					parameters[index] = new PredicateEntity(entity.getParameters()[index].getEntityId(), new StringEntity(tree.placeName(rs.getString(index+1), null)));
					break;
			}
		}
		return new PredicateEntity(entity.getEntityId(), parameters);
	}

	// parse "expression" (field = Val, field in [List...], field in Val .. Val, field ~ Val, field is null, field is not null; ...)
	private boolean buildWhereString(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity node, final StringBuilder sql) throws ContentException, IOException {
		// TODO Auto-generated method stub
		if (FProUtil.hasAnyVariableOrAnonymous(node)) {
			return false;
		}
		else {
			switch (node.getEntityType()) {
				case integer		:
					sql.append(node.getEntityId());
					return true;
				case list			:
					final char[]	prefix = {' '};
					final boolean[]	result = {true};
					
					sql.append('(');
					FProUtil.forList((IFProList)node, (e)->{
						sql.append(prefix[0]);
						result[0] &= buildWhereString(global, local, e, sql);
						prefix[0] = ',';
						return ContinueMode.CONTINUE;
					});
					sql.append(')');
					return result[0];
				case real			:
					sql.append(Double.longBitsToDouble(node.getEntityId()));
					return true;
				case string			:
					sql.append('\'').append(global.repo.stringRepo().getName(node.getEntityId())).append('\'');
					return true;
				case operator		:
					switch (classify(global, (IFProOperator)node)) {
						case OpAnd		:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" and "));  
						case OpBetween	:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql.append(" between ")) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" and "));  
						case OpIn		:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" in "));  
						case OpIs		:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" is "));  
						case OpLike		:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" like "));  
						case OpNot		:
							return buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" not "));  
						case OpOr		:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" or "));  
						case OpEq:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" = "));  
						case OpGe:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" >= "));  
						case OpGt:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" > "));  
						case OpLe:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" <= "));  
						case OpLt:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" < "));  
						case OpNe:
							return buildWhereString(global, local, ((IFProOperator)node).getLeft(), sql) && buildWhereString(global, local, ((IFProOperator)node).getRight(), sql.append(" <> "));  
						case OpUnknown	:
							return false;
						default : throw new UnsupportedOperationException("Operator type ["+classify(global, (IFProOperator)node)+"] is not supported yet");
					}
				case predicate :
					if (((IFProPredicate)node).getArity() != 0) {
						return false;
					}
					else {
						sql.append(global.repo.termRepo().getName(node.getEntityId()));
						return true;
					}
				case operatordef : case variable : case externalplugin	: case any : case anonymous : 
					return false;
				default	:
					return false;
			}
		}
	}

	private static boolean buildDeleteString(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProPredicate node, final StringBuilder sql) {
		if (node.getArity() == 0) {
			sql.append("delete from ").append(global.repo.termRepo().getName(node.getEntityId()));
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private DatabaseProcessorGlobal.OperatorType classify(final DatabaseProcessorGlobal global, final IFProOperator node) {
		final RegisteredEntities	re = FProUtil.detect(global.registered, node, RegisteredEntities.Others); 
		
		if (re == RegisteredEntities.Others) {
			final ResolvableAndGlobal<GlobalDescriptor>	rag = FProUtil.getStandardResolver(global.repo);
			
			switch (FProUtil.detect(rag.global.registered, node, StandardResolver.RegisteredEntities.Others)) {
				case Op1100xfyOr 			: return DatabaseProcessorGlobal.OperatorType.OpOr;
				case Op1000xfyAnd			: return DatabaseProcessorGlobal.OperatorType.OpAnd;
				case Op900fyNot				: return DatabaseProcessorGlobal.OperatorType.OpNot;
				case Op700xfxEqual			: return DatabaseProcessorGlobal.OperatorType.OpEq;
				case Op700xfxNotEqual		: return DatabaseProcessorGlobal.OperatorType.OpNe;
				case Op700xfxIs				: return DatabaseProcessorGlobal.OperatorType.OpIs;
				case Op700xfxLess			: return DatabaseProcessorGlobal.OperatorType.OpLt;
				case Op700xfxLessEqual		: return DatabaseProcessorGlobal.OperatorType.OpLe;
				case Op700xfxGreater		: return DatabaseProcessorGlobal.OperatorType.OpGt;
				case Op700xfxGreaterEqual	: return DatabaseProcessorGlobal.OperatorType.OpGe;
				default : return DatabaseProcessorGlobal.OperatorType.OpUnknown;  
			}
		}
		else {
			return re.getOperatorType();
		}
	}
}