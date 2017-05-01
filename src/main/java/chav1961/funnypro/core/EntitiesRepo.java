package chav1961.funnypro.core;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.exceptions.FProException;
import chav1961.funnypro.core.exceptions.FProParsingException;
import chav1961.funnypro.core.exceptions.FProPrintingException;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter.FProParserCallback;
import chav1961.funnypro.core.interfaces.IFProRepo;
import chav1961.funnypro.core.interfaces.IFProRepo.NameAndArity;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IGentlemanSet;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.chartarget.WriterCharTarget;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

class EntitiesRepo implements IFProEntitiesRepo, IGentlemanSet {
	private static final int					SERIALIZATION_MAGIC = 0x12123000;
	
	private final LoggerFacade					log;
	private final Properties					props;
	private final AndOrTree<SerializableString>	stringRepo; 
	private final AndOrTree<SerializableString>	termRepo;
	private final OperatorDefRepo				fill = new OperatorDefRepo(-Integer.MAX_VALUE,new OperatorDefEntity(0,OperatorType.xfx,-Integer.MAX_VALUE)); 
	
	private final FactRuleRepo					frRepo;
	private final ExternalPluginsRepo			epRepo;
	private final long							anonymousId;
	private final long							opId;
	private final long							externId;
	private final long							goalId;
	private final long							questionId;
	private final long[]						opTypes = new long[OperatorType.values().length];
	
	private OperatorDefRepo[]					operators = new OperatorDefRepo[16];
	private int[]								operatorPriorities = new int[0];
	private int									amount = 0; 

	public EntitiesRepo(final LoggerFacade log, final Properties prop) throws IOException {
		if (log == null) {
			throw new IllegalArgumentException("Log can't be null"); 
		}
		else if (prop == null) {
			throw new IllegalArgumentException("Properties can't be null"); 
		}
		else {
			this.log = log;			this.props = prop;
			
			Arrays.fill(this.operators,fill);
			this.stringRepo = new AndOrTree<SerializableString>(1); 
			this.termRepo = new AndOrTree<SerializableString>(2); 
			this.frRepo = new FactRuleRepo(log,props);
			this.epRepo = new ExternalPluginsRepo(log, props);
			this.epRepo.prepare(this);
			
			this.anonymousId = this.termRepo.placeName("_",null);
			this.opId = this.termRepo.placeName("op",null);
			this.externId = this.termRepo.placeName("extern",null);
			this.goalId = this.termRepo.placeName(":-",null);
			this.questionId = this.termRepo.placeName("?-",null);
			
			for (int index = 0; index < this.opTypes.length; index++) {
				this.opTypes[index] = this.termRepo.placeName(OperatorType.values()[index].toString(),null);
			}
		}
	}
	
	@Override
	public void close() throws Exception {
		for (long item : this.opTypes) {
			termRepo.removeName(item);
		}
		termRepo.removeName(externId);
		termRepo.removeName(opId);
		termRepo.removeName(anonymousId);
		termRepo.removeName(goalId);
		termRepo.removeName(questionId);
		pluginsRepo().close();
	}
	
	@Override public LoggerFacade getDebug() {return log;}
	@Override public Properties getParameters() {return props;}		
	
