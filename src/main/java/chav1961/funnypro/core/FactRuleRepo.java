package chav1961.funnypro.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProQuickList;
import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.funnypro.core.interfaces.IGentlemanSet;
import chav1961.funnypro.core.interfaces.IStreamSerializable;
import chav1961.funnypro.core.CommonUtil;
import chav1961.purelib.basic.interfaces.LoggerFacade;

class FactRuleRepo implements IFProRepo, IStreamSerializable, IGentlemanSet {
	private static final int					SERIALIZATION_MAGIC = 0x12122080;
	
	private final LoggerFacade					log;
	private final Properties					props;
	private IFProQuickList<ChainDescriptor>[]	predicates = new QuickList[IFProPredicate.MAX_ARITY];

	public FactRuleRepo(final LoggerFacade log, final Properties prop) {
		if (log == null) {
			throw new IllegalArgumentException("Log can't be null"); 
		}
		else if (prop == null) {
			throw new IllegalArgumentException("Properties can't be null"); 
		}
		else {
			this.log = log;			this.props = prop;
		}
	}
	
	@Override public LoggerFacade getDebug() {return log;}
	@Override public Properties getParameters() {return props;}	
	@Override public boolean canUseInMultiThread() {return false;}
	@Override public boolean canUseForMultiThreadOnResolution() {return false;}

	@Override
	public void assertA(final IFProEntity value) {
		if (value == null) {
			throw new IllegalArgumentException("Value can't be null!");
		}
		else {
			switch (value.getEntityType()) {
				case operator : 
					switch (((IFProOperator)value).getType()) {
						case fx : case fy : case xf : case yf :
							assertValue(1,true,value);
							break;
						default :
							assertValue(2,true,value);
							break;
					}
				case predicate :
					assertValue(((IFProPredicate)value).getArity(),true,value);
					break;
				default :
					throw new IllegalArgumentException("Predicate type ["+value.getEntityType()+"] can't be stored directory to the fact/rule base");
			}
		}
	}

