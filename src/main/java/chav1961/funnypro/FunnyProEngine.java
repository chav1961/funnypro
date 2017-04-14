package chav1961.funnypro;

import java.io.Reader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

class FunnyProEngine extends AbstractScriptEngine {
	private final ScriptEngineFactory	factory;
	
	FunnyProEngine(final ScriptEngineFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		// TODO Auto-generated method stub
		System.err.println("CALL: "+script);
		return null;
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		// TODO Auto-generated method stub
		System.err.println("CALL2: "+reader);
		return null;
	}

	@Override
	public Bindings createBindings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}
}
