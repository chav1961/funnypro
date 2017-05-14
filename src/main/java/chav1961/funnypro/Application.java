package chav1961.funnypro;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * <p>This class implements stand-alone console-oriented application for the Funny Prolog interpreter</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class Application {
	public static void main(String[] args) throws ScriptException {
		try(final Reader	in = new InputStreamReader(System.in,args.length == 0 ? "UTF8" : args[0]);
			final Writer	out = new OutputStreamWriter(System.out); 
			final Writer	err = new OutputStreamWriter(System.err)) {
			
			final ScriptEngineManager 	factory = new ScriptEngineManager();
			final ScriptEngine 			engine = factory.getEngineByName("FunnyProlog");

			((FunnyProEngine)engine).console(in,out,err);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
