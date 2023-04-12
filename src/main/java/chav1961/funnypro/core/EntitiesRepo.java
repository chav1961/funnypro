package chav1961.funnypro.core;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo;
import chav1961.funnypro.core.interfaces.IFProModule;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorSort;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.funnypro.core.interfaces.IFProRepo.NameAndArity;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

public class EntitiesRepo implements IFProEntitiesRepo, IFProModule {
	private static final int						SERIALIZATION_MAGIC = 0x12123000;
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
	
	
	
	private final LoggerFacade					log;
	private final SubstitutableProperties		props;
	private final AndOrTree<SerializableString>	stringRepo; 
	private final AndOrTree<SerializableString>	termRepo;
	
	private final FactRuleRepo					frRepo;
	private final ExternalPluginsRepo			epRepo;
	private final long							anonymousId;
	private final long							opId;
	private final long							goalId;
	private final long							questionId;
	private final long[]						opTypes = new long[OperatorType.values().length];
	
	private LongIdMap<OperatorDefRepo>			operators = new LongIdMap<>(OperatorDefRepo.class);
	private Set<OperatorDefRepo>				operatorsSet = new HashSet<>(); 
	private int[]								operatorPriorities = new int[0];

	public EntitiesRepo(final LoggerFacade log, final SubstitutableProperties prop) throws IOException, NullPointerException {
		if (log == null) {
			throw new NullPointerException("Log can't be null"); 
		}
		else if (prop == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else {
			this.log = log;			this.props = prop;
			
			this.stringRepo = new AndOrTree<SerializableString>(1,16); 
			this.termRepo = new AndOrTree<SerializableString>(2,16);

			for (int index = 0; index < this.opTypes.length; index++) {
				this.opTypes[index] = this.termRepo.placeName((CharSequence)OperatorType.values()[index].toString(),null);
			}
			
			this.anonymousId = this.termRepo.placeName((CharSequence)"_",null);
			this.opId = this.termRepo.placeName((CharSequence)"op",null);
			this.goalId = this.termRepo.placeName((CharSequence)":-",null);
			this.questionId = this.termRepo.placeName((CharSequence)"?-",null);
			
			this.frRepo = new FactRuleRepo(log, props, stringRepo());
			this.epRepo = new ExternalPluginsRepo(log, props);
			this.epRepo.prepare(this);
		}
	}
	
	@Override
	public void close() throws RuntimeException {
		for (long item : this.opTypes) {
			termRepo.removeName(item);
		}
		termRepo.removeName(opId);
		termRepo.removeName(anonymousId);
		termRepo.removeName(goalId);
		termRepo.removeName(questionId);
		pluginsRepo().close();
	}
	
	@Override public LoggerFacade getDebug() {return log;}
	@Override public SubstitutableProperties getParameters() {return props;}		
	
	@Override
	public void serialize(final DataOutput target) throws IOException, NullPointerException {
		if (target == null) {
			throw new NullPointerException("Target stream can't be null");
		}
		else {
			target.writeInt(SERIALIZATION_MAGIC);				// Write magic
			CommonUtil.writeTree(target,stringRepo);			// Write string repo
			CommonUtil.writeTree(target,termRepo);				// Write term repo
			frRepo.serialize(target);							// Write fact/rule repo
			
			int		count = 0, unique = 0;
			for (OperatorDefRepo item : operatorsSet) {
				unique++;
				IFProOperator	root = item.data;
				
				while(root != null) {
					count++;
					root = (IFProOperator)root.getParent();
				}
			}
			
			target.writeInt(operatorsSet.size());				// Write length of operator array
			target.writeInt(unique);							// Write amount of unique ids
			target.writeInt(count);								// Write amount of all operators
			
			for (OperatorDefRepo item : operatorsSet) {
				IFProOperator	actual = item.data;
				
				while (actual != null) {						// Write every definition;
					target.writeInt(actual.getPriority());		// Write priority
					target.writeInt(actual.getOperatorType().ordinal());	// Write operator type
					target.writeLong(actual.getEntityId());		// Write operator id
					
					actual = (IFProOperator)actual.getParent();
				}
			}
		}
	}

	@Override
	public void deserialize(final DataInput source) throws IOException, NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source stream can't be null"); 
		}
		else if (source.readInt() != SERIALIZATION_MAGIC) {
			throw new IllegalArgumentException("Illegal content of the source. Magic !"); 
		}
		else {
			CommonUtil.readTree(source,stringRepo,SerializableString.class);
			CommonUtil.readTree(source,termRepo,SerializableString.class);
			frRepo.deserialize(source);
	
			source.readInt();
			source.readInt();
			final int	counts2Read = source.readInt();
			
			this.operators = new LongIdMap<>(OperatorDefRepo.class);
			this.operatorsSet.clear();
			
			for (int index = 0; index < counts2Read; index++) {
				final int			prty = source.readInt();
				final OperatorType	type = OperatorType.values()[source.readInt()];
				final long			id = source.readLong();
				
				putOperatorDef(new OperatorDefEntity(prty,type,id));
			}
		}
	}

	@Override public SyntaxTreeInterface<SerializableString> stringRepo() {return stringRepo;}
	@Override public SyntaxTreeInterface<SerializableString> termRepo() {return termRepo;}
	@Override public IFProRepo predicateRepo() {return frRepo;}
	@Override public IFProExternalPluginsRepo pluginsRepo() {return epRepo;}

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
	public OperatorType operatorType(final long id) {
		for (int index = 0; index < opTypes.length; index++) {
			if (opTypes[index] == id) {
				return OperatorType.values()[index];
			}
		}
		throw new IllegalArgumentException("Unknown id ["+id+"] for operator type"); 
	}
	
	@Override
	public IFProOperator[] getOperatorDef(final long id, final int minPrty, final int maxPrty, final OperatorSort sort) {
		if (minPrty < IFProOperator.MIN_PRTY || minPrty > IFProOperator.MAX_PRTY) {
			throw new IllegalArgumentException("Min priority ["+minPrty+"] out of bounds. Need be in "+IFProOperator.MIN_PRTY+".."+IFProOperator.MAX_PRTY);
		}
		else if (maxPrty < IFProOperator.MIN_PRTY || maxPrty > IFProOperator.MAX_PRTY) {
			throw new IllegalArgumentException("Max priority ["+maxPrty+"] out of bounds. Need be in "+IFProOperator.MIN_PRTY+".."+IFProOperator.MAX_PRTY);
		}
		else if (sort == null) {
			throw new NullPointerException("Operator sort can't be null");
		}
		else {
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
	}

	@Override
	public void putOperatorDef(final IFProOperator op) throws NullPointerException, IllegalArgumentException {
		if (op == null) {
			throw new NullPointerException("Operador def to add can't be null"); 
		} 
		else {
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
						throw new IllegalArgumentException("Attempt to put operator def: operator "+op+" already registered in the database!");
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
	}

	@Override public int[] getOperatorPriorities() {return operatorPriorities;}
	
	@Override
	public Iterable<IFProOperator> registeredOperators() {
		final List<IFProOperator>	result = new ArrayList<IFProOperator>();
		
		for (OperatorDefRepo item : operatorsSet) {
			IFProOperator	root = item.data;
			while (root != null) {
				result.add(root);
				root = (IFProOperator) root.getParent();
			}
		}
		return result;
	}
	
	@Override
	public void consult(final CharacterSource source) throws SyntaxException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source can't be null");
		}
		else {
			final ParserAndPrinter	pap = new ParserAndPrinter(getDebug(),getParameters(),this);
			int[]					count = new int[1], ops = new int[1];
			
			try{pap.parseEntities(source,(entity,vars)->
										   {if (entity.getEntityId() == goalId && entity.getEntityType() == EntityType.operator && ((IFProOperator)entity).getOperatorType() == OperatorType.fx) {
												if (((IFProOperator)entity).getRight().getEntityType() == EntityType.operatordef) {
													putOperatorDef((IFProOperator)((IFProOperator)entity).getRight());
													ops[0]++;
												}
												else {
													getDebug().message(Severity.warning,"Consulted data contains goal. Content will be ignored");
												}
											}
											else if (entity.getEntityId() == questionId && entity.getEntityType() == EntityType.operator && ((IFProOperator)entity).getOperatorType() == OperatorType.fx) {
												getDebug().message(Severity.warning,"Consulted data contains question. Content will be ignored");
											}
											else {
												predicateRepo().assertZ(entity);
												count[0]++;
											}
											return true;
										   }
										);
			} catch (IOException | ContentException e) {
				throw new SyntaxException(0,0,e.getMessage()); 
			} 
			getDebug().message(Severity.info,"%1$d entities and %2$d operators was consulted",count[0],ops[0]);
		}
	}

	@Override
	public int consult(final char[] source, int from) throws SyntaxException, NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source can't be null");
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From location ["+from+"] outside the range 0.."+(source.length-1));
		}
		else {
			final ParserAndPrinter	pap = new ParserAndPrinter(getDebug(),getParameters(),this);
			int[]					count = new int[1], ops = new int[1];
			
			try{from = pap.parseEntities(source,from,(entity,vars)->
											{if (entity.getEntityId() == goalId && entity.getEntityType() == EntityType.operator && ((IFProOperator)entity).getOperatorType() == OperatorType.fx) {
												if (((IFProOperator)entity).getRight().getEntityType() == EntityType.operatordef) {
													putOperatorDef((IFProOperator)((IFProOperator)entity).getRight());
													ops[0]++;
												}
												else {
													getDebug().message(Severity.warning,"Consulted data contains goal. Content will be ignored");
												}
											}
											else if (entity.getEntityId() == questionId && entity.getEntityType() == EntityType.operator && ((IFProOperator)entity).getOperatorType() == OperatorType.fx) {
												getDebug().message(Severity.warning,"Consulted data contains question. Content will be ignored");
											}
											else {
												predicateRepo().assertZ(entity);
												count[0]++;
											}
											return true;
											}
										);
			} catch (SyntaxException | IOException e) {
//				e.printStackTrace();
				throw new SyntaxException(0,0,e.getMessage()); 
			} 
			getDebug().message(Severity.info,"%1$d entities and %2$d operators was consulted",count[0],ops[0]);
			return from;
		}
	}

	@Override
	public void save(final CharacterTarget target) throws PrintingException, NullPointerException {
		if (target == null) {
			throw new NullPointerException("Target can't be null");
		}
		else {
			try{final IFProParserAndPrinter	pap = new ParserAndPrinter(getDebug(),getParameters(),this);
				IFProEntity[]				parm;
				int							count = 0;
				
				for (NameAndArity naa : predicateRepo().content(-1)) {
					parm = new IFProEntity[naa.getArity()];
					Arrays.fill(parm,new AnonymousEntity());
					
					for (IFProEntity item : predicateRepo().call(new PredicateEntity(naa.getId(),parm))) {
						pap.putEntity(item,target);
						target.put(" . ");
						count++;
					}
				}
				getDebug().message(Severity.info,"%1$d entities was saved",count);
			} catch (SyntaxException | IOException | PrintingException e) {
				throw new PrintingException(e.getMessage());
			}
		}
	}

	@Override
	public int save(final char[] target, final int from) throws PrintingException, NullPointerException, IllegalArgumentException {
		if (target == null) {
			throw new NullPointerException("Target can't be null");
		}
		else if (from < 0 || from >= target.length) {
			throw new IllegalArgumentException("From location ["+from+"] outside the range 0.."+(target.length-1));
		}
		else {
			String	result = "";
			
			try(final Writer	wr = new StringWriter()) {
				save(new WriterCharTarget(wr,false));
				wr.flush();
				result = wr.toString();			
			} catch (IOException e) {
//				e.printStackTrace();
				throw new PrintingException(e.getMessage());
			}
			
			if (result.length() < target.length - from) {
				result.getChars(0,result.length(),target,from);
				return from+result.length();
			}
			else {
				return -(from+result.length());
			}
		}
	}

	@Override
	public String toString() {
		return "EntitiesRepo [anonymousId=" + anonymousId + ", opId=" + opId + ", operators=" + operatorsSet + "]";
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
