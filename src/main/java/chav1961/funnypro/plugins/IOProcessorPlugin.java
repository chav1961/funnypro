package chav1961.funnypro.plugins;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import chav1961.funnypro.core.FProUtil;
import chav1961.funnypro.core.GlobalStack;
import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.entities.ExternalPluginEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
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
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.DottedVersion;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.streams.charsource.ReaderCharSource;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;

public class IOProcessorPlugin implements IResolvable<IOProcessorGlobal,IOProcessorLocal>, FProPluginList {
	public static final String			PLUGIN_NAME = "IOProcessorPlugin";
	public static final String			PLUGIN_DESCRIPTION = "This plugin supports a set of I/O predicates and operators based on I.Bratko Prolog definition";
	public static final String			PLUGIN_PRODUCER = "(c) 2023, Alexander V. Chernomyrdin aka chav1961";
	public static final DottedVersion	PLUGIN_VERSION = DottedVersion.ZERO;

	public static final char[]			PREDICATE_SEE = "see(Value).".toCharArray(); 
	public static final char[]			PREDICATE_GET = "get(Value).".toCharArray(); 
	public static final char[]			PREDICATE_GET0 = "get0(Value).".toCharArray(); 
	public static final char[]			PREDICATE_READ = "read(Value).".toCharArray(); 
	public static final char[]			PREDICATE_SEEN = "seen.".toCharArray();
	
	public static final char[]			PREDICATE_TELL = "tell(Value).".toCharArray(); 
	public static final char[]			PREDICATE_PUT = "put(Value).".toCharArray(); 
	public static final char[]			PREDICATE_WRITE = "write(Value).".toCharArray(); 
	public static final char[]			PREDICATE_WRITELN = "writeln(Value).".toCharArray(); 
	public static final char[]			PREDICATE_NL = "nl.".toCharArray(); 
	public static final char[]			PREDICATE_TAB = "tab(N).".toCharArray(); 
	public static final char[]			PREDICATE_TOLD = "told.".toCharArray(); 

	public static final char[]			ATOM_EOF = "end_of_file.".toCharArray(); 
	public static final char[]			ATOM_USER = "user.".toCharArray(); 
	
	private final PluginDescriptor	DESC = new PluginDescriptor(){
									@SuppressWarnings("unchecked")
									@Override public IFProExternalEntity<IOProcessorGlobal,IOProcessorLocal> getPluginEntity() {return new ExternalPluginEntity<IOProcessorGlobal,IOProcessorLocal>(1, PLUGIN_NAME, PLUGIN_PRODUCER, PLUGIN_VERSION, IOProcessorPlugin.this);}
									@Override public String getPluginPredicate() {return null;}
									@Override public String getPluginDescription() {return PLUGIN_DESCRIPTION;}
								};
	private final char[]		tempCharArray = new char[1];
	private long				eofId, userId;
	private long				seeId, getId, get0Id, readId, seenId;
	private long				tellId, putId, writeId, writeLnId, nlId, tabId, toldId;
	
