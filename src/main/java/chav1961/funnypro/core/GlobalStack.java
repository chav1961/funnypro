package chav1961.funnypro.core;

import java.util.Properties;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.ExternalEntityDescriptor;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProModule;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class GlobalStack implements IFProGlobalStack, IFProModule {
	private final LoggerFacade		log;
	private final Properties		props;
	private final IFProEntitiesRepo	repo;
	
	private StackTop				top = null;

	public GlobalStack(final LoggerFacade log, final Properties prop, final IFProEntitiesRepo repo) throws NullPointerException {
		if (log == null) {
			throw new NullPointerException("Log can't be null"); 
		}
		else if (prop == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else if (repo == null) {
			throw new NullPointerException("Repo can't be null"); 
		}
		else {
			this.log = log;			this.props = prop;
			this.repo = repo;
		}
	}

	@Override public LoggerFacade getDebug() {return log;}
	@Override public Properties getParameters() {return props;}

	@Override
	public void close() throws Exception {
		clear();
	}
	
	public IFProEntitiesRepo getRepo() {
		return repo;
	}	

	@Override public boolean isEmpty() {return top == null;}
	
	@Override
	public IFProGlobalStack clear() {
		StackTop	temp;
		
		while (top != null) {
			top.entity = null;		
			temp = top.prev;
			top.prev = null;
			top = temp;
		}
		return this;
	}

	@Override
	public IFProGlobalStack push(final GlobalStackTop entity) throws NullPointerException {
		if (entity == null) {
			throw new NullPointerException("Entity can't be null");
		}
		else {
			final StackTop	newTop = new StackTop();
			
			newTop.prev = top;
			newTop.entity = entity;
			top = newTop;
			return this;
		}
	}

	@Override
	public GlobalStackTop peek() {
		if (isEmpty()) {
			return null;
		}
		else {
			return top.entity;
		}
	}
	
	@Override
	public StackTopType getTopType() {
		return top == null ? null : top.entity.getTopType();
	}	
	
	@Override
	public GlobalStackTop pop() {
		if (top == null) {
			throw new IllegalStateException("Stack exhaused!");
		}
		else {
			final GlobalStackTop	entity = peek();
			final StackTop			temp = top.prev;
			
			top.prev = null;		top = temp;
			return entity;
		}
	}

	@Override
	public String toString() {
		return "GlobalStack {"+top+"}";
	}

	private static class StackTop {
		public StackTop 		prev; 
		public GlobalStackTop	entity;
		
		@Override
		public String toString() {
			return "StackTop [entity=" + entity + ", prev=" + prev + "]";
		}
	}
	
	public static AndChainStackTop getAndChainStackTop(final IFProEntity entity) throws NullPointerException {
		if (entity == null) {
			throw new NullPointerException("Entity can't be null");
		}
		else {
			return new AndChainStackTop(){
				@Override public StackTopType getTopType() {return StackTopType.andChain;}
				@Override public IFProEntity getEntity() {return entity;}
				@Override public IFProEntity getEntityAssicated() {return entity;}
				@Override public String toString() {return "AndChainStackTop [assoc=+getEntityAssicated()]";}
			};
		}
	}

	public static OrChainStackTop getOrChainStackTop(final IFProEntity entity, final boolean isFirst) throws NullPointerException {
		if (entity == null) {
			throw new NullPointerException("Entity can't be null");
		}
		else {
			return new IFProGlobalStack.OrChainStackTop(){
				@Override public StackTopType getTopType() {return StackTopType.orChain;}
				@Override public boolean isFirst() {return isFirst;}
				@Override public IFProEntity getEntityAssicated() {return entity;}
				@Override public String toString(){return "OrChainStackTop [isFirst="+isFirst()+", assoc="+getEntityAssicated()+"]";}
			};
		}
	}

	public static <T> IteratorStackTop<T> getIteratorStackTop(final IFProEntity entity, final Iterable<T> iterator, final Class<T> clazz) throws NullPointerException {
		if (iterator == null) {
			throw new NullPointerException("Iterator can't be null");
		}
		else if (clazz == null) {
			throw new NullPointerException("Iterator content class can't be null");
		}
		else {
			return new IteratorStackTop<T>(){
				@Override public StackTopType getTopType() {return StackTopType.iterator;}
				@Override public Iterable<T> getIterator() {return iterator;}
				@Override public IFProEntity getEntityAssicated() {return entity;}
				@Override public String toString(){return "IteratorStackTop [iteratorClass="+clazz+", assoc="+getEntityAssicated()+"]";}
			};
		}
	}
	
	public static BoundStackTop<Change> getBoundStackTop(final IFProEntity entity, final IFProEntity mark, final Change change) throws NullPointerException {
		if (mark == null) {
			throw new NullPointerException("Mark can't be null");
		}
		else if (change == null) {
			throw new NullPointerException("Change chain can't be null");
		}
		else {
			return new BoundStackTop<Change>(){
				@Override public StackTopType getTopType() {return StackTopType.bounds;}
				@Override public Change getChangeChain() {return change;}
				@Override public IFProEntity getMark() {return mark;}
				@Override public IFProEntity getEntityAssicated() {return entity;}
				@Override public String toString(){return "BoundStackTop [mark="+getMark()+", assoc="+getEntityAssicated()+"]";}
			};
		}
	}

	public static TemporaryStackTop getTemporaryStackTop(final IFProEntity entity, final IFProEntity temporary) throws NullPointerException {
		if (temporary == null) {
			throw new NullPointerException("Entity can't be null");
		}
		else {
			return new TemporaryStackTop(){
				@Override public StackTopType getTopType() {return StackTopType.temporary;}
				@Override public IFProEntity getEntity() {return temporary;}
				@Override public IFProEntity getEntityAssicated() {return entity;}
				@Override public String toString(){return "TemporaryStackTop [entity="+getEntity()+", assoc="+getEntityAssicated()+"]";}
			};
		}
	}

	public static ExternalStackTop getExternalStackTop(final IFProEntity entity, final ExternalEntityDescriptor<?> desc, final Object localData) throws NullPointerException {
		if (desc == null) {
			throw new NullPointerException("Entity descriptor can't be null");
		}
		else {
			return new ExternalStackTop(){
				@Override public StackTopType getTopType() {return StackTopType.external;}
				@Override public ExternalEntityDescriptor<?> getDescriptor() {return desc;}
				@Override public Object getLocalData() {return localData;}
				@Override public IFProEntity getEntityAssicated() {return entity;}
				@Override public String toString() {return "ExternalStackTop{"+desc+", assoc="+getEntityAssicated()+"}";}
			};
		}
	}
}
