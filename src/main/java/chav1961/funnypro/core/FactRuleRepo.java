package chav1961.funnypro.core;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProModule;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProQuickList;
import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.funnypro.core.interfaces.IFProStreamSerializable;
import chav1961.purelib.basic.ReusableInstances;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class FactRuleRepo implements IFProRepo, IFProStreamSerializable, IFProModule {
	private static final int					SERIALIZATION_MAGIC = 0x12122080;
	
	private final LoggerFacade					log;
	private final SubstitutableProperties		props;
	private final SyntaxTreeInterface<?>		repo;
	@SuppressWarnings("unchecked")
	private IFProQuickList<ChainDescriptor>[]	predicates = new QuickList[IFProPredicate.MAX_ARITY];
	private ReusableInstances<Change[]>			tempChanges = new ReusableInstances<>(()->{return new Change[1];}); 

	public FactRuleRepo(final LoggerFacade log, final SubstitutableProperties prop, final SyntaxTreeInterface<?> repo) throws NullPointerException {
		if (log == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (prop == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else if (repo == null) {
			throw new NullPointerException("String repo can't be null"); 
		}
		else {
			this.log = log;			
			this.props = prop;
			this.repo = repo;
		}
	}
	
	@Override public LoggerFacade getDebug() {return log;}
	@Override public Properties getParameters() {return props;}	
	@Override public boolean canUseInMultiThread() {return false;}
	@Override public boolean canUseForMultiThreadOnResolution() {return false;}

	@Override
	public void assertA(final IFProEntity value) throws NullPointerException, IllegalArgumentException {
		if (value == null) {
			throw new NullPointerException("Value can't be null!");
		}
		else {
			switch (value.getEntityType()) {
				case operator : 
					switch (((IFProOperator)value).getOperatorType()) {
						case fx : case fy : case xf : case yf : 
							assertValue(1,true,value);
							break;
						default :
							assertValue(2,true,value);
							break;
					}
					break;
				case predicate :
					assertValue(((IFProPredicate)value).getArity(),true,value);
					break;
				default :
					throw new IllegalArgumentException("Predicate type ["+value.getEntityType()+"] can't be stored directly to the fact/rule base");
			}
		}
	} 

	@Override
	public void assertZ(final IFProEntity value) throws NullPointerException, IllegalArgumentException {
		if (value == null) {
			throw new NullPointerException("Value can't be null!");
		}
		else {
			switch (value.getEntityType()) {
				case operator :
					switch (((IFProOperator)value).getOperatorType()) {
						case fx : case fy : case xf : case yf :
							assertValue(1,false,value);
							break;
						default :
							assertValue(2,false,value);
							break;
					}
					break;
				case predicate :
					assertValue(((IFProPredicate)value).getArity(),false,value);
					break;
				default :
					throw new IllegalArgumentException("Predicate type ["+value.getEntityType()+"] can't be stored directly to the fact/rule base");
			}
		}
	}

	@Override
	public void retractAll(final IFProEntity value) throws NullPointerException, IllegalArgumentException {
		if (value == null) {
			throw new NullPointerException("Value can't be null!");
		}
		else {
			while (retractFirst(value)) {
			}
		}
	}

	@Override
	public void retractAll(IFProEntity value, int module) throws NullPointerException, IllegalArgumentException {
		throw new UnsupportedOperationException("Not supported!"); 
	}

	@Override
	public void retractAll(final long id, final int arity) throws NullPointerException, IllegalArgumentException {
		removeChain(arity,id);
	}
	
	@Override
	public boolean retractFirst(final IFProEntity value) throws NullPointerException, IllegalArgumentException {
		if (value == null) {
			throw new NullPointerException("Value can't be null!");
		}
		else {
			final Change[]	temp = tempChanges.allocate();

			try{switch (value.getEntityType()) {
					case operator :
						switch (((IFProOperator)value).getOperatorType()) {
							case fx : case fy : case xf : case yf :
								return removeFromChain(1,value.getEntityId(),value,temp);
							default :
								return removeFromChain(2,value.getEntityId(),value,temp);
						}
					case predicate :
						return removeFromChain(((IFProPredicate)value).getArity(),value.getEntityId(),value,temp);
					default :
						throw new IllegalArgumentException("Entity type ["+value.getEntityType()+"] can't be removed directly from the fact/rule base");
				}
			} finally {
				tempChanges.free(temp);
			}
		}
	}
	
	@Override
	public Iterable<IFProEntity> call(final IFProEntity value) throws NullPointerException, IllegalArgumentException {
		return call(value,0);
	}

	@Override
	public Iterable<IFProEntity> call(IFProEntity value, int module) throws NullPointerException, IllegalArgumentException {
		if (value == null) {
			throw new NullPointerException("Value can't be null!");
		}
		else {
			switch (value.getEntityType()) {
				case operator :
					switch (((IFProOperator)value).getOperatorType()) {
						case fx : case fy : case xf : case yf :
							return getChain(1,value.getEntityId());
						default :
							return getChain(2,value.getEntityId());
					}
				case predicate :
					return getChain(((IFProPredicate)value).getArity(),value.getEntityId());
				default :
					throw new IllegalArgumentException("Predicate type ["+value.getEntityType()+"] can't be called directly to the fact/rule base");
			}
		}
	}

	@Override
	public Iterable<NameAndArity> content(final int arity, final long... ids) {
		final List<NameAndArity>	result = new ArrayList<NameAndArity>();
		final int					minArity = arity == -1 ? IFProPredicate.MIN_ARITY : arity, maxArity = arity == -1 ? IFProPredicate.MAX_ARITY-1 : arity;   

		for (int actualArity = minArity; actualArity <= maxArity; actualArity++) {
			if (predicates[actualArity] != null) {
				if (ids.length == 0) {
					for (Long item : predicates[actualArity].content()) {
						if (predicates[actualArity].get(item).start != null) {
							result.add(new ContentDescriptor(predicates[actualArity].get(item).predicateId,predicates[actualArity].get(item).start.getEntityType(),actualArity));
						}
					}
				}
				else {
					for (long item : ids) {
						if (predicates[actualArity].get(item) != null && predicates[actualArity].get(item).start != null) {
							result.add(new ContentDescriptor(predicates[actualArity].get(item).predicateId,predicates[actualArity].get(item).start.getEntityType(),actualArity));
						}
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public void serialize(final DataOutput target) throws IOException, NullPointerException {
		if (target == null) {
			throw new NullPointerException("Target stream can't be null!");
		}
		else {
			target.writeInt(SERIALIZATION_MAGIC);	// Write magic
			target.writeInt(predicates.length);		// Write arity size
			
			for (int arityIndex = 0; arityIndex < predicates.length; arityIndex++) {	// Write individual chains
				if (predicates[arityIndex] != null) {
					target.writeInt(predicates[arityIndex].size());			// Write amount of the predicates in this chain
					
					for (Long item : predicates[arityIndex].content()) {		// Write chain contents
						if (predicates[arityIndex].get(item).start != null) {
							IFProEntity	actual = predicates[arityIndex].get(item).start;
							int			count;
							
							for (count = 0; actual != null; count++, actual = actual.getParent()) {}
							target.writeInt(count);		// Amount of predicates in the chain
							
							if (count > 0) {
								actual = predicates[arityIndex].get(item).start;
								for (int index = 0; index < count; index++, actual = actual.getParent()) {
									FProUtil.serialize(target,actual);	// Serialize each predicate
								}
							}
						}
						else {
							target.writeInt(0);	// No data in this chain
						}
					}
				}
				else {
					target.writeInt(0);			// No data with this arity
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(final DataInput source) throws IOException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Target stream can't be null!");
		}
		else if (source.readInt() != SERIALIZATION_MAGIC) {
			throw new IllegalArgumentException("Illegal content (magic!)");
		}
		else {
			final int		arity = source.readInt();
					
			predicates = new QuickList[arity];
			for (int arities = 0; arities < arity; arities++) {
				final int	actualPredAmount = source.readInt();
				
				if (actualPredAmount > 0) {
					for (int predIndex = 0; predIndex < actualPredAmount; predIndex++) {
						final int	chainLen = source.readInt();
						
						for (int count = 0; count < chainLen; count++) {
							assertZ(FProUtil.deserialize(source));
						}
					}
				}
			}
		}		
	}	
	
	private void assertValue(final int arity, final boolean atTheBeginning, final IFProEntity value) {
		if (predicates[arity] == null) {
			predicates[arity] = new QuickList<ChainDescriptor>(ChainDescriptor.class);
		}
		if (!predicates[arity].contains(value.getEntityId())) {
			predicates[arity].insert(value.getEntityId(),new ChainDescriptor(value.getEntityId()));
		}
		final ChainDescriptor	key = predicates[arity].get(value.getEntityId()); 
		
		if (key.start == null) {
			key.start = key.end = value;
			value.setParent(null);
		}
		else {
			IFProEntity	current = key.start;
			
			while (current != null) {
				if (current == value) {
					throw new IllegalArgumentException("Attempt to insert value instance that was inserted earlier! Duplicate it before inserting by FProUtiosl.duplicate(...) method call");
				}
				else {
					current = current.getParent();
				}
			}
			
			if (atTheBeginning) {
				value.setParent(key.start);
				key.start = value;
			}
			else {
				key.end.setParent(value);
				key.end = value;
				value.setParent(null);
			}
		}
	}

	private void removeChain(final int arity, final long entityId) {
		if (predicates[arity] != null) {
			if (predicates[arity].contains(entityId)) {
				final ChainDescriptor	key = predicates[arity].get(entityId);
				IFProEntity				start = key.start, temp;
				
				while (start != null) {
					temp = start.getParent();
					FProUtil.removeEntity(repo, start);
					start = temp;
				}
				
				key.start = key.end = null;
			}
		}		
	}

	
	private boolean removeFromChain(final int arity, final long entityId, final IFProEntity template, final Change[] changes) {
		if (predicates[arity] != null) {
			if (predicates[arity].contains(entityId)) {
				final ChainDescriptor	key = predicates[arity].get(entityId);
				IFProEntity				start = key.start, temp;

				if (start != null) {
					if (FProUtil.unify(start,template,changes)) {
						FProUtil.unbind(changes[0]);
						temp = start.getParent();
						FProUtil.removeEntity(repo, start);
						if ((key.start = temp) == null) {
							key.end = null;
						}
						return true;
					}
					else {
						while (start.getParent() != null) {
							if (FProUtil.unify(start.getParent(),template,changes)) {
								FProUtil.unbind(changes[0]);
								temp = start.getParent().getParent();
								FProUtil.removeEntity(repo, start.getParent());
								if (temp == null) {
									key.end = start;
								}
								return true;
							}
							else {
								start = start.getParent();
							}
						}
						return false;
					}
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		}		
		else {
			return false;
		}
	}


	private Iterable<IFProEntity> getChain(final int arity, final long entityId) {
		if (arity < 0 || arity > IFProPredicate.MAX_ARITY) {
			throw new IllegalArgumentException("Predicate arity ["+arity+"] outside available. Need be in 0.."+IFProPredicate.MAX_ARITY);
		}
		else if (predicates[arity] != null) {
			if (predicates[arity].contains(entityId)) {
				final Iterator<IFProEntity>	iterator = new Iterator<IFProEntity>(){
															private IFProEntity		actual = predicates[arity].get(entityId).start;
										
															@Override public boolean hasNext() {return actual != null;}
										
															@Override
															public IFProEntity next() {	// Clone - to protect side effects of unification in the fact/rule repo (about variables)
																final IFProEntity	returned = FProUtil.hasAnyVariable(actual) ? FProUtil.cloneEntity(actual) : actual;
																
																actual = actual.getParent();
																return returned;
															}
														};
				return new Iterable<IFProEntity>(){@Override public Iterator<IFProEntity> iterator() {return iterator;}};
			}
			else {
				return new ArrayList<IFProEntity>();
			}
		}
		else {
			return new ArrayList<IFProEntity>();
		}
	}

	private static class ChainDescriptor implements Comparable<ChainDescriptor> {
		public long			predicateId;
		public IFProEntity	start;
		public IFProEntity	end;

		public ChainDescriptor(final long id) {
			this.predicateId = id;
		}
		
		@Override
		public int compareTo(final ChainDescriptor another) {
			return another.predicateId == this.predicateId ? 0 : another.predicateId > this.predicateId ? 1 : -1;
		}

		@Override public String toString() {return "ChainDescriptor [predicateId=" + predicateId + ", start=" + start + ", end=" + end + "]";}
	}
	
	private static class ContentDescriptor implements NameAndArity {
		private final long			id;
		private final EntityType	type;
		private final int			arity;

		public ContentDescriptor(final long id, final EntityType type, final int arity) {
			this.id = id;			this.type = type;
			this.arity = arity;
		}

		@Override public long getId() {return id;}
		@Override public EntityType getType() {return type;}
		@Override public int getArity() {return arity;}

		@Override public String toString() {return "ContentDescriptor [id=" + id + ", type=" + type + ", arity=" + arity + "]";}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + arity;
			result = prime * result + (int) (id ^ (id >>> 32));
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ContentDescriptor other = (ContentDescriptor) obj;
			if (arity != other.arity) return false;
			if (id != other.id) return false;
			if (type != other.type) return false;
			return true;
		}
	}
}
