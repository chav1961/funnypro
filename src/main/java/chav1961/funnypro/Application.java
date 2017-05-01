package chav1961.funnypro;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Application {

	public static void main(String[] args) throws ScriptException {
		try{final ScriptEngineManager 	factory = new ScriptEngineManager();
			final ScriptEngine 			engine = factory.getEngineByName("FunnyProlog");

			engine.eval("print('Hello, World')");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