	@Override
	public void serialize(final OutputStream target) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target stream can't be null");
		}
		else {
			CommonUtil.writeInt(target,SERIALIZATION_MAGIC);	// Write magic
			CommonUtil.writeTree(target,stringRepo);			// Write string repo
			CommonUtil.writeTree(target,termRepo);				// Write term repo
			frRepo.serialize(target);							// Write fact/rule repo
			
			int		count = 0, unique = 0;
			for (int index = 0; index < operators.length; index++) {
				if (operators[index] != fill) {
					unique++;
					IFProOperator	root = operators[index].data;
					
					while(root != null) {
						count++;
						root = (IFProOperator)root.getParent();
					}
				}
			}
			
			CommonUtil.writeInt(target,operators.length);		// Write length of operator array
			CommonUtil.writeInt(target,unique);					// Write amount of unique ids
			CommonUtil.writeInt(target,count);					// Write amount of all operators
			
			for (int index = 0; index < operators.length; index++) {
				if (operators[index] != fill) {
					IFProOperator	actual = operators[index].data;
					
					while (actual != null) {						// Write every definition;
						CommonUtil.writeInt(target,actual.getPriority());		// Write priority
						CommonUtil.writeInt(target,actual.getType().ordinal());	// Write operator type
						CommonUtil.writeLong(target,actual.getEntityId());		// Write operator id
						
						actual = (IFProOperator)actual.getParent();
					}
				}
			}			
		}
	}

	@Override
	public void deserialize(final InputStream source) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Source stream can't be null"); 
		}
		else if (CommonUtil.readInt(source) != SERIALIZATION_MAGIC) {
			throw new IllegalArgumentException("Illegal content of the source. Magic !"); 
		}
		else {
			CommonUtil.readTree(source,stringRepo,SerializableString.class);
			CommonUtil.readTree(source,termRepo,SerializableString.class);
			frRepo.deserialize(source);
	
			final int	length2Read = CommonUtil.readInt(source);
			CommonUtil.readInt(source);
			final int	counts2Read = CommonUtil.readInt(source);
			
			this.operators = new OperatorDefRepo[length2Read];
			this.amount = 0;
			
			Arrays.fill(this.operators,fill);
			for (int index = 0; index < counts2Read; index++) {
				final int			prty = CommonUtil.readInt(source);
				final OperatorType	type = OperatorType.values()[CommonUtil.readInt(source)];
				final long			id = CommonUtil.readLong(source);
				
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
		else if (id == externId) {
			return Classification.extern;
		}
		else if (Arrays.binarySearch(operators, new OperatorDefRepo(id,null)) >= 0) {
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
		return null;
	}
	
	@Override
	public IFProOperator[] getOperatorDef(long id, int minPrty, int maxPrty, OperatorType... types) {
		if (minPrty < IFProOperator.MIN_PRTY || minPrty > IFProOperator.MAX_PRTY) {
			throw new IllegalArgumentException("Min priority ["+minPrty+"] out of bounds. Need be in "+IFProOperator.MIN_PRTY+".."+IFProOperator.MAX_PRTY);
		}
		else if (maxPrty < IFProOperator.MIN_PRTY || maxPrty > IFProOperator.MAX_PRTY) {
			throw new IllegalArgumentException("Max priority ["+maxPrty+"] out of bounds. Need be in "+IFProOperator.MIN_PRTY+".."+IFProOperator.MAX_PRTY);
		}
		else {
			final OperatorDefRepo	odr = new OperatorDefRepo(id,null); 
			final  int				found = Arrays.binarySearch(operators,odr);
			final int				min = Math.min(minPrty,maxPrty), max = Math.max(minPrty,maxPrty); 
			
			if (found >= 0) {
				IFProOperator		root = operators[found].data;
				int					count = 0;
				
				while (root != null) {
					if (root.getPriority() >= min && root.getPriority() <= max && (types.length == 0 || inList(root.getType(),types))) {
						count++;
					}
					root = (IFProOperator)root.getParent();
				}
				
				if (count > 0) {
					final IFProOperator[]	result = new IFProOperator[count];
					
					root = operators[found].data;
					count = 0;
					while (root != null) {
						if (root.getPriority() >= min && root.getPriority() <= max && (types.length == 0 || inList(root.getType(),types))) {
							result[count++] = root;
						}
						root = (IFProOperator)root.getParent();
					}
					if (count > 1) {
						Arrays.sort(result,new Comparator<IFProOperator>(){
							@Override
							public int compare(final IFProOperator o1, final IFProOperator o2) {
								return minPrty < maxPrty ? o1.getPriority() - o2.getPriority() : o2.getPriority() - o1.getPriority();
							}
						});
					}
					
					return result;
				}
				else {
					return new IFProOperator[0];
				}
			}
			else {
				return new IFProOperator[0];
			}
		}
	}

	@Override
	public void putOperatorDef(final IFProOperator op) {
		if (op == null) {
			throw new IllegalArgumentException(); 
		}
		else {
			final OperatorDefRepo	odr = new OperatorDefRepo(op.getEntityId(),op); 
			int						found = Arrays.binarySearch(operators,odr); 
					
			if (found < 0) {
				if (amount >= operators.length) {
					final OperatorDefRepo[]	newOperators = new OperatorDefRepo[2*operators.length];
					
					Arrays.fill(newOperators,fill);
					System.arraycopy(operators,0,newOperators,operators.length,operators.length);
					operators = newOperators;
					found = Arrays.binarySearch(operators,odr); 
				}
				if (-found > operators.length) {
					System.arraycopy(operators,1,operators,0,-2-found);
					operators[-2-found] = odr;
				}
				else if (found == -1) {
					System.arraycopy(operators,0,operators,1,operators.length-1);
					operators[0] = odr;
				}
				else {
					System.arraycopy(operators,1,operators,0,-2-found);
					operators[-2-found] = odr;
				}
				amount++;
			}
			else {
				IFProOperator		root = operators[found].data;
				while (root != null) {
					if (root.getPriority() == op.getPriority() && root.getType().equals(op.getType())) {
						throw new IllegalArgumentException("Attempt to put operator def: operator "+op+" already registered in the database!");
					}
					else {
						root = (IFProOperator)root.getParent();
					}
				}
				operators[found].data = (IFProOperator) op.setParent(operators[found].data);
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
		
		for (int index = 0; index < operators.length; index++) {
			if (operators[index] != fill) {
				IFProOperator	root = operators[index].data;
				while (root != null) {
					result.add(root);
					root = (IFProOperator) root.getParent();
				}
			}
		}
		return result;
	}
	
	@Override
	public void consult(final CharacterSource source) throws FProParsingException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null");
		}
		else {
			final ParserAndPrinter	pap = new ParserAndPrinter(getDebug(),getParameters(),this);
			int[]					count = new int[1], ops = new int[1];
			
			try{pap.parseEntities(source,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProParsingException, IOException {
											if (entity.getEntityId() == goalId && entity.getEntityType() == EntityType.operator && ((IFProOperator)entity).getType() == OperatorType.fx) {
												if (((IFProOperator)entity).getRight().getEntityType() == EntityType.operatordef) {
													putOperatorDef((IFProOperator)((IFProOperator)entity).getRight());
													ops[0]++;
												}
												else {
													getDebug().message(Severity.warning,"Consulted data contains goal. It will be ignored");
												}
											}
											else if (entity.getEntityId() == questionId && entity.getEntityType() == EntityType.operator && ((IFProOperator)entity).getType() == OperatorType.fx) {
												getDebug().message(Severity.warning,"Consulted data contains question. It will be ignored");
											}
											else {
												predicateRepo().assertZ(entity);
												count[0]++;
											}
											return true;
										}
									}
				);
			} catch (FProException | IOException | ContentException e) {
				e.printStackTrace();
				throw new FProParsingException(0,0,e.getMessage()); 
			} 
			getDebug().message(Severity.info,"%1$d entities and %2$d operators was consulted",count[0],ops[0]);
		}
	}

	@Override
	public int consult(final char[] source, int from) throws FProParsingException {
		if (source == null) {
			throw new IllegalArgumentException("Source can't be null");
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From location ["+from+"] outside the range 0.."+(source.length-1));
		}
		else {
			final ParserAndPrinter	pap = new ParserAndPrinter(getDebug(),getParameters(),this);
			int[]					count = new int[1], ops = new int[1];
			
			try{from = pap.parseEntities(source,from,new FProParserCallback(){
										@Override
										public boolean process(final IFProEntity entity, final List<IFProVariable> vars) throws FProParsingException, IOException {
											if (entity.getEntityId() == goalId && entity.getEntityType() == EntityType.operator && ((IFProOperator)entity).getType() == OperatorType.fx) {
												if (((IFProOperator)entity).getRight().getEntityType() == EntityType.operatordef) {
													putOperatorDef((IFProOperator)((IFProOperator)entity).getRight());
													ops[0]++;
												}
												else {
													getDebug().message(Severity.warning,"Consulted data contains goal. It will be ignored");
												}
											}
											else if (entity.getEntityId() == questionId && entity.getEntityType() == EntityType.operator && ((IFProOperator)entity).getType() == OperatorType.fx) {
												getDebug().message(Severity.warning,"Consulted data contains question. It will be ignored");
											}
											else {
												predicateRepo().assertZ(entity);
												count[0]++;
											}
											return true;
										}
									}
				);
			} catch (FProException | IOException e) {
				e.printStackTrace();
				throw new FProParsingException(0,0,e.getMessage()); 
			} 
			getDebug().message(Severity.info,"%1$d entities and %2$d operators was consulted",count[0],ops[0]);
			return from;
		}
	}

	@Override
	public void save(final CharacterTarget target) throws FProPrintingException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
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
						target.put('.');
						count++;
					}
				}
				getDebug().message(Severity.info,"%1$d entities was saved",count);
			} catch (FProException | IOException | PrintingException e) {
				throw new FProPrintingException(e.getMessage());
			}
		}
	}

	@Override
	public int save(final char[] target, final int from) throws FProPrintingException {
		if (target == null) {
			throw new IllegalArgumentException("Target can't be null");
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
				e.printStackTrace();
				throw new FProPrintingException(e.getMessage());
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
		return "EntitiesRepo [anonymousId=" + anonymousId + ", opId=" + opId + ", externId=" + externId + ", operators=" + Arrays.toString(operators) + "]";
	}

	private boolean inList(OperatorType type, OperatorType[] types) {
		for (OperatorType item : types) {
			if (item.equals(type)) {
				return true;
			}
		}
		return false;
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
