package chav1961.funnypro.core;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginItem;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.funnypro.core.interfaces.IResolvable.ResolveRC;
import chav1961.funnypro.core.interfaces.IGentlemanSet;
import chav1961.funnypro.core.CommonUtil;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class FProVM implements IFProVM, IGentlemanSet {
	private static final int			SERIALIZATION_MAGIC = 0x12120000;
	private static final int			SERIALIZATION_VERSION = 0x00010001;

	private final LoggerFacade			log;
	private final Properties			props;
	private boolean						turnedOn = false;
	private IFProEntitiesRepo			repo = null;
	private long						question, goal;
	
	public FProVM(final LoggerFacade log, final Properties prop) {
		if (log == null) {
			throw new IllegalArgumentException("Log can't be null"); 
		}
		else if (prop == null) {
			throw new IllegalArgumentException("Properties can't be null"); 
		}
		else {
			this.log = log;				this.props = prop;
		}
	}

	@Override public LoggerFacade getDebug() {return log;}
	@Override public Properties getParameters() {return props;}	
	
	@Override
	public void close() throws IOException {
		if (isTurnedOn()) {
			try{turnOff(null);
			} catch (FProException e) {
				throw new IOException(e.getMessage(),e);
			}
		}
	}
	
	@Override
	public void turnOn(final InputStream source) throws FProException, IOException {
		if (isTurnedOn()) {
			throw new IllegalStateException("VM already is turned on");
		}
		else {
			repo = new EntitiesRepo(getDebug(),getParameters());
			
			if (source != null) {
				int		magic, version;
				
				if ((magic = CommonUtil.readInt(source)) != SERIALIZATION_MAGIC) {
					throw new FProException("Invalid source stream content: magic readed ["+magic+"] is differenf to awaited ["+SERIALIZATION_MAGIC+"]");
				}
				else if ((version = CommonUtil.readInt(source)) != SERIALIZATION_VERSION) {
					throw new FProException("Unsupported source stream content: version readed ["+version+"] is differenf to awaited ["+SERIALIZATION_VERSION+"]");
				}
				else {
					repo.deserialize(source);
					if ((magic = CommonUtil.readInt(source)) != SERIALIZATION_MAGIC) {
						throw new FProException("Invalid source stream content: input stream contains something after the data end!");
					}
				}
			}

			question = repo.termRepo().placeName("?-",null);
			goal = repo.termRepo().placeName(":-",null);
			turnedOn = true;
		}
	}

	@Override
	public void turnOff(OutputStream target) throws FProException, IOException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("VM already is turned off");
		}
		else {
			turnedOn = false;
			repo.termRepo().removeName(question);
			repo.termRepo().removeName(goal);
			
			if (target != null) {
				CommonUtil.writeInt(target, SERIALIZATION_MAGIC);		// Write magic
				CommonUtil.writeInt(target, SERIALIZATION_VERSION);		// Write magic
				repo.serialize(target);									// Write content
				CommonUtil.writeInt(target, SERIALIZATION_MAGIC);		// Write tail
				target.flush();
			}
			
			try{repo.close();
			} catch (Exception e) {
				throw new FProException(e.getMessage(),e);
			}
		}
	}

	@Override
	public boolean isTurnedOn() {
		return turnedOn;
	}

	@Override
	public void newFRB(final OutputStream target) throws FProException, IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
		}
		else if (isTurnedOn()) {
			throw new IllegalStateException("You can't make this operatio when VM is turned on. Turn off VM firstly!");
		}
		else {
			try(final IFProEntitiesRepo	temp = new EntitiesRepo(getDebug(),getParameters())) {
				
				CommonUtil.writeInt(target, SERIALIZATION_MAGIC);		// Write magic
				CommonUtil.writeInt(target, SERIALIZATION_VERSION);		// Write magic
				temp.serialize(target);									// Write content
				CommonUtil.writeInt(target, SERIALIZATION_MAGIC);		// Write tail
				target.flush();
			} catch (Exception e) {
				throw new FProException(e.getMessage(),e);
			}
		}
	}
	
	@Override
	public boolean question(final String question, final IFProCallback callback) throws FProException, IOException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("You can't make this operatio when VM is turned on. Turn off VM firstly!");
		}
		else {
			return question(question,repo,callback);
		}
	}	
	
	@Override
	public boolean question(final String question, final IFProEntitiesRepo repo, final IFProCallback callback) throws FProException, IOException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("You can't make this operatio when VM is turned on. Turn off VM firstly!");
		}
		else {
			return internalGoal(question,repo,callback,this.question,"question");
		}
	}

	@Override
	public boolean goal(final String goal, final IFProCallback callback) throws FProException, IOException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("You can't make this operatio when VM is turned on. Turn off VM firstly!");
		}
		else {
			return goal(goal,repo,callback);
		}
	}	
	
	@Override
	public boolean goal(final String goal, final IFProEntitiesRepo repo, final IFProCallback callback) throws FProException, IOException {
		return internalGoal(goal,repo,callback,this.goal,"goal");
	}

	@Override
	public void consult(final Reader source) throws FProParsingException, IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null");
		}
		else if (!isTurnedOn()) {
			throw new IllegalStateException("VM is not turned on. Turn on it firstly!");
		}
		else {
			repo.consult(source);
		}
	}

	@Override
	public void save(final PrintWriter target) throws FProPrintingException, IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
		}
		else if (!isTurnedOn()) {
			throw new IllegalStateException("VM is not turned on. Turn on it firstly!");
		}
		else {
			repo.save(target);
		}
	}

	@Override
	public void console(Reader source, PrintWriter target, PrintWriter errors) throws FProException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("VM is not turned on. Turn on it firstly!");
		}
		else {
			IFProEntity			entity;
			
			try{final BufferedReader		brdr = new BufferedReader(source);	// Don't close!!!
				final IFProParserAndPrinter pap = new ParserAndPrinter(getDebug(), getParameters(), repo);
				final boolean[]				continuation = new boolean[]{true};
				String						command;
				
				final long	quit = repo.termRepo().placeName("quit",null);
				
				while (continuation[0] && (command = brdr.readLine()) != null) {
					try{pap.parseEntities(command.toCharArray(),0,new FProParserCallback() {
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProParsingException, IOException {
												try{
												if (entity.getEntityId() == quit && entity.getEntityType().equals(EntityType.predicate) && ((IFProPredicate)entity).getArity() == 0) {
													target.println("quit successful!");
													continuation[0] = false;
													return false;
												}
												else if (entity.getEntityId() == goal && entity.getEntityType().equals(EntityType.operator) && ((IFProOperator)entity).getType().equals(OperatorType.fx)) {
													target.println(inference(entity,vars,repo,new IFProCallback(){
														@Override public void beforeFirstCall() {}
														@Override public boolean onResolution(Map<String, Object> resolvedVariables) {return true;}
														@Override public void afterLastCall() {}
													}));
												}
												else if (entity.getEntityId() == question && entity.getEntityType().equals(EntityType.operator) && ((IFProOperator)entity).getType().equals(OperatorType.fx)) {
													target.println(inference(entity,vars,repo,new IFProCallback(){
														@Override public void beforeFirstCall() {}
														
														@Override 
														public boolean onResolution(final Map<String, Object> resolvedVariables) throws FProPrintingException, FProParsingException {
															for (Entry<String, Object> item : resolvedVariables.entrySet()) {
																target.println(String.format("%1$s = %2%s\n",item.getKey(),item.getValue()));
															}
															target.println("proceed? ");

															try{final String	buffer = brdr.readLine();
																return "yY+tT".indexOf(buffer == null ? "" : buffer) >= 0;
															} catch (IOException e) {
																return false;
															}
														}
														
														@Override public void afterLastCall() {}
													}));
												}
												else {
													repo.predicateRepo().assertZ(entity);
												}
												} catch (FProException exc) {
													exc.printStackTrace();
													errors.println(String.format("Error executing input: "+exc.getMessage()));
													return false;
												}
												return true;
											}
										}
					);
					
					} catch (FProParsingException e) {
						errors.println(String.format("Error parsing input: "+e.getMessage()));
					}
				}				
			} catch (Exception e) {
				throw new FProException(e.getMessage()); 
			} finally {
				final long	quitName = repo.termRepo().seekName("quit"); 
				if (quitName >= 0) {
					repo.termRepo().removeName(quitName);
				}
			}
		}
	}

	private boolean internalGoal(final String source, final IFProEntitiesRepo repo, final IFProCallback callback, final long opId, final String awaited) throws FProException, IOException {
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Question string can't be null");
		}
		else if (repo == null) { 
			throw new IllegalArgumentException("Repo can't be null");
		}
		else if (callback == null) { 
			throw new IllegalArgumentException("Callback can't be null");
		}
		else {
			final IFProParserAndPrinter	pap = new ParserAndPrinter(getDebug(), getParameters(), repo);
			final List<IFProVariable>	vars = new ArrayList<>();
			final boolean[]				result = new boolean[1];
			
			pap.parseEntities(source.toCharArray(),0,new FProParserCallback(){
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProParsingException, IOException {
												if (entity.getEntityId() == opId && entity.getEntityType().equals(EntityType.operator) && ((IFProOperator)entity).getType().equals(OperatorType.fx)) {
													try{result[0] = inference(entity,vars,repo,callback);
													} catch (FProException e) {
														e.printStackTrace();
														throw new FProParsingException(0,0,e.getMessage());
													}
												}
												else {
													throw new FProParsingException(0,0,"Illegal source content ("+awaited+" awaited)"); 
												}
												return false;
											}
										}
			);
			return result[0];
		}
	}
	
	
	protected ResolvableAndGlobal getStandardResolver() {
		for (PluginItem item : repo.pluginsRepo().seek(StandardResolver.PLUGIN_NAME,StandardResolver.PLUGIN_PRODUCER,StandardResolver.PLUGIN_VERSION)) {
			return new ResolvableAndGlobal(item.getDescriptor().getPluginEntity().getResolver(),item.getGlobal());
		}
		throw new IllegalStateException("No standard resolver was registered in the system. Use inference with explicit call");
	}

	private boolean inference(final IFProEntity entity, final List<IFProVariable> vars, final IFProEntitiesRepo repo, final IFProCallback callback) throws FProException {
		return inference(entity,vars,repo,getStandardResolver(),callback);
	}	
	
	private boolean inference(final IFProEntity entity, final List<IFProVariable> vars, final IFProEntitiesRepo repo, final ResolvableAndGlobal rag, final IFProCallback callback) throws FProException {
		final GlobalStack	stack = new GlobalStack(getDebug(),getParameters(),repo); 
		final Object 		data = rag.resolver.beforeCall(rag.global,stack,vars,callback);
		
		try{if (rag.resolver.firstResolve(rag.global,data,entity) == ResolveRC.True) {
				while (rag.resolver.nextResolve(rag.global,data,entity) == ResolveRC.True) {}
				return true;
			}
			else {
				return false;
			}
		} finally {
			rag.resolver.endResolve(rag.global,data,entity);
			rag.resolver.afterCall(rag.global,data);
		}
	}
	
	public static class ResolvableAndGlobal {
		public IResolvable	resolver;
		public Object		global;
		
		public ResolvableAndGlobal(final IResolvable resolver, final Object global) {
			this.resolver = resolver;
			this.global = global;
		}
		
	}
}
