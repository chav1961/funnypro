package chav1961.funnypro.core.interfaces;

import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.ExternalEntityDescriptor;

/**
 * <p>This interface describes global stack</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public interface IFProGlobalStack extends AutoCloseable {
	/**
	 * <p>This enumeration describes content of the global stack top:</p>
	 * <ul>
	 * <li>andChain - AND node of the expression</li>
	 * <li>orChain - OR node of the expression</li>
	 * <li>iterator - any expression source</li>
	 * <li>bounds - binded variable list</li>
	 * <li>temporary - temporary data</li>
	 * </ul>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	enum StackTopType {
		andChain, orChain, iterator, bounds, temporary, external
	}
	
	/**
	 * <p>This interface describes common stack top element</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	interface GlobalStackTop {
		/**
		 * <p>Get element type</p>
		 * @return element type
		 */
		StackTopType getTopType();
	}

	/**
	 * <p>This interface describes common AND node of the expression </p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	interface AndChainStackTop extends GlobalStackTop {
		/**
		 * <p>Get AND node expression component</p>
		 * @return AND node expression component. Can be empty but not null
		 */
		IFProEntity getEntity();
	}
	
	/**
	 * <p>This interface describes common OR node of the expression </p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	interface OrChainStackTop extends GlobalStackTop {
		/**
		 * <p>Is this node a first component in the OR expression</p>
		 * @return true if yes
		 */
		boolean isFirst();
	}

	/**
	 * <p>This interface describes any data iterator in the stack</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	interface IteratorStackTop<T> extends GlobalStackTop {
		/**
		 * <p>Get iterator</p>
		 * @return data iterator. Can be empty but not null
		 */
		Iterable<T> getIterator();
	}

	/**
	 * <p>This interface describes any binded data list as a result of unification process</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	interface BoundStackTop<T> extends GlobalStackTop {
		/**
		 * <p>Get top mark</p>
		 * @return top mark. Can be empty but not null
		 */
		IFProEntity getMark();
		
		/**
		 * <p>Get change records chain to backtrace the unification</p>
		 * @return change chain. Can be empty, but not null
		 */
		T getChangeChain();
	}

	/**
	 * <p>This interface describes any temporary data placed in the stack</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	interface TemporaryStackTop extends GlobalStackTop {
		/**
		 * <p>Get placed entity</p>
		 * @return entity placed. Can be empty but not null
		 */
		IFProEntity getEntity();
	}

	/**
	 * <p>This interface describes external predicate placed in the stack</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.1
	 */
	interface ExternalStackTop extends GlobalStackTop {
		/**
		 * <p>Get external entity descriptor</p>
		 * @return entity descriptor
		 */
		ExternalEntityDescriptor getDescriptor();
		
		/**
		 * <p>Get local data for entity descriptor</p>
		 * @return local data
		 */
		Object getLocalData();
	}
	
	/**
	 * <p>Is the stack empty</p>
	 * @return true if yes
	 */
	boolean isEmpty();
	
	/**
	 * <p>Clear stack content</p>
	 * @return self
	 */
	IFProGlobalStack clear();
	
	/**
	 * <p>Push entity into stack</p>
	 * @param entity entity to push
	 * @return self
	 */
	IFProGlobalStack push(GlobalStackTop entity);
	
	/**
	 * <p>Peek stack top</p>
	 * @return stack top
	 */
	GlobalStackTop peek();
	
	/**
	 * <p>Pop stack top</p>
	 * @return stack top will be removed
	 */
	GlobalStackTop pop();
}
