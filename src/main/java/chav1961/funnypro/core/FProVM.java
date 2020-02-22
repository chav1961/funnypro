package chav1961.funnypro.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

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
import chav1961.funnypro.core.interfaces.IFProModule;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.funnypro.core.interfaces.IResolvable.ResolveRC;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class is a Funny Prolog Virtual machine implementation</p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class FProVM implements IFProVM, IFProModule {
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
			} catch (ContentException e) {
				throw new IOException(e.getMessage(),e);
			}
		}
	}
	
	@Override
	public void turnOn(final InputStream source) throws ContentException, IOException {
		if (isTurnedOn()) {
			throw new IllegalStateException("VM already is turned on");
		}
		else {
			repo = new EntitiesRepo(getDebug(),getParameters());
			
			if (source != null) {
				final DataInputStream	dis = new DataInputStream(source);
				int						magic, version;
				
				if ((magic = dis.readInt()) != SERIALIZATION_MAGIC) {
					throw new ContentException("Invalid source stream content: magic readed ["+magic+"] is differenf to awaited ["+SERIALIZATION_MAGIC+"]");
				}
				else if ((version = dis.readInt()) != SERIALIZATION_VERSION) {
					throw new ContentException("Unsupported source stream content: version readed ["+version+"] is differenf to awaited ["+SERIALIZATION_VERSION+"]");
				}
				else {
					repo.deserialize(dis);
					if ((magic = dis.readInt()) != SERIALIZATION_MAGIC) {
						throw new ContentException("Invalid source stream content: input stream contains something after the data end!");
					}
				}
			}

			question = repo.termRepo().placeName("?-",null);
			goal = repo.termRepo().placeName(":-",null);
			turnedOn = true;
		}
	}

	@Override
	public void turnOff(OutputStream target) throws ContentException, IOException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("VM already is turned off");
		}
		else {
			turnedOn = false;
			repo.termRepo().removeName(question);
			repo.termRepo().removeName(goal);
			
			if (target != null) {
				final DataOutputStream	dos = new DataOutputStream(target); 
				
				dos.writeInt(SERIALIZATION_MAGIC);		// Write magic
				dos.writeInt(SERIALIZATION_VERSION);	// Write magic
				repo.serialize(dos);					// Write content
				dos.writeInt(SERIALIZATION_MAGIC);		// Write tail
				dos.flush();
			}
			
			try{repo.close();
			} catch (Exception e) {
				throw new ContentException(e.getMessage(),e);
			}
		}
	}

	@Override
	public boolean isTurnedOn() {
		return turnedOn;
	}

	@Override
	public void newFRB(final OutputStream target) throws ContentException, IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
		}
		else if (isTurnedOn()) {
			throw new IllegalStateException("You can't make this operatio when VM is turned on. Turn off VM firstly!");
		}
		else {
			try(final IFProEntitiesRepo	temp = new EntitiesRepo(getDebug(),getParameters())) {
				final DataOutputStream	dos = new DataOutputStream(target); 
				
				dos.writeInt(SERIALIZATION_MAGIC);		// Write magic
				dos.writeInt(SERIALIZATION_VERSION);	// Write magic
				temp.serialize(dos);					// Write content
				dos.writeInt(SERIALIZATION_MAGIC);		// Write tail
				dos.flush();
			} catch (Exception e) {
				throw new ContentException(e.getMessage(),e);
			}
		}
	}
	
	@Override
	public boolean question(final String question, final IFProCallback callback) throws ContentException, IOException, SyntaxException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("You can't make this operatio when VM is turned on. Turn off VM firstly!");
		}
		else {
			return question(question,repo,callback);
		}
	}	
	
	@Override
	public boolean question(final String question, final IFProEntitiesRepo repo, final IFProCallback callback) throws ContentException, IOException, SyntaxException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("You can't make this operatio when VM is turned on. Turn off VM firstly!");
		}
		else {
			return internalGoal(question,repo,callback,this.question,"question");
		}
	}

	@Override
	public boolean goal(final String goal, final IFProCallback callback) throws ContentException, IOException, SyntaxException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("You can't make this operatio when VM is turned on. Turn off VM firstly!");
		}
		else {
			return goal(goal,repo,callback);
		}
	}	
	
	@Override
	public boolean goal(final String goal, final IFProEntitiesRepo repo, final IFProCallback callback) throws ContentException, SyntaxException, IOException {
		return internalGoal(goal,repo,callback,this.goal,"goal");
	}

	@Override
	public void consult(final CharacterSource source) throws SyntaxException, IOException {
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
	public void save(final CharacterTarget target) throws PrintingException, IOException {
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
	public void console(final Reader source, final Writer target, final Writer errors) throws ContentException {
		if (!isTurnedOn()) {
			throw new IllegalStateException("VM is not turned on. Turn on it firstly!");
		}
		else {
			try{final BufferedReader		brdr = new BufferedReader(source);	// Don't close!!!
				final IFProParserAndPrinter pap = new ParserAndPrinter(getDebug(), getParameters(), repo);
				final boolean[]				continuation = new boolean[]{true};
				String						command;
				
				final long	quit = repo.termRepo().placeName("quit",null);
				
				target.write("Funny prolog console...\n>");
				target.flush();
				while (continuation[0] && (command = brdr.readLine()) != null) {
					if (!command.isEmpty()) {
						try{pap.parseEntities(command.toCharArray(),0,new FProParserCallback() {
												@Override
												public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
													try{
													if (entity.getEntityId() == quit && entity.getEntityType().equals(EntityType.predicate) && ((IFProPredicate)entity).getArity() == 0) {
														target.write("quit successful!\n");
														continuation[0] = false;
														return false;
													}
													else if (entity.getEntityId() == goal && entity.getEntityType().equals(EntityType.operator) && ((IFProOperator)entity).getOperatorType().equals(OperatorType.fx)) {
														target.write(String.valueOf(inference(entity,vars,repo,new IFProCallback(){
															@Override public void beforeFirstCall() {}
															@Override public boolean onResolution(final String[] names, final IFProEntity[] resolvedVariables, final String[] printedValues) {return true;}
															@Override public void afterLastCall() {}
														}))+"\n>");
													}
													else if (entity.getEntityId() == question && entity.getEntityType().equals(EntityType.operator) && ((IFProOperator)entity).getOperatorType().equals(OperatorType.fx)) {
														target.write("Answer="+String.valueOf(inference(entity,vars,repo,new IFProCallback(){
															@Override public void beforeFirstCall() {}
															
															@Override 
															public boolean onResolution(final String[] names, final IFProEntity[] resolvedVariables, final String[] printedValues) throws PrintingException, SyntaxException {
																try{for (int index = 0, maxIndex = Math.min(names.length,resolvedVariables.length); index < maxIndex; index++) {
																		final StringBuilder	sb = new StringBuilder();
																		
																		pap.putEntity((IFProEntity)resolvedVariables[index],new StringBuilderCharTarget(sb));
																		target.write(String.format("%1$s = %2$s\n",names[index],sb));
																	}
																	target.write("proceed? ");
																	target.flush();
																} catch (IOException | ContentException e) {
																	throw new PrintingException(e);
																}
	
																try{final String	buffer = brdr.readLine();
																	return "yY+tT".indexOf(buffer == null ? "" : buffer) >= 0;
																} catch (IOException e) {
																	return false;
																}
															}
															
															@Override public void afterLastCall() {}
														}))+"\n>");
													}
													else {
														repo.predicateRepo().assertZ(entity);
														target.write("Predicate was asserted\n>");
													}
													} catch (ContentException exc) {
	//													exc.printStackTrace();
														errors.write(String.format("Error executing input: "+exc.getMessage()));
														return false;
													}
													return true;
												}
											}
								);
						} catch (SyntaxException e) {
							errors.write(e.getMessage());
							throw new ContentException(e.getMessage()); 
						} finally {
							target.flush();
							errors.flush();
						}
					}
				}				
			} catch (Exception e) {
				e.printStackTrace();
				throw new ContentException(e.getMessage()); 
			} finally {
				final long	quitName = repo.termRepo().seekName("quit"); 
				if (quitName >= 0) {
					repo.termRepo().removeName(quitName);
				}
			}
		}
	}

	private boolean internalGoal(final String source, final IFProEntitiesRepo repo, final IFProCallback callback, final long opId, final String awaited) throws ContentException, SyntaxException, IOException {
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
			final boolean[]				result = new boolean[1];
			
			pap.parseEntities(source.toCharArray(),0,new FProParserCallback(){
											@Override
											public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws SyntaxException, IOException {
												if (entity.getEntityId() == opId && entity.getEntityType().equals(EntityType.operator) && ((IFProOperator)entity).getOperatorType().equals(OperatorType.fx)) {
													try{result[0] = inference(entity,vars,repo,callback);
													} catch (ContentException e) {
														e.printStackTrace();
														throw new SyntaxException(0,0,e.getMessage());
													}
												}
												else {
													throw new SyntaxException(0,0,"Illegal source content ("+awaited+" awaited)"); 
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

	private boolean inference(final IFProEntity entity, final List<IFProVariable> vars, final IFProEntitiesRepo repo, final IFProCallback callback) throws ContentException {
		return inference(entity,vars,repo,getStandardResolver(),callback);
	}	
	
	private boolean inference(final IFProEntity entity, final List<IFProVariable> vars, final IFProEntitiesRepo repo, final ResolvableAndGlobal rag, final IFProCallback callback) throws ContentException {
		try(final GlobalStack	stack = new GlobalStack(getDebug(),getParameters(),repo)) {
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
		} catch (Exception e) {
			throw new ContentException(e.getMessage(),e);
		}
	}
	
	static class ResolvableAndGlobal {
		public IResolvable	resolver;
		public Object		global;
		
		public ResolvableAndGlobal(final IResolvable resolver, final Object global) {
			this.resolver = resolver;
			this.global = global;
		}
		
	}
}
