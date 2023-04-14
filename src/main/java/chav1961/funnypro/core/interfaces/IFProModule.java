package chav1961.funnypro.core.interfaces;

import java.util.Properties;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;

/**
 * <p>This interface describes the minimal functionality for the modules</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProModule extends LoggerFacadeOwner {
	/**
	 * <p>Get debug and log associated with this module</p>
	 * @return debug and log associated. Can't be null
	 */
	LoggerFacade getDebug();
	
	/**
	 * <p>Get Parameters associated with this module</p>
	 * @return parameters associated. Can't be null
	 */
	Properties getParameters();
	
	@Override
	default LoggerFacade getLogger() {
		return getDebug();
	}
}
