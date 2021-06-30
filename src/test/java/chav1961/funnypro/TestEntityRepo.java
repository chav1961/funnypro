package chav1961.funnypro;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorSort;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IResolvable;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

public class TestEntityRepo implements IFProEntitiesRepo {
	private static final IFProOperator[]			EMPTY_OPERATOR_LIST = new IFProOperator[0];
	private static final Comparator<IFProOperator>	OP_COMPARATOR_MINMAX = new Comparator<IFProOperator>(){
														@Override
														public int compare(final IFProOperator o1, final IFProOperator o2) {
															return o1.getPriority() - o2.getPriority();
														}
													};
	private static final Comparator<IFProOperator>	OP_COMPARATOR_MAXMIN = new Comparator<IFProOperator>(){
														@Override
														public int compare(final IFProOperator o1, final IFProOperator o2) {
															return o2.getPriority() - o1.getPriority();
														}
													};
	
	private final SyntaxTreeInterface<IFProEntitiesRepo.SerializableString>	strings = new AndOrTree<>(1,16);
	private final SyntaxTreeInterface<IFProEntitiesRepo.SerializableString>	terms = new AndOrTree<>(2,16);
	private final long		anonymousId = terms.placeName("_", null);
	private final long		opId = terms.placeName("op", null);
	private final long		goalId = terms.placeName(":-",null);
	private final long		questionId = terms.placeName("?-",null);
	private final long		divizorId = terms.placeName(",",null);
	private final long[]						opTypes = new long[OperatorType.values().length];
	private LongIdMap<OperatorDefRepo>			operators = new LongIdMap<>(OperatorDefRepo.class);
	private Set<OperatorDefRepo>				operatorsSet = new HashSet<>(); 
	private int[]								operatorPriorities = new int[0];
	
	public TestEntityRepo() {
		for (int index = 0; index < this.opTypes.length; index++) {
			this.opTypes[index] = terms.placeName(OperatorType.values()[index].toString(),null);
		}
		
		putOperatorDef(new OperatorDef(goalId, OperatorType.fx, 1200));
		putOperatorDef(new OperatorDef(questionId, OperatorType.fx, 1200));
		putOperatorDef(new OperatorDef(divizorId, OperatorType.xfy, 1000));
	}
	
	@Override public void serialize(DataOutput target) throws IOException {}
	@Override public void deserialize(DataInput source) throws IOException {}
	@Override public void close() throws Exception {}
	@Override public SyntaxTreeInterface<SerializableString> stringRepo() {return strings;}
	@Override public SyntaxTreeInterface<SerializableString> termRepo() {return terms;}
	