	@Override
	public void assertZ(final IFProEntity value) {
		if (value == null) {
			throw new IllegalArgumentException("Value can't be null!");
		}
		else {
			switch (value.getEntityType()) {
				case operator :
					switch (((IFProOperator)value).getType()) {
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
					throw new IllegalArgumentException("Predicate type ["+value.getEntityType()+"] can't be stored directory to the fact/rule base");
			}
		}
	}

	@Override
	public void retractAll(final IFProEntity value) {
		if (value == null) {
			throw new IllegalArgumentException("Value can't be null!");
		}
		else {
			switch (value.getEntityType()) {
				case operator :
					switch (((IFProOperator)value).getType()) {
						case fx : case fy : case xf : case yf :
							removeChain(1,value.getEntityId());
							break;
						default :
							removeChain(2,value.getEntityId());
							break;
					}
				case predicate :
					removeChain(((IFProPredicate)value).getArity(),value.getEntityId());
					break;
				default :
					throw new IllegalArgumentException("Predicate type ["+value.getEntityType()+"] can't be stored directory to the fact/rule base");
			}
		}
	}

	@Override
	public void retractAll(IFProEntity value, int module) {
		throw new UnsupportedOperationException("Not supported!"); 
	}

	@Override
	public void retractAll(final long id, final int arity) {
		removeChain(arity,id);
	}
	
	@Override
	public boolean retractFirst(final IFProEntity value) {
		if (value == null) {
			throw new IllegalArgumentException("Value can't be null!");
		}
		else {
			switch (value.getEntityType()) {
				case operator :
					switch (((IFProOperator)value).getType()) {
						case fx : case fy : case xf : case yf :
							return removeFromChain(1,value.getEntityId(),value);
						default :
							return removeFromChain(2,value.getEntityId(),value);
					}
				case predicate :
					return removeFromChain(((IFProPredicate)value).getArity(),value.getEntityId(),value);
				default :
					throw new IllegalArgumentException("Predicate type ["+value.getEntityType()+"] can't be stored directory to the fact/rule base");
			}
		}
	}
	
	@Override
	public Iterable<IFProEntity> call(IFProEntity value) {
		if (value == null) {
			throw new IllegalArgumentException("Value can't be null!");
		}
		else {
			switch (value.getEntityType()) {
				case operator :
					switch (((IFProOperator)value).getType()) {
						case fx : case fy : case xf : case yf :
							return getChain(1,value.getEntityId());
						default :
							return getChain(2,value.getEntityId());
					}
				case predicate :
					return getChain(((IFProPredicate)value).getArity(),value.getEntityId());
				default :
					throw new IllegalArgumentException("Predicate type ["+value.getEntityType()+"] can't be stored directory to the fact/rule base");
			}
		}
	}

	@Override
	public Iterable<IFProEntity> call(IFProEntity value, int module) {
		throw new UnsupportedOperationException("Not supported!"); 
	}

	@Override
	public Iterable<NameAndArity> content(final int arity, final long... ids) {
		final List<NameAndArity>	result = new ArrayList<NameAndArity>();
		final int					minArity = arity == -1 ? IFProPredicate.MIN_ARITY : arity, maxArity = arity == -1 ? IFProPredicate.MAX_ARITY-1 : arity;   

		for (int actualArity = minArity; actualArity <= maxArity; actualArity++) {
			if (predicates[actualArity] != null) {
				if (ids.length == 0) {
					for (Long item : predicates[actualArity].content()) {
						result.add(new ContentDescriptor(predicates[actualArity].get(item).predicateId,predicates[actualArity].get(item).start.getEntityType(),actualArity));
					}
				}
				else {
					for (long item : ids) {
						result.add(new ContentDescriptor(predicates[actualArity].get(item).predicateId,predicates[actualArity].get(item).start.getEntityType(),actualArity));
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public void serialize(final OutputStream target) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target stream can't be null!");
		}
		else {
			CommonUtil.writeInt(target,SERIALIZATION_MAGIC);	// Write magic
			CommonUtil.writeInt(target,predicates.length);		// Write arity size
			
			for (int arityIndex = 0; arityIndex < predicates.length; arityIndex++) {	// Write individual chains
				if (predicates[arityIndex] != null) {
					CommonUtil.writeInt(target,predicates[arityIndex].size());			// Write amount of the predicates in this chain
					
					for (Long item : predicates[arityIndex].content()) {		// Write chain contents
						if (predicates[arityIndex].get(item).start != null) {
							IFProEntity	actual = predicates[arityIndex].get(item).start;
							int			count;
							
							for (count = 0; actual != null; count++, actual = actual.getParent()) {}
							CommonUtil.writeInt(target,count);		// Amount of predicates in the chain
							
							if (count > 0) {
								actual = predicates[arityIndex].get(item).start;
								for (int index = 0; index < count; index++, actual = actual.getParent()) {
									FProUtil.serialize(target,actual);	// Serialize each predicate
								}
							}
						}
						else {
							CommonUtil.writeInt(target,0);	// No data in this chain
						}
					}
				}
				else {
					CommonUtil.writeInt(target,0);			// No data with this arity
				}
			}
		}
	}

	@Override
	public void deserialize(final InputStream source) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Target stream can't be null!");
		}
		else if (CommonUtil.readInt(source) != SERIALIZATION_MAGIC) {
			throw new IllegalArgumentException("Illegal content (magic!)");
		}
		else {
			final int		arity = CommonUtil.readInt(source);
					
			predicates = new QuickList[arity];
			for (int arities = 0; arities < arity; arities++) {
				final int	actualPredAmount = CommonUtil.readInt(source);
				
				if (actualPredAmount > 0) {
					for (int predIndex = 0; predIndex < actualPredAmount; predIndex++) {
						final int	chainLen = CommonUtil.readInt(source);
						
						for (int count = 0; count < chainLen; count++) {
							assertZ(FProUtil.deserialize(source));
						}
					}
				}
			}
		}		
	}	
	
	private void assertValue(final int arity, final boolean atTheBeginning, final IFProEntity value) {
		if (arity < 0 || arity > IFProPredicate.MAX_ARITY) {
			throw new IllegalArgumentException("Predicate arity ["+arity+"] outside available. Need be in 0.."+IFProPredicate.MAX_ARITY);
		}
		else {
			if (predicates[arity] == null) {
				predicates[arity] = new QuickList<ChainDescriptor>();
			}
			if (!predicates[arity].contains(value.getEntityId())) {
				predicates[arity].insert(value.getEntityId(),new ChainDescriptor(value.getEntityId()));
			}
			final ChainDescriptor	key = predicates[arity].get(value.getEntityId()); 
			
			if (key.start == null) {
				key.start = key.end = value;
			}
			else if (atTheBeginning) {
				value.setParent(key.start);
				key.start = value;
			}
			else {
				key.end.setParent(value);
				key.end = value;
			}
		}
	}

	private void removeChain(final int arity, final long entityId) {
		if (arity < 0 || arity > IFProPredicate.MAX_ARITY) {
			throw new IllegalArgumentException("Predicate arity ["+arity+"] outside available. Need be in 0.."+IFProPredicate.MAX_ARITY);
		}
		else if (predicates[arity] != null) {
			if (predicates[arity].contains(entityId)) {
				final ChainDescriptor	key = predicates[arity].get(entityId);
				IFProEntity				start = key.start, temp;
				
				while (start != null) {
					temp = start.getParent();
					FProUtil.removeEntity(start);
					start = temp;
				}
				
				key.start = null;		key.end = null;
			}
		}		
	}

	
	private boolean removeFromChain(final int arity, final long entityId, final IFProEntity template) {
		if (arity < 0 || arity > IFProPredicate.MAX_ARITY) {
			throw new IllegalArgumentException("Predicate arity ["+arity+"] outside available. Need be in 0.."+IFProPredicate.MAX_ARITY);
		}
		else if (predicates[arity] != null) {
			if (predicates[arity].contains(entityId)) {
				final ChainDescriptor	key = predicates[arity].get(entityId);
				IFProEntity				start = key.start, temp;
				Change[]				changes = new Change[1];

				if (start != null) {
					if (FProUtil.unify(start,template,changes)) {
						FProUtil.unbind(changes[0]);
						temp = start.getParent();
						FProUtil.removeEntity(start);
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
								FProUtil.removeEntity(start.getParent());
								if (temp == null) {
									key.end = start;
								}
								return true;
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
															public IFProEntity next() {
																final IFProEntity	returned = actual;
																
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

		public ChainDescriptor() {
			this(0);
		}
		
		public ChainDescriptor(final long id) {
			this.predicateId = id;
		}
		
		@Override
		public int compareTo(final ChainDescriptor another) {
			return another.predicateId == this.predicateId ? 0 : another.predicateId > this.predicateId ? 1 : -1;
		}

		@Override public String toString() {return "ChainDescriptor [predicateId=" + predicateId + ", start=" + start + ", end=" + end + "]";}
	}
	
	private static class Chain {
		public ChainDescriptor[] data;
		
		public Chain() {
			data = new ChainDescriptor[16];
			Arrays.fill(data,new ChainDescriptor());
		}
		
		public void expand() {
			final ChainDescriptor[]	newData = new ChainDescriptor[2*data.length];
			
			Arrays.fill(newData,0,data.length,new ChainDescriptor());
			System.arraycopy(data,0,newData,data.length,data.length);
			data = newData;
		}
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
