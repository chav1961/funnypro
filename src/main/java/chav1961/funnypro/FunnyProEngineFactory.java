package chav1961.funnypro;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class FunnyProEngineFactory implements ScriptEngineFactory {
	private static final String			ENGINE_NAME = "MyEngineName";
	private static final String			ENGINE_VERSION = "1.0";
	private static final String			LANG_NAME = "myLang";
	private static final String			LANG_VERSION = "1.0";
	private static final List<String>	SCRIPT_EXTENSIONS = new ArrayList<String>(){{add("mxt");}};
	private static final List<String>	SCRIPT_MIMES = new ArrayList<String>(){{add("text/mxt");}};
	private static final List<String>	SCRIPT_NAMES = new ArrayList<String>(){{add(LANG_NAME);}};
	
	public FunnyProEngineFactory() {
		System.err.println("Call");
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
		return new FunnyProEngine(this);
	}
}
