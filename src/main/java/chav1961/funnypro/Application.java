package chav1961.funnypro;

import java.io.IOException;
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
		String		encoding = "UTF8";
		boolean		screenMode = false;
		
		for (String item : args) {
			switch (item) {
				case "-screen"	:
					screenMode = true;
					break;
				default :
					encoding = item;
					break;
			}
		}

		final ScriptEngineManager 	factory = new ScriptEngineManager();
		final ScriptEngine 			engine = factory.getEngineByName("FunnyProlog");
		
		if (screenMode) {
			try{final JScreen		screen = new JScreen((FunnyProEngine)engine);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		else {
			try(final Reader	in = new InputStreamReader(System.in,encoding);
				final Writer	out = new OutputStreamWriter(System.out); 
				final Writer	err = new OutputStreamWriter(System.err)) {
				
				((FunnyProEngine)engine).console(in,out,err);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
	}
}
