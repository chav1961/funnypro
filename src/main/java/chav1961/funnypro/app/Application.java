package chav1961.funnypro.app;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import chav1961.funnypro.gui.JScreen;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.useful.JSimpleSplash;

/**
 * <p>This class implements stand-alone console-oriented application for the Funny Prolog interpreter</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class Application {
	private static final String		PARM_SCREEN = "screen";
	private static final String		PARM_ENCODING = "encoding";
	
	public static void main(String[] args) throws ScriptException {
		final ArgParser			argParser = new ApplicationArgParser();
		
		try(final InputStream		is = JScreen.class.getResourceAsStream("application.xml")) {
			final ArgParser			parsed = argParser.parse(true, true, args);
			
			if (parsed.getValue(PARM_SCREEN, boolean.class)) {
				try(final JSimpleSplash		jss = new JSimpleSplash()) {
					jss.start("Loading...");					

					final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
					final ScriptEngine 				engine = new ScriptEngineManager().getEngineByName(FunnyProEngineFactory.LANG_NAME);
					new JScreen(PureLibSettings.PURELIB_LOCALIZER, xda, (FunnyProEngine)engine).setVisible(true);
				}
			}
			else {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				final ScriptEngine 				engine = new ScriptEngineManager().getEngineByName(FunnyProEngineFactory.LANG_NAME);
				final String		encoding = parsed.getValue(PARM_ENCODING, String.class);
				
				try(final Reader	in = new InputStreamReader(System.in, encoding);
					final Writer	out = new OutputStreamWriter(System.out, encoding); 
					final Writer	err = new OutputStreamWriter(System.err, encoding)) {
					
					((FunnyProEngine)engine).console(in, out, err);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		} catch (CommandLineParametersException e) {
			System.err.println(e.getLocalizedMessage());
			System.err.println(argParser.getUsage("funnypro"));
			System.exit(128);
		} catch (IOException | EnvironmentException e) {
			System.err.println("Error starting application: "+e.getLocalizedMessage());
			System.exit(129);
		}
	}

	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new BooleanArg(PARM_SCREEN, false, false, "Start application with GUI")
				 ,new StringArg(PARM_ENCODING, false, "Source file encoding", PureLibSettings.DEFAULT_CONTENT_ENCODING));
		}
	}
}
