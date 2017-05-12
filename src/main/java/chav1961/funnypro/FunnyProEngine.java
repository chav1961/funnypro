package chav1961.funnypro;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Properties;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import chav1961.funnypro.core.FProVM;
import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProVM;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class implements engine for the Funny prolog interpreter. All requirements about it can be read in the {@link AbstractScriptEngine}
 * description of the javax.script package. To get engine, use code:</p>
 * <code> 
 *	final ScriptEngineManager factory = new ScriptEngineManager();<br>
 *	final FunnyPrologEngine engine = (FunnyPrologEngine)factory.getEngineByName("FunnyProlog");<br>
 * </code>
 * 
 * <p>This engine implements the {@link Closeable} interface, so can be used in the <b>try-with-resource</b> statements.</p>
 * <p>This engine contains a Funny Prolog virtual machine inside, named <b>FProVM</b>. Life cycle of FProVm is:</p>
 * <code>
 * 1. {@link IFProVM#turnOn(InputStream)} - start VM and deserialize it's persistent database from the input stream<br>
 * 2. Use {@link IFProVM#consult(CharacterSource)}, {@link IFProVM#save(CharacterTarget)}, {@link IFProVM#console(Reader, Writer, Writer)},
 * {@link IFProVM#goal(String, IFProEntitiesRepo, chav1961.funnypro.core.interfaces.IFProVM.IFProCallback)} etc.<br>    
 * 3. {@link IFProVM#turnOff(OutputStream)} - stop VM and serialize it's persistent database to the output stream<br>
 * </code>
 * <p>You also can create empty persistent database by calling {@link IFProVM#newFRB(OutputStream)}. After getting this class by 
 * {@link ScriptEngineManager#getEngineByName(String)}, the FProVM is turned on with the empty persistent database. Calling {@link FunnyProEngine#close()}
 * will turns off the FProVM.</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see javax.script
 * @see javax.script.AbstractScriptEngine 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

public class FunnyProEngine extends AbstractScriptEngine implements Closeable, IFProVM {
	public static final String			LOGGER = "logger";
	public static final String			PARAMETERS = "parameters";
	
	private final ScriptEngineFactory	factory;
	private final Bindings				currentBingdings = new SimpleBindings();
	private final FProVM				vm;
	private final LoggerFacade			logger = new SystemErrLoggerFacade();
	
	FunnyProEngine(final ScriptEngineFactory factory) throws FProException, IOException {
		this.factory = factory;
		this.vm = new FProVM(logger,new Properties());
		this.vm.turnOn(null);
	}

	@Override
	public void close() throws IOException {
		try{vm.turnOff(new OutputStream(){public void write(int data) throws IOException {}});
		} catch (FProException e) {
		}
		vm.close();
	}
	
	@Override
	public Object eval(final String script, ScriptContext context) throws ScriptException {
		try(final Reader	rdr = new StringReader(script)) {
			return eval(rdr,context);
		} catch (IOException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		try{console(reader,context.getWriter(),context.getErrorWriter());
		} catch (FProException e) {
			throw new ScriptException(e);
		}
		return null;
	}

	@Override
	public Bindings createBindings() {
		return currentBingdings;
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	@Override
	public void turnOn(final InputStream source) throws FProException, IOException {
		vm.turnOn(source);
	}

	@Override
	public void turnOff(final OutputStream target) throws FProException, IOException {
		vm.turnOff(target);
	}

	@Override
	public void newFRB(final OutputStream target) throws FProException, IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target stream can't be null"); 
		}
		else {
			vm.newFRB(target);
		}
	}

	@Override
	public boolean isTurnedOn() {
		return vm.isTurnedOn();
	}

	@Override
	public boolean question(final String question, final IFProCallback callback) throws FProException, IOException {
		return vm.question(question, callback);
	}

	@Override
	public boolean question(final String question, final IFProEntitiesRepo repo, final IFProCallback callback) throws FProException, IOException {
		return vm.question(question, repo, callback);
	}

	@Override
	public boolean goal(final String goal, final IFProCallback callback) throws FProException, IOException {
		return vm.goal(goal, callback);
	}

	@Override
	public boolean goal(final String goal, final IFProEntitiesRepo repo, final IFProCallback callback) throws FProException, IOException {
		return vm.goal(goal,repo,callback);
	}

	@Override
	public void consult(final CharacterSource source) throws FProParsingException, IOException {
		vm.consult(source);
	}

	@Override
	public void save(final CharacterTarget target) throws FProPrintingException, IOException {
		vm.save(target);
	}

	@Override
	public void console(final Reader source, final Writer target, final Writer errors) throws FProException {
		vm.console(source, target, errors);
	}
}
