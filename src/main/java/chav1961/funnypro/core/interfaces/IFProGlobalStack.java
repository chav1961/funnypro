package chav1961.funnypro.core.interfaces;

/**
 * <p>This interface describes global stack</p>
 * @author chav1961
 *
 */
public interface IFProGlobalStack extends AutoCloseable{
	public enum StackTopType {
		andChain, orChain, iterator, bounds, temporary
	}
	
	public interface GlobalStackTop {
		StackTopType getTopType();
	}

	public interface AndChainStackTop extends GlobalStackTop {
		IFProEntity getEntity();
	}
	
	public interface OrChainStackTop extends GlobalStackTop {
		boolean isFirst();
	}

	public interface IteratorStackTop<T> extends GlobalStackTop {
		Iterable<T> getIterator();
	}

	public interface BoundStackTop<T> extends GlobalStackTop {
		IFProEntity getMark();
		T getChangeChain();
	}

	public interface TemporaryStackTop extends GlobalStackTop {
		IFProEntity getEntity();
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
