package chav1961.funnypro.core.interfaces;

import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.StandardResolver;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

/**
 * <p>This interface describes any external predicates/operators plugin. Life cycle of every plugin is:</p>
 * <h3>Plugin loading</h3>
 * <p>Load plugin and call its {@link IResolvable#onLoad(LoggerFacade, Properties, IFProEntitiesRepo)} method.
 * Common practice is to register all predicates and operators that the given plugin supports (see {@link StandardResolver}
 * class source code for example). The plugin also can create any structure for it's own purposes and return it as a result
 * of calling this method. This structure will be passed to all other methods in the 'global' parameter.</p>  
 * <h3>Resolving predicate</h3>
 * <p>A life cycle to resolve one of the registered predicates (see below)</p>
 * <h3>Plugin removing</h3>
 * <p>Remove plugin and free it's resources. Common practice is to deregister all the registered predicates and operators and
 * free global structure, created by {@link IResolvable#onLoad(LoggerFacade, Properties, IFProEntitiesRepo)} method.</p>
 * <p>The life cycle of the resolving predicate is:</p>
 * <h3>Preparing resolving</h3>
 * <p>Prepare resolving process and call {@link IResolvable#beforeCall(Object, IFProGlobalStack, List, IFProCallback)} method.
 * Plugin can create any structure for it's own purposes and return it as a result of calling method. This structure will be 
 * passed to all other methods in the 'local' parameter.</p>   
 * <h3>The first resolution</h3>
 * <p>Forward resolution of the predicate/operator by calling {@link IResolvable#firstResolve(Object, Object, IFProEntity)}. Returns
 * {@link ResolveRC} instance as a result.</p>
 * <h3>The backtrace</h3>
 * <p>This is an optional step and executes only if the first resolution was successful. Calling {@link IResolvable#nextResolve(Object, Object, IFProEntity)} method.
 * Returns {@link ResolveRC} instance as a result.</p>
 * <h3>End resolving</h3>
 * <p>Finish resolving the given predicate/operator and call {@link IResolvable#endResolve(Object, Object, IFProEntity)}.</p>
 * <p>Unprepare resolving</p>
 * <p>Calling {@link IResolvable#afterCall(Object, Object)}. Common practice is to free structures returning by {@link IResolvable#beforeCall(Object, IFProGlobalStack, List, IFProCallback)}
 * method</p>     
 * @param <Global> type of the global object for the plugin
 * @param <Local> type of the local object for the plugin
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IResolvable<Global,Local> {
	/**
	 * <p>This enumeration describes return codes from resolving results. They can be:</p>
	 * <ul>
	 * <li>False - resolution unsuccessful.</li> 
	 * <li>True - resolution successful.</li> 
	 * <li>FalseWithoutBacktracking - resolution unsuccessful, backtracking need be rejected.</li> 
	 * <li>UltimateFalse - a special code for cutting predicate (!). Avoid using it in your own plugins.</li> 
	 * </ul>
	 * 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	public enum ResolveRC {
		False, True, FalseWithoutBacktracking, TrueWithoutBacktracking, UltimateFalse
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
	 * @param repo entities repo to use with. Can be stored in the plugin for longer using
	 * @return global object to use with longer calls
	 * @throws SyntaxException any plugin loading problem
	 * @throws PreparationException plugin in not prepared correctly and must be excluded from plugin list 
	 */
	Global onLoad(LoggerFacade debug, SubstitutableProperties parameters, IFProEntitiesRepo repo) throws SyntaxException, PreparationException;
	
	/**
	 * <p>This method will be called before removing</p>
	 * @param global object to use with longer calls (see {@link #onLoad(LoggerFacade, Properties, IFProEntitiesRepo)}
	 * @throws SyntaxException any plugin removing problem
	 */
	void onRemove(Global global) throws SyntaxException;
	
	/**
	 * <p>This method will be called before first call of the predicate</p>
	 * @param global object to use with longer calls (see {@link #onLoad(LoggerFacade, Properties, IFProEntitiesRepo)}
	 * @param gs global stack for the resolving
	 * @param vars variables from the predicate to use. Can be null
	 * @param callback to use for the inference
	 * @return local object to use with longer calls
	 * @throws SyntaxException if any problems was detected
	 */
	Local beforeCall(Global global, IFProGlobalStack gs, List<IFProVariable> vars, IFProCallback callback) throws SyntaxException;
	
	/**
	 * <p>This method will be called on the first resolution</p>
	 * @param global object to use with longer calls (see {@link #onLoad(LoggerFacade, Properties, IFProEntitiesRepo)})
	 * @param local object to use with longer calls (see {@link #beforeCall(Object, IFProGlobalStack, List, IFProCallback)})
	 * @param entity entity to resolve
	 * @return resolution result (see {@link ResolveRC})
	 * @throws SyntaxException if any problems was detected
	 */
	ResolveRC firstResolve(Global global, Local local, IFProEntity entity) throws SyntaxException;
	
	/**
	 * <p>This method will be called on the sequential next resolution. Optional (if firstResolve was false, will not be called)</p>
	 * @param global object to use with longer calls (see {@link #onLoad(LoggerFacade, Properties, IFProEntitiesRepo)}
	 * @param local object to use with longer calls (see {@link #beforeCall(Object, IFProGlobalStack, List, IFProCallback)}
	 * @param entity entity to resolve
	 * @return resolution result (see {@link ResolveRC})
	 * @throws SyntaxException if any problems was detected
	 */
	ResolveRC nextResolve(Global global, Local local, IFProEntity entity) throws SyntaxException;
	
	/**
	 * <p>This method ends resolution loop</p> 
	 * @param global object to use with longer calls (see {@link #onLoad(LoggerFacade, Properties, IFProEntitiesRepo)}
	 * @param local object to use with longer calls (see {@link #beforeCall(Object, IFProGlobalStack, List, IFProCallback)}
	 * @param entity entity to resolve
	 * @throws SyntaxException if any problems was detected
	 */
	void endResolve(Global global, Local local, IFProEntity entity) throws SyntaxException;
	
	/**
	 * <p>This method calls after resolution loop</p>  
	 * @param global object to use with longer calls (see {@link #onLoad(LoggerFacade, Properties, IFProEntitiesRepo)}
	 * @param local object to use with longer calls (see {@link #beforeCall(Object, IFProGlobalStack, List, IFProCallback)}
	 * @throws SyntaxException if any problems was detected
	 */
	void afterCall(Global global, Local local) throws SyntaxException;
}

