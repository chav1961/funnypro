package chav1961.funnypro;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
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
		String		encoding = "UTF8";
		boolean		screenMode = false;
		
		for (String item : args) {
			switch (item) {
				case "-screen"	:
					screenMode = true;
					break;
				default :
					try{"test".getBytes(item);
						encoding = item;
					} catch (UnsupportedEncodingException e) {
						System.err.println("Unsupported encoding ["+item+"] was typed.");
						printUsage();
						System.exit(128);
					}
					break;
			}
		}
		
		try(final InputStream				is = Application.class.getResourceAsStream("application.xml");
			final Localizer					localizer = PureLibSettings.PURELIB_LOCALIZER;
			final LoggerFacade				logger = new SystemErrLoggerFacade()) {
			final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
			final ScriptEngineManager 		factory = new ScriptEngineManager();
			final ScriptEngine 				engine = factory.getEngineByName("FunnyProlog");
			
			if (screenMode) {
				new JScreen(localizer,xda,(FunnyProEngine)engine,logger);
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
		} catch (IOException | EnvironmentException e) {
			System.err.println("Error starting application: "+e.getLocalizedMessage());
			System.exit(129);
		}
	}

	private static void printUsage() {
		System.err.println("Use: java -jar funnypro.jar [-screen] [<encoding>]");
	}
}
