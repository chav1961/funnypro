package chav1961.funnypro.core.interfaces;

import java.util.Properties;

import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This interface describes the minimal functionality for the modules</p>
 * @author chav1961
 */
public interface IGentlemanSet {
	/**
	 * <p>Get debug anf log associated with this module</p>
	 * @return debug ang log associated. Can't be null
	 */
	LoggerFacade getDebug();
	
	/**
	 * <p>Get Parameters associated with this module</p>
	 * @return parameters associated. Can't be null
	 */
	Properties getParameters();
}
