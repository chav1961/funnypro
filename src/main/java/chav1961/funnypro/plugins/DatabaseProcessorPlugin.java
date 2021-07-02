package chav1961.funnypro.plugins;


import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import chav1961.purelib.basic.SubstitutableProperties;
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
	public static final String		PROP_CONN_STRING = "fpro.connection.string";
	public static final String		PROP_CONN_USER = "fpro.connection.user";
	public static final String		PROP_CONN_PASSWD = "fpro.connection.passwd";
	public static final char[]		PREDICATE_ASSERT_DB = "assertDb(Table).".toCharArray(); 
	public static final char[]		PREDICATE_ASSERT_DB_2 = "assertDb(Table,Content).".toCharArray(); 
	public static final char[]		PREDICATE_RETRACT_DB = "retractDb(Table).".toCharArray(); 
	public static final char[]		PREDICATE_RETRACT_DB_2 = "retractDb(Table,Cond).".toCharArray(); 
	public static final char[]		PREDICATE_CALL_DB = "callDb(Table).".toCharArray(); 
	public static final char[]		PREDICATE_CALL_DB_2 = "callDb(Table,Cond).".toCharArray(); 
	
	private long	assertId, retractId, callId;
	
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
		try(final LoggerFacade 				actualLog = debug.transaction("StringProcesorPlugin:onLoad")) {
			final DatabaseProcessorGlobal	global = new DatabaseProcessorGlobal(DriverManager.getConnection(
													parameters.getProperty(PROP_CONN_STRING),
													parameters.getProperty(PROP_CONN_USER),
													parameters.getProperty(PROP_CONN_PASSWD))); 
			final IFProParserAndPrinter 	pap = new ParserAndPrinter(debug,parameters,repo);
			
			global.repo = repo;
			global.pap = pap;
			try{
				pap.parseEntities(PREDICATE_ASSERT_DB,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							assertId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,DatabaseProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_ASSERT_DB)+" was registeded successfully");
				pap.parseEntities(PREDICATE_ASSERT_DB_2,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							repo.pluginsRepo().registerResolver(entity,vars,DatabaseProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_ASSERT_DB_2)+" was registeded successfully");
				
				pap.parseEntities(PREDICATE_RETRACT_DB,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							retractId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,DatabaseProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_RETRACT_DB)+" was registeded successfully");
				pap.parseEntities(PREDICATE_RETRACT_DB_2,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							repo.pluginsRepo().registerResolver(entity,vars,DatabaseProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_RETRACT_DB_2)+" was registeded successfully");

				pap.parseEntities(PREDICATE_CALL_DB,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							callId = entity.getEntityId();
							repo.pluginsRepo().registerResolver(entity,vars,DatabaseProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_CALL_DB)+" was registeded successfully");
				pap.parseEntities(PREDICATE_CALL_DB_2,0,new FProParserCallback(){
						@Override
						public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
							repo.pluginsRepo().registerResolver(entity,vars,DatabaseProcessorPlugin.this,global);
							return true;
						}
					}
				);
				actualLog.message(Severity.info,"Predicate "+new String(PREDICATE_CALL_DB_2)+" was registeded successfully");
			} catch (SyntaxException | IOException exc) {
				actualLog.message(Severity.info,"Predicate registration failed: %1$s", exc.getMessage());
				throw new IllegalArgumentException("Attempt to register predicate/operator failed: "+exc.getMessage(),exc); 
			}
			actualLog.rollback();
			return global;
		} catch (SQLException exc) {
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
		if (entity.getEntityType() == EntityType.predicate) {
			if (entity.getEntityId() == callId) {
				final String selectString, whereString;
				
				switch (((IFProPredicate)entity).getArity()) {
					case 1 	:
						selectString = buildSelectString(global,local,((IFProPredicate)entity).getParameters()[0]);
						whereString = null;
						break;
					case 2 	:
						selectString = buildSelectString(global,local,((IFProPredicate)entity).getParameters()[0]);
						whereString = buildWhereString(global,local,((IFProPredicate)entity).getParameters()[1]);
						break;
					default :
						return ResolveRC.False;
				}
				try{local.stmt = global.conn.createStatement();
					local.rs = local.stmt.executeQuery(buildSelectStmt(selectString,whereString));
				
					if (local.rs.next()) {
						
					}
					local.rs.close();
					local.stmt.close();
					return ResolveRC.False;
				} catch (SQLException e) {
					throw new SyntaxException(0, 0, e.getLocalizedMessage(), e);
				}
			}
			else if (entity.getEntityId() == assertId) {
				switch (((IFProPredicate)entity).getArity()) {
					case 1 	:
					case 2 	:
					default :
						return ResolveRC.False;
				}
			}
			else if (entity.getEntityId() == retractId) {
				switch (((IFProPredicate)entity).getArity()) {
					case 1 	:
					case 2 	:
					default :
						return ResolveRC.False;
				}
			}
			else {
				return ResolveRC.False;
			}
		}
		else {
			return ResolveRC.False;
		}
	}
	
	@Override
	public ResolveRC nextResolve(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		return ResolveRC.False;
	}
	
	@Override
	public void endResolve(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		if (entity.getEntityType() == EntityType.predicate) {
			if (entity.getEntityId() == callId && (((IFProPredicate)entity).getArity() == 1 || ((IFProPredicate)entity).getArity() == 2)) {
				try{local.rs.close();
					local.stmt.close();
				} catch (SQLException exc) {
				}
			}
			else if (entity.getEntityId() == assertId) {
				switch (((IFProPredicate)entity).getArity()) {
					case 1 	:
					case 2 	:
					default :
						return;
				}
			}
			else if (entity.getEntityId() == retractId) {
				switch (((IFProPredicate)entity).getArity()) {
					case 1 	:
					case 2 	:
					default :
						return;
				}
			}
		}
	}
	
	@Override
	public void afterCall(final DatabaseProcessorGlobal global, final DatabaseProcessorLocal local) throws SyntaxException {
		global.collection.free(local);
	} 

	private static String buildWhereString(DatabaseProcessorGlobal global, DatabaseProcessorLocal local, IFProEntity ifProEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String buildSelectString(DatabaseProcessorGlobal global, DatabaseProcessorLocal local, IFProEntity ifProEntity) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String buildSelectStmt(String selectString, String whereString) {
		// TODO Auto-generated method stub
		return null;
	}

}