	@Override
	public PluginDescriptor[] getPluginDescriptors() {
		return new PluginDescriptor[]{DESC};
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
	public IOProcessorGlobal onLoad(final LoggerFacade debug, final SubstitutableProperties parameters, final IFProEntitiesRepo repo) throws SyntaxException {
		try(final LoggerFacade 				actualLog = debug.transaction("IOProcesorPlugin:onLoad")) {
			final IOProcessorGlobal			global = new IOProcessorGlobal(); 
			final IFProParserAndPrinter 	pap = new ParserAndPrinter(debug,parameters,repo);
			
			global.repo = repo;
			global.pap = pap;
			eofId = repo.termRepo().placeName(ATOM_EOF, 0, ATOM_EOF.length, null);
			userId = repo.termRepo().placeName(ATOM_USER, 0, ATOM_USER.length, null);
			try{
				seeId = registerPredicate(PREDICATE_SEE, actualLog, pap, repo, global);
				getId = registerPredicate(PREDICATE_GET, actualLog, pap, repo, global);
				get0Id = registerPredicate(PREDICATE_GET0, actualLog, pap, repo, global);
				readId = registerPredicate(PREDICATE_READ, actualLog, pap, repo, global);
				seenId = registerPredicate(PREDICATE_SEEN, actualLog, pap, repo, global);

				tellId = registerPredicate(PREDICATE_TELL, actualLog, pap, repo, global);
				putId = registerPredicate(PREDICATE_PUT, actualLog, pap, repo, global);
				writeId = registerPredicate(PREDICATE_WRITE, actualLog, pap, repo, global);
				writeLnId = registerPredicate(PREDICATE_WRITELN, actualLog, pap, repo, global);
				nlId = registerPredicate(PREDICATE_NL, actualLog, pap, repo, global);
				tabId = registerPredicate(PREDICATE_TAB, actualLog, pap, repo, global);
				toldId = registerPredicate(PREDICATE_TOLD, actualLog, pap, repo, global);
			} catch (SyntaxException | IOException exc) {
				actualLog.message(Severity.info,exc,"Predicate registration failed: %1$s", exc.getLocalizedMessage());
				throw new IllegalArgumentException("Attempt to register predicate/operator failed: "+exc.getLocalizedMessage(), exc); 
			}
			actualLog.rollback();
			return global;
		}
	}

	@Override
	public void onRemove(final IOProcessorGlobal global) throws SyntaxException {
		global.repo.termRepo().removeName(eofId);
		global.repo.termRepo().removeName(userId);
		global.repo.pluginsRepo().purgeResolver(this);
	}
	
	@Override
	public IOProcessorLocal beforeCall(final IOProcessorGlobal global, final IFProGlobalStack gs, final List<IFProVariable> vars, final IFProCallback callback) throws SyntaxException {
		final IOProcessorLocal	result = global.collection.allocate();
		
		result.callback = callback;
		result.stack = gs;
		result.vars = vars;
		return result;
	}
	
	@Override
	public ResolveRC firstResolve(final IOProcessorGlobal global, final IOProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		if (entity.getEntityType() == EntityType.predicate) {
			try {
				switch (((IFProPredicate)entity).getArity()) {
					case 0 :
						final long	predId0 = entity.getEntityId();
						
						if (predId0 == seenId) {
							if (global.openedReader != null) {
								global.openedReader.close();
								global.openedReader = null;
							}
							global.currentReader = new ReaderCharSource(new InputStreamReader(System.in), true);
							return ResolveRC.True;
						}
						else if (predId0 == nlId) {
							global.currentWriter.append('\n');
							global.currentWriter.flush();
							return ResolveRC.True;
						}
						else if (predId0 == toldId) {
							global.currentWriter.flush();
							if (global.openedWriter != null) {
								global.openedWriter.flush();
								global.openedWriter.close();
								global.openedWriter = null;
							}
							global.currentWriter = new WriterCharTarget(System.out, true);
							return ResolveRC.True;
						}
						else {
							return ResolveRC.False;
						}
					case 1 :
						final long			predId1 = entity.getEntityId();
						final IFProEntity	parm = ((IFProPredicate)entity).getParameters()[0];
						
						if (predId1 == seeId) {
							if (parm.getEntityType() == EntityType.string) {
								final File	f = new File(global.repo.stringRepo().getName(parm.getEntityId()));
								
								if (f.exists() && f.isFile() && f.canRead()) {
									global.openedReader = new FileReader(f);
									global.currentReader = new ReaderCharSource(global.openedReader, true);
									return ResolveRC.True;
								}
								else {
									return ResolveRC.False;
								}
							}
							else if (parm.getEntityType() == EntityType.predicate && parm.getEntityId() == userId) {
								if (global.openedReader != null) {
									global.openedReader.close();
									global.openedReader = null;
								}
								global.currentReader = new ReaderCharSource(new InputStreamReader(System.in), true);
								return ResolveRC.True;
							}
							else {
								return ResolveRC.False;
							}
						}
						else if (predId1 == getId) {
							char	symbol = global.currentReader.next();
							
							while (Character.isSpaceChar(symbol)) {
								symbol = global.currentReader.next();
							}
							return resolveSymbol(symbol, entity, global, local);
						}
						else if (predId1 == get0Id) {
							return resolveSymbol(global.currentReader.next(), entity, global, local);
						}
						else if (predId1 == readId) {
							global.pap.parseEntities(global.currentReader, (pred,vars)->{global.forEntity[0] = pred; return false;});
							
							if (FProUtil.unify(global.forEntity[0], parm, local.list)) {
								if (local.list[0] != null) {
									local.stack.push(GlobalStack.getBoundStackTop(entity, entity, local.list[0]));
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
						else if (predId1 == tellId) {
							if (((IFProPredicate)entity).getParameters()[0].getEntityType() == EntityType.string) {
								final File	f = new File(global.repo.stringRepo().getName(((IFProPredicate)entity).getParameters()[0].getEntityId()));
								
								if (f.exists() && !f.isDirectory() && f.canWrite() || !f.exists()) {
									global.openedWriter = new PrintStream(new FileOutputStream(f));
									global.currentWriter = new WriterCharTarget(global.openedWriter, true);
									return ResolveRC.True;
								}
								else {
									return ResolveRC.False;
								}
							}
							else if (((IFProPredicate)entity).getParameters()[0].getEntityType() == EntityType.predicate && ((IFProPredicate)entity).getParameters()[0].getEntityId() == userId) {
								if (global.openedWriter != null) {
									global.openedWriter.flush();
									global.openedWriter.close();
									global.openedWriter = null;
								}
								global.currentWriter = new WriterCharTarget(System.out, true);
								return ResolveRC.True;
							}
							else {
								return ResolveRC.False;
							}
						}
						else if (predId1 == putId) {
							switch (parm.getEntityType()) {
								case integer	:
									global.currentWriter.append((char)parm.getEntityId());
									return ResolveRC.True;
								case real		:
									global.currentWriter.append((char)Double.longBitsToDouble(parm.getEntityId()));
									return ResolveRC.True;
								case string		:
									final String symbol = global.repo.stringRepo().getName(parm.getEntityId());
									
									if (symbol.length() > 0) {
										global.currentWriter.append(symbol.charAt(0));
										return ResolveRC.True;
									}
									else {
										return ResolveRC.False;
									}
								case anonymous : case any : case externalplugin : case list : case operator : case operatordef : case predicate : case variable:
									return ResolveRC.False;
								default :
									throw new UnsupportedOperationException("Entity type ["+((IFProPredicate)entity).getParameters()[0].getEntityType()+"] is not supported yet");
							}
						}
						else if (predId1 == writeId) {
							final IFProEntity[]		parms = ((IFProPredicate)entity).getParameters();
							
							global.pap.putEntity(parms[0], global.currentWriter);
							return ResolveRC.True;
						}
						else if (predId1 == writeLnId) {
							final IFProEntity[]		parms = ((IFProPredicate)entity).getParameters();
							
							global.pap.putEntity(parms[0], global.currentWriter);
							global.currentWriter.append('\n');
							global.currentWriter.flush();
							return ResolveRC.True;
						}
						else if (predId1 == tabId) {
							switch (parm.getEntityType()) {
								case integer	:
									for(int index = 0, maxIndex = (int)parm.getEntityId(); index < maxIndex; index++) {
										global.currentWriter.append('\t');
									}
									return ResolveRC.True;
								case real		:
									for(int index = 0, maxIndex = (int)Double.longBitsToDouble(parm.getEntityId()); index < maxIndex; index++) {
										global.currentWriter.append('\t');
									}
									return ResolveRC.True;
								case string : case anonymous : case any : case externalplugin : case list : case operator : case operatordef : case predicate : case variable:
									return ResolveRC.False;
								default :
									throw new UnsupportedOperationException("Entity type ["+((IFProPredicate)entity).getParameters()[0].getEntityType()+"] is not supported yet");
							}
						}
						else {
							return ResolveRC.False;
						}
					default :
						return ResolveRC.False;
				}
			} catch (ContentException | IOException e) {
				return ResolveRC.False;
			}
		}
		else {
			return ResolveRC.False;
		}
	}
	
	@Override
	public ResolveRC nextResolve(final IOProcessorGlobal global, final IOProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		if (entity.getEntityType() == EntityType.predicate) {
			return ResolveRC.False;
		}
		return ResolveRC.False;
	}
	
	@Override
	public void endResolve(final IOProcessorGlobal global, final IOProcessorLocal local, final IFProEntity entity) throws SyntaxException {
		if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.temporary && ((TemporaryStackTop)local.stack.peek()).getEntityAssicated() == entity) {
			FProUtil.releaseTemporaries(entity, global.repo.stringRepo(), local.stack);
		}
		if (!local.stack.isEmpty() && local.stack.peek().getTopType() == StackTopType.bounds && ((BoundStackTop)local.stack.peek()).getMark() == entity) {
			FProUtil.unbind(((BoundStackTop<FProUtil.Change>)local.stack.pop()).getChangeChain());
		}
	}
	
	@Override
	public void afterCall(final IOProcessorGlobal global, final IOProcessorLocal local) throws SyntaxException {
		global.collection.free(local);
	} 

	private long registerPredicate(final char[] predicate, final LoggerFacade logger, final IFProParserAndPrinter pap, final IFProEntitiesRepo repo, final IOProcessorGlobal global) throws SyntaxException, IOException {
		final long[] result = {0};
		
		pap.parseEntities(predicate, 0, (entity, vars) -> {
			result[0] = entity.getEntityId();
			repo.pluginsRepo().registerResolver(entity, vars, IOProcessorPlugin.this, global);
			return true;
		});
		logger.message(Severity.info,"Predicate "+new String(predicate)+" was registeded successfully");
		return result[0];
	}
	
	private ResolveRC resolveSymbol(final char symbol, final IFProEntity entity, final IOProcessorGlobal global, final IOProcessorLocal local) {
		final IFProEntity	readed; 
		
		tempCharArray[0] = symbol;
		if (symbol == CharacterSource.EOF) {
			readed = new PredicateEntity(eofId);
		}
		else {
			final long	symbolId = global.repo.stringRepo().placeName(tempCharArray, 0, 1, null);
			
			readed = new StringEntity(symbolId);
		}
		if (FProUtil.unify(readed, ((IFProPredicate)entity).getParameters()[0], local.list)) {
			if (local.list[0] != null) {
				local.stack.push(GlobalStack.getBoundStackTop(entity, entity, local.list[0]));
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