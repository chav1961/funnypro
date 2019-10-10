package chav1961.funnypro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import chav1961.funnypro.core.exceptions.FProException;

/**
 * <p>This class implements engine factory for the Funny Prolog interpreter. All requirements about it can be read in the {@link ScriptEngineFactory}
 * description. This factory use SPI protocol to incorporate into JRE and uses <b>META-INF\services\javax.script.ScriptEngineFactory</b> file for it</p>
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * @see chav1961.purelib.basic JUnit tests
 * @see FunnyProEngine
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class FunnyProEngineFactory implements ScriptEngineFactory {
	private static final String			ENGINE_NAME = "Funny Prolog interpreter engine";
	private static final String			ENGINE_VERSION = "1.0";
	private static final String			LANG_NAME = "FunnyProlog";
	private static final String			LANG_VERSION = "1.0";
	private static final List<String>	SCRIPT_EXTENSIONS = new ArrayList<>();
	private static final List<String>	SCRIPT_MIMES = new ArrayList<>();
	private static final List<String>	SCRIPT_NAMES = new ArrayList<>();
	
	static {
		SCRIPT_EXTENSIONS.add("fpro");
		SCRIPT_MIMES.add("text/funnyprolog");
		SCRIPT_NAMES.add(LANG_NAME);
	}
	
	public FunnyProEngineFactory() {
	}
	
	@Override public String getEngineName() {return ENGINE_NAME;}
	@Override public String getEngineVersion() {return ENGINE_VERSION;}
	@Override public List<String> getExtensions() {return SCRIPT_EXTENSIONS;}
	@Override public List<String> getMimeTypes() {return SCRIPT_MIMES;}
	@Override public List<String> getNames() {return SCRIPT_NAMES;}
	@Override public String getLanguageName() {return LANG_NAME;}
	@Override public String getLanguageVersion() {return LANG_VERSION;}

	@Override
	public Object getParameter(final String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key parameter can't be null"); 
		}
		else {
			switch (key) {
				case ScriptEngine.ENGINE			: return getEngineName();
				case ScriptEngine.ENGINE_VERSION	: return getEngineVersion();
				case ScriptEngine.NAME				: return getNames(); 
				case ScriptEngine.LANGUAGE			: return getLanguageName();
				case ScriptEngine.LANGUAGE_VERSION	: return getLanguageVersion();
				case "THREADING"					: return null;
				default : return null;
			}
		}
	}

	@Override
	public String getMethodCallSyntax(final String obj, final String m, final String... args) {
		final StringBuilder	ret = new StringBuilder(obj);
		  
		ret.append("." + m + "(");
		for (int i = 0; i < args.length; i++) {
			ret.append(args[i]);
			if (i < args.length - 1) {
				ret.append(",");
			}
		}
		return ret.append(")").toString();
	}

	@Override
	public String getOutputStatement(final String toDisplay) {
		return "print(" + toDisplay + ")";
	}

	@Override
	public String getProgram(final String... statements) {
		 final StringBuilder	ret = new StringBuilder();
	      
	      for (int i = 0; i < statements.length; i++) {
	          ret.append(statements[i]).append(";\n");
	      }
	      return ret.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		try{return new FunnyProEngine(this);
		} catch (FProException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
