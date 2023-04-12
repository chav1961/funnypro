package chav1961.funnypro;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

/**
 * <p>This class implements stand-alone console-oriented application for the Funny Prolog interpreter</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class Application {
	
	public static void main(String[] args) throws ScriptException {
		final ArgParser			argParser = new ApplicationArgParser();
		
		try(final InputStream				is = Application.class.getResourceAsStream("application.xml");
			final Localizer					localizer = PureLibSettings.PURELIB_LOCALIZER) {
			
			final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
			
			final ScriptEngineManager 		factory = new ScriptEngineManager();
			final ScriptEngine 				engine = factory.getEngineByName("FunnyProlog");
			final ArgParser					parsed = argParser.parse(true,true,args);
			
			if (parsed.getValue("screen",boolean.class)) {
				new JScreen(localizer, xda, (FunnyProEngine)engine, PureLibSettings.CURRENT_LOGGER).setVisible(true);
			}
			else {
				try(final Reader	in = new InputStreamReader(System.in, parsed.getValue("encoding",String.class));
					final Writer	out = new OutputStreamWriter(System.out); 
					final Writer	err = new OutputStreamWriter(System.err)) {
					
					((FunnyProEngine)engine).console(in, out, err);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		} catch (IOException | EnvironmentException | CommandLineParametersException e) {
			System.err.println("Error starting application: "+e.getLocalizedMessage());
			System.exit(129);
		}
	}

	static class ApplicationArgParser extends ArgParser {
		public ApplicationArgParser() {
			super(new BooleanArg("screen",false,false,"Start application with GUI")
				 ,new StringArg("encoding",false,"Source file encoding","UTF-8"));
		}
	}
}
