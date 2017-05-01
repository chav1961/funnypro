package chav1961.funnypro;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Properties;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
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

/**
 * <p>This class implements engine for the Funny prolog interpreter. All requirements about it can be read in the {@link AbstractScriptEngine}
 * description</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */

class FunnyProEngine extends AbstractScriptEngine implements Closeable, IFProVM {
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
		try(final PrintWriter	out = new PrintWriter(context.getWriter());
			final PrintWriter	err = new PrintWriter(context.getErrorWriter());) {
			
			console(reader,out,err);
			out.flush();
			err.flush();
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
	public void consult(final Reader source) throws FProParsingException, IOException {
		vm.consult(source);
	}

	@Override
	public void save(final PrintWriter target) throws FProPrintingException, IOException {
		vm.save(target);
	}

	@Override
	public void console(final Reader source, final PrintWriter target, final PrintWriter errors) throws FProException {
		vm.console(source, target, errors);
	}
}