	@Override
	public IFProRepo predicateRepo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFProExternalPluginsRepo pluginsRepo() {
		// TODO Auto-generated method stub
		return new IFProExternalPluginsRepo() {
			@Override
			public void prepare(IFProEntitiesRepo repo) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Iterable<PluginItem> seek(String pluginName, String pluginProducer, int[] pluginVersion) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Iterable<PluginItem> allPlugins() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <Global, Local> void registerResolver(IFProEntity template, List<IFProVariable> vars, IResolvable<Global, Local> resolver, Global global) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <Global> ExternalEntityDescriptor<Global> getResolver(IFProEntity template) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <Global, Local> void purgeResolver(IResolvable<Global, Local> resolver) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void close() throws RuntimeException {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public Classification classify(final long id) {
		if (id == anonymousId) {
			return Classification.anonymous;
		}
		else if (id == opId) {
			return Classification.op;
		}
		else if (operators.contains(id)) {
			return Classification.operator;
		}
		else {
			return Classification.term;
		}
	}

	@Override
	public OperatorType operatorType(long id) {
		for (int index = 0; index < opTypes.length; index++) {
			if (opTypes[index] == id) {
				return OperatorType.values()[index];
			}
		}
		throw new IllegalArgumentException("Unknown id ["+id+"] for operator type"); 
	}

	@Override
	public IFProOperator[] getOperatorDef(long id, int minPrty, int maxPrty, OperatorSort sort) {
		final OperatorDefRepo	found = operators.get(id);
		
		if (found != null) {
			final int			min = Math.min(minPrty,maxPrty), max = Math.max(minPrty,maxPrty); // Max prty can be less than min prty when seek postfix operators
			IFProOperator		root = found.data;
			int					count = 0, prty;
			
			while (root != null) {
				if ((prty = root.getPriority()) >= min && prty <= max && root.getOperatorType().getSort() == sort) {
					count++;
				}
				root = (IFProOperator)root.getParent();
			}
			
			if (count > 0) {
				final IFProOperator[]	result = new IFProOperator[count];
				
				root = found.data;
				count = 0;
				while (root != null) {
					if ((prty = root.getPriority()) >= min && prty <= max && root.getOperatorType().getSort() == sort) {
						result[count++] = root;
					}
					root = (IFProOperator)root.getParent();
				}
				if (count > 1) {
					Arrays.sort(result, minPrty < maxPrty ? OP_COMPARATOR_MINMAX : OP_COMPARATOR_MAXMIN);
				}
				
				return result;
			}
			else {
				return EMPTY_OPERATOR_LIST;
			}
		}
		else {
			return EMPTY_OPERATOR_LIST;
		}
	}

	@Override
	public void putOperatorDef(IFProOperator op) {
		final OperatorDefRepo	odr = new OperatorDefRepo(op.getEntityId(),op); 
		OperatorDefRepo			found = operators.get(op.getEntityId()); 
				
		if (found == null) {
			operators.put(op.getEntityId(),odr);
			operatorsSet.add(odr);
		}
		else {
			IFProOperator		root = found.data;
			while (root != null) {
				if (root.getPriority() == op.getPriority() && root.getOperatorType() == op.getOperatorType()) {
					throw new IllegalArgumentException("Attempt to put operator def: operator "+op+" ("+terms.getName(op.getEntityId())+") already registered in the database!");
				}
				else {
					root = (IFProOperator)root.getParent();
				}
			}
			found.data = (IFProOperator) op.setParent(found.data);
		}
		
		if (Arrays.binarySearch(getOperatorPriorities(),op.getPriority()) < 0) {
			final int[] 	newOperatorPriorities = new int[operatorPriorities.length+1];
			
			System.arraycopy(operatorPriorities, 0, newOperatorPriorities, 1, operatorPriorities.length);
			newOperatorPriorities[0] = op.getPriority();
			Arrays.sort(newOperatorPriorities);
			operatorPriorities = newOperatorPriorities;
		}
	}

	@Override
	public int[] getOperatorPriorities() {
		return operatorPriorities;
	}

	@Override
	public Iterable<IFProOperator> registeredOperators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public void consult(CharacterSource source) throws SyntaxException {}
	@Override public int consult(char[] source, int from) throws SyntaxException {return 0;}
	@Override public void save(CharacterTarget target) throws PrintingException {}
	@Override public int save(char[] target, int from) throws PrintingException {return 0;}


	public static class OperatorDef implements IFProOperator {
		private IFProEntity		parent = null, rule = null, left = null, right = null;
		private long			id;
		private OperatorType	type;
		private int				priority;
		
		public OperatorDef(final long id, final OperatorType type, final int priority) {
			this.id = id;
			this.type = type;
			this.priority = priority;
		}

		@Override public EntityType getEntityType() {return EntityType.operator;}
		@Override public long getEntityId() {return id;}
		@Override public IFProEntity setEntityId(long entityId) {this.id = id; return this;}
		@Override public IFProEntity getParent() {return parent;}
		@Override public IFProEntity setParent(IFProEntity entity) {this.parent = parent; return this;}
		@Override public IFProEntity getRule() {return rule;}
		@Override public IFProEntity setRule(IFProEntity rule) {this.rule = rule; return this;}
		@Override public OperatorType getOperatorType() {return type;}
		@Override public int getPriority() {return priority;}
		@Override public int getUnderlyingPriority() {return type == OperatorType.fx || type == OperatorType.xf ? priority - 1 : priority;}

		@Override
		public int getUnderlyingPriority(int prioritySide) {
			if (prioritySide == IFProOperator.LEFT) {
				return type == OperatorType.xfx || type == OperatorType.xfy ? priority - 1 : priority;
			}
			else {
				return type == OperatorType.xfx || type == OperatorType.yfx ? priority - 1 : priority;
			}
		}

		@Override public IFProEntity getLeft() {return left;}
		@Override public IFProEntity getRight() {return right;}
		@Override public IFProOperator setLeft(IFProEntity node) {this.left = node; return this;}
		@Override public IFProOperator setRight(IFProEntity node) {this.right = node; return this;}
	}

	private static class OperatorDefRepo implements Comparable<OperatorDefRepo>{
		public long id;
		public IFProOperator data;
		
		public OperatorDefRepo(long id, IFProOperator data) {
			this.id = id;			this.data = data;
		}

		@Override
		public int compareTo(final OperatorDefRepo other) {
			return other.id - this.id < 0 ? 1 : (other.id - this.id > 0 ? -1 : 0);
		}

		@Override public String toString() {return "OperatorDefRepo [id=" + id + ", data=" + data + "]";}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			OperatorDefRepo other = (OperatorDefRepo) obj;
			if (data == null) {
				if (other.data != null) return false;
			} else if (!data.equals(other.data)) return false;
			if (id != other.id) return false;
			return true;
		}
	}
	
}
