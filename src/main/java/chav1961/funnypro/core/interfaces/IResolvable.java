package chav1961.funnypro.core.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This interface describes any external predicates/operators</p>
 */
public interface IResolvable {
	public enum ResolveRC {
		False, True, FalseWithoutBacktracking, UltimateFalse
	}
	
	/**
	 * <p>Get implementation module name</p>
	 * @return implementation module name
	 */
	String getName();
	
	/**
	 * <p>Get implementation module version</p>
	 * @return implementation module version
	 */
	int[] getVersion();
	
	/**
	 * <p>This method will be called after load</p>
	 * @param debug log to use
	 * @param parameters to use
	 * @param entites repo to use with. Can be stored in the plugin for longer using
	 * @return global object to use with longer calls
	 * @throws FProException
	 */
	Object onLoad(LoggerFacade debug, Properties parameters, IFProEntitiesRepo repo) throws FProException;
	
	/**
	 * <p>This method will be called before removing</p>
	 * @param global object to use with longer calls 
	 * @throws FProException
	 */
	void onRemove(Object global) throws FProException;
	
	/**
	 * <p>This method will be called
	 * @return global object to use with longer calls
	 * @param gs global stack for the resolving
	 * @param vars variables from the predicate to use. Can be null
	 * @param callback to use for the inference
	 * @return local object to use with longer calls
	 * @throws FProException
	 */
	Object beforeCall(Object global, IFProGlobalStack gs, List<IFProVariable> vars, IFProCallback callback) throws FProException;
	
	/**
	 * <p>This method will be called on the first resolution</p>
	 * @return global object to use with longer calls
	 * @return local object to use with longer calls
	 * @param entity entity to resolve
	 * @return true if resolution is successful
	 * @throws FProException
	 */
	ResolveRC firstResolve(Object global, Object local, IFProEntity entity) throws FProException;
	
	/**
	 * <p>This method will be called on the sequential next resolution. Optional (if firstResolve was false, will not be called)</p>
	 * @return global object to use with longer calls
	 * @return local object to use with longer calls
	 * @param entity entity to resolve
	 * @return true if resolution is successful
	 * @throws FProException
	 */
	ResolveRC nextResolve(Object global, Object local, IFProEntity entity) throws FProException;
	
	/**
	 * <p>This method ends resolution loop</p> 
	 * @return global object to use with longer calls
	 * @return local object to use with longer calls
	 * @param entity entity to resolve
	 * @throws FProException
	 */
	void endResolve(Object global, Object local, IFProEntity entity) throws FProException;
	
	/**
	 * <p>This method calls after resolution loop</p>  
	 * @return global object to use with longer calls
	 * @return local object to use with longer calls
	 * @throws FProException
	 */
	void afterCall(Object global, Object local) throws FProException;
}

