package chav1961.funnypro.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.FProUtil.ContentType;
import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorDefEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo.Classification;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalEntity;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorSort;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProRuledEntity;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IFProModule;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.ExtendedBitCharSet;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;

public class ParserAndPrinter implements IFProParserAndPrinter, IFProModule {
	private static final ExtendedBitCharSet	VALID_LETTERS = new ExtendedBitCharSet(); 
	private static final ExtendedBitCharSet	VALID_UPPER_LETTERS = new ExtendedBitCharSet(); 
	private static final ExtendedBitCharSet	VALID_LOWER_LETTERS = new ExtendedBitCharSet(); 

	private final long				colonId, tailId, goalId;
	private final LoggerFacade		log;
	private final Properties		props;
	private final IFProEntitiesRepo	repo;
	private final long				tempLong[] = new long[2], entityId[] = new long[1];

	private enum NameClassification {
		anonymous, term
	}

	static {
		VALID_LETTERS.add('_');
		VALID_LETTERS.addRange('a','z');
		VALID_LETTERS.addRange('A','Z');
		VALID_LETTERS.addRange('\u0410','\u044F');
		VALID_LETTERS.add('\u0401');
		VALID_LETTERS.add('\u0451');
		VALID_LETTERS.addRange('0','9');
		
		VALID_UPPER_LETTERS.addRange('A','Z');
		VALID_UPPER_LETTERS.addRange('\u0410','\u042F');
		VALID_UPPER_LETTERS.add('\u0401');

		VALID_LOWER_LETTERS.addRange('a','z');
		VALID_LOWER_LETTERS.addRange('\u0430','\u044F');
		VALID_LOWER_LETTERS.add('\u0451');
	}

	private final int[]		forIntResult = new int[2];
	
	public ParserAndPrinter(final LoggerFacade log, final Properties prop, final IFProEntitiesRepo repo) throws SyntaxException, NullPointerException {
		if (log == null) {
			throw new NullPointerException("Log can't be null"); 
		}
		else if (prop == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else if (repo == null) {
			throw new NullPointerException("Entities repo can't bw null"); 
		}
		else {
			this.log = log;			this.props = prop;
			this.repo = repo;		
			this.colonId = repo.termRepo().seekName(",");
			this.tailId = repo.termRepo().seekName("|");
			this.goalId = repo.termRepo().seekName(":-");
		}
	}
	
	@Override public LoggerFacade getDebug() {return log;}
	@Override public Properties getParameters() {return props;}	

	public IFProEntitiesRepo getRepo() {
		return repo;
	}

	@Override
	public void parseEntities(final CharacterSource source, final FProParserCallback callback) throws SyntaxException, IOException, ContentException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Source reader can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else {
			final GrowableCharArray<GrowableCharArray<?>>	gca = new GrowableCharArray<>(false);
			char						symbol;
			
			while ((symbol = source.next()) != CharacterSource.EOF) {
				gca.append(symbol);
			}
			parseEntities(gca.extract(),0,callback);
			gca.clear();
		}
	}

	@Override
	public int parseEntities(final char[] source, int from, final FProParserCallback callback) throws SyntaxException, IOException, NullPointerException, IllegalArgumentException {
		if (source == null) {
			throw new NullPointerException("Source array can't be null");
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(source.length-1));
		}
		else if (callback == null) {
			throw new NullPointerException("Parser callback can't be null");
		}
		else {
			final List<IFProVariable>	vars = new ArrayList<>();
			
			try{final IFProEntity[]		result = new IFProEntity[1];
				
				while (from < source.length) {
					try(final VarRepo	varRepo = new VarRepo(vars)) {
						from = parse(source,from,repo.getOperatorPriorities(),IFProOperator.MAX_PRTY,varRepo,result);
					}
					if (result[0] != null) {
						if (!callback.process(result[0],vars)) {
							break;
						}
						else {
							vars.clear();
						}
					}
					else {
						break;
					}
				}
				return from;
			} catch (Exception e) {
				if (e instanceof SyntaxException) {
					throw (SyntaxException)e;
				}
				else {
					e.printStackTrace();
					throw new SyntaxException(0,0,e.getMessage(),e);
				}
			}
		}
	}
	
	@Override
	public void putEntity(final IFProEntity entity, final CharacterTarget target) throws IOException, PrintingException , NullPointerException{
		if (entity == null) {
			throw new NullPointerException("Entity to put can't be null!"); 
		}
		else if (target == null) {
			throw new NullPointerException("Target stream can't be null!"); 
		}
		else {
			switch (entity.getEntityType()) {
				case string				:
					target.put('\"').put(getRepo().stringRepo().getName(entity.getEntityId())).put('\"');
					break;
				case integer			:
					target.put(String.valueOf(entity.getEntityId()));
					break;
				case real				:
					target.put(String.valueOf(Double.longBitsToDouble(entity.getEntityId())));
					break;
				case anonymous			:
					target.put('_');
					break;
				case variable			:
					target.put(getRepo().termRepo().getName(entity.getEntityId()));
					break;
				case list				:
					if (((IFProList)entity).getChild() == null && ((IFProList)entity).getTail() == null) {
						target.put("[]");
					}
					else {
						char			start = '[';
						IFProEntity		actual = entity;
						
						while (actual instanceof IFProList) {
							target.put(start);		start = ',';
							putEntity(((IFProList)actual).getChild(),target);
							actual = ((IFProList)actual).getTail();
						}
						
						if (actual != null) {
							target.put('|');	putEntity(actual,target);
						}
						target.put("]");
					}
					break;
				case operator			:
					final String	opName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
					
					switch (((IFProOperator)entity).getOperatorType().getSort()) {
						case prefix		:
							printBracket(target,(IFProOperator)entity,((IFProOperator)entity).getLeft());
							target.put(opName);
							break;
						case postfix	:
							target.put(opName);
							printBracket(target,(IFProOperator)entity,((IFProOperator)entity).getRight());
							break;
						case infix		:
							printBracket(target,(IFProOperator)entity,((IFProOperator)entity).getLeft());
							target.put(opName);
							printBracket(target,(IFProOperator)entity,((IFProOperator)entity).getRight());
							break;
					}
					if (((IFProRuledEntity)entity).getRule() != null) {
						target.put(":-");
						putEntity(((IFProRuledEntity)entity).getRule(),target);
					}
					break;
				case predicate			:
					target.put(getRepo().termRepo().getName(entity.getEntityId()));
					if (((IFProPredicate)entity).getArity() > 0) {
						char	prefix = '(';
						
						for (int index = 0; index < ((IFProPredicate)entity).getArity(); index++) {
							target.put(prefix);	prefix = ',';
							putEntity(((IFProPredicate)entity).getParameters()[index],target);
						}
						target.put(')');
					}
					if (((IFProRuledEntity)entity).getRule() != null) {
						target.put(":-");
						putEntity(((IFProRuledEntity)entity).getRule(),target);
					}
					break;
				case operatordef		:
					target.put(String.format("op(%1$d,%2$s,%3$s)",((IFProOperator)entity).getPriority(),((IFProOperator)entity).getOperatorType(),repo.termRepo().getName(entity.getEntityId())));
					break;
				default :
					throw new UnsupportedOperationException("Unknown type ti upload: "+entity.getEntityType());
			}
		}
	}
	
	@Override
	public int putEntity(final IFProEntity entity, final char[] target, int from) throws IOException, PrintingException {
		final int	result = internalPutEntity(entity,target,from);
		
		return result > target.length ? -result : result;
	}
		
	private int parse(final char[] source, int from, final int[] priorities, final int maxPrty, final VarRepo vars, final IFProEntity[] result) throws ContentException, IOException, SyntaxException {
		final IFProEntity[]	top = new IFProEntity[priorities.length+1];
		final int			maxLen = source.length;
		boolean				prefixNow = true, found;
		int					actualMin = IFProOperator.MIN_PRTY, actualMax = maxPrty;
		
		while (from < maxLen && source[from] <= ' ') from++;
		
loop:	while (from < maxLen && source[from] != '.') {
			while (from < maxLen && source[from] <= ' ') from++;
			
			switch (source[from]) {
				case '%'	:
					while (from < maxLen && source[from] != '\n') {
						from++;
					}
				case '\n'	:
					continue loop;
				case '\r'	:
					from++;
					continue loop;
				case '['	:
					from++;
					if (!prefixNow) {
						throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
					}
					else {
						while (from < maxLen && source[from] <= ' ') from++;
						
						if (from < maxLen && source[from] == ']') {
							from++;
							top[0] = new ListEntity(null,null);
							actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
							prefixNow = false;
						}
						else {
							from = parse(source,from,priorities,1151,vars,result);
							
							if (from < maxLen && source[from] == ']') {
								from++;
								top[0] = convert2List(source,from,result[0]);
								actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
								prefixNow = false;
							}
							else {
								throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Close bracket ']' missing!"); 
							}
						}
					}
					break; 
				case '('	:
					from++;
					if (!prefixNow) {
						throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
					}
					else {
						from = parse(source,from,priorities,1101,vars,result);
						
						while (from < maxLen && source[from] <= ' ') from++;
						if (from < maxLen && source[from] == ')') {
							from++;
							top[0] = result[0];
							actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
							prefixNow = false;
						}
						else {
							throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Close bracket ')' missing!"); 
						}
					}
					break;
				case ')'	:
				case ']'	:
					break loop;
				case '\"'	:
					final int	startConst = from++;
					
					while (from < maxLen && source[from] != '\"' && source[from] != '\n') {
						from++;
					}
					
					if (from < maxLen && source[from] == '\"') {
						final long	stringId = getRepo().stringRepo().placeName(source,startConst+1,from,null);
						
						if (!prefixNow) {
							throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
						}
						else {
							top[0] = new StringEntity(stringId);
							actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
							prefixNow = false;
							from++;
						}
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Unclosed quoted string detected");
					}					
					break;
				case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
					
					from = CharUtils.parseNumber(source,from,tempLong,CharUtils.PREF_ANY,false);
					if (!prefixNow) {
						throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
					}
					else {
						switch ((int)tempLong[1]) {
							case CharUtils.PREF_INT 	: top[0] = new IntegerEntity(tempLong[0]); break;								
							case CharUtils.PREF_LONG 	: top[0] = new IntegerEntity(tempLong[0]); break;
							case CharUtils.PREF_FLOAT 	: top[0] = new RealEntity(Float.intBitsToFloat((int)tempLong[0])); break;
							case CharUtils.PREF_DOUBLE	: top[0] = new RealEntity(Double.longBitsToDouble(tempLong[0])); break;
						}
						actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
						prefixNow = false;
					}
					break;
				case '!' :	// 'cut' predicate
					from++;
					if (!prefixNow) {
						throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
					}
					else {
						top[0] = new PredicateEntity(repo.termRepo().placeName("!",null));
						actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
						prefixNow = false;
					}
					break;
				default :
					if (VALID_UPPER_LETTERS.contains(source[from])) {
						final int	startVar = from, endVar = from = skipName(source,from);
						final long	varId = getRepo().termRepo().placeName(source,startVar,endVar,null);
						final IFProVariable		var = new VariableEntity(varId); 
						
						if (!prefixNow) {
							throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
						}
						else {
							top[0] = var;
							vars.storeVariable(var);
							actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
							prefixNow = false;
						}
					}
					else if (VALID_LOWER_LETTERS.contains(source[from]) || source[from] == '_') {
						final int	startName = from, endName = from = skipName(source,from);
						final long	nameId = getRepo().termRepo().placeName(source,startName,endName,null);
						
						switch (classifyName(source,startName,endName)) {
							case anonymous	:
								if (!prefixNow) {
									throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
								}
								else {
									top[0] = new AnonymousEntity();
									prefixNow = false;
								}
								break;
							case term		:
								switch (getRepo().classify(nameId)) {
									case operator 	:
										found = false;
										
										if (prefixNow) {
											for (IFProOperator item : getRepo().getOperatorDef(nameId // See prefix operators with nearest priorities
																						,actualMax
																						,actualMin
																						,OperatorSort.postfix)) {
												final int 	location = Arrays.binarySearch(priorities,item.getPriority()) + 1;
												
												if (location > 0) {
													final IFProOperator	op = new OperatorEntity(item);
													
													if (top[location] != null) {
														op.setParent(top[location]);
														((IFProOperator)top[location]).setRight(op);
														top[location] = op; 
													}
													else {
														top[location] = op;
													}
													actualMax = item.getUnderlyingPriority();
													found = true;		break;
												}
											}
										}
										else {
											for (IFProOperator item : getRepo().getOperatorDef(nameId // See postfix operators with nearest priorities
																						,actualMin
																						,actualMax
																						,OperatorSort.prefix)) {
												final int 	location = Arrays.binarySearch(priorities,item.getPriority()) + 1;
												
												if (location > 0) {
													final IFProOperator	op = new OperatorEntity(item);
													
													top[location] = op.setLeft(collapse(top,location).setParent(op));
													actualMin = item.getPriority() + (item.getOperatorType() == OperatorType.xf ? 1 : 0);
													found = true;		
													break;
												}
											}
											if (!found) {
												for (IFProOperator item : getRepo().getOperatorDef(nameId // See infix operators with nearest priorities
																							,actualMin
																							,actualMax
																							,OperatorSort.infix)) {
													final int 	location = Arrays.binarySearch(priorities,item.getPriority()) + 1;
													
													if (location > 0) {
														final IFProOperator	op = new OperatorEntity(item);
														
														top[location] = op.setLeft(collapse(top,location).setParent(op));
														actualMax = item.getUnderlyingPriority(IFProOperator.RIGHT);
														actualMin = IFProOperator.MIN_PRTY;
														prefixNow = true;
														found = true;
														break;
													}
												}
											}
										}
										if (!found) {
											throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Illegal nesting!"); 
										}
										break;
									case term 		:
										if (!prefixNow) {
											throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
										}
										else {
											top[0] = new PredicateEntity(nameId);
											
											while (from < maxLen && source[from] <= ' ') {
												from++;
											}
											if (from < maxLen && source[from] == '(') {
												from++;
												while (from < maxLen && source[from] <= ' ') {
													from++;
												}
												if (from < maxLen && source[from] == ')') { 
													from++;
												}
												else {
													from = parse(source,from,priorities,1101,vars,result);
													
													while (from < maxLen && source[from] <= ' ') {
														from++;
													}
													if (from < maxLen && source[from] == ')') {
														from++;
														((IFProPredicate)top[0]).setParameters(andChain2Array(result[0],top[0],colonId));
													}
													else {
														throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Close bracket ')' missing!"); 
													}												
												}
											}
											actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
											prefixNow = false;
										}
										break;
									case op		:
										if (!prefixNow) {
											throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
										}
										else {
											from = parseOp(source,from,result);
											top[0] = result[0];
											actualMin = IFProOperator.MIN_PRTY+1;		actualMax = maxPrty; 
											prefixNow = false;
										}
										break;
									default :
										throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Unsupported classification ["+getRepo().classify(nameId)+"] for the term"); 
								}
								break;
							default:
								throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Unsupported classification ["+classifyName(source,startName,endName)+"] for the term"); 
						}
					}
					else {
						from = extractEntityId(source,from,entityId);
						found = false;
						
						if (repo.classify(entityId[0]) != Classification.operator) {
							throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Operator awaited!"); 
						}
						
						if (prefixNow) {
							for (IFProOperator item : getRepo().getOperatorDef(entityId[0] // See prefix operators with nearest priorities
																		,actualMax
																		,actualMin
																		,OperatorSort.postfix)) {
								final int 	location = Arrays.binarySearch(priorities,item.getPriority()) + 1;
								
								if (location > 0) {
									final IFProOperator	op = new OperatorEntity(item);
									
									if (top[location] != null) {
										op.setParent(top[location]);
										((IFProOperator)top[location]).setRight(op);
										top[location] = op; 
									}
									else {
										top[location] = op;
									}
									actualMax = item.getUnderlyingPriority();
									found = true;		break;
								}
							}
						}
						else {
							for (IFProOperator item : getRepo().getOperatorDef(entityId[0] // See postfix operators with nearest priorities
																		,actualMin
																		,actualMax
																		,OperatorSort.prefix)) {
								final int 	location = Arrays.binarySearch(priorities,item.getPriority()) + 1;
								
								if (location > 0) {
									final IFProOperator	op = new OperatorEntity(item);
									
									top[location] = op.setLeft(collapse(top,location).setParent(op));
									actualMin = item.getPriority() + (item.getOperatorType() == OperatorType.xf ? 1 : 0);
									found = true;		
									break;
								}
							}
							if (!found) {
								for (IFProOperator item : getRepo().getOperatorDef(entityId[0] // See infix operators with nearest priorities
																			,actualMin
																			,actualMax
																			,OperatorSort.infix)) {
									final int 	location = Arrays.binarySearch(priorities,item.getPriority()) + 1;
									
									if (location > 0) {
										final IFProOperator	op = new OperatorEntity(item);
										
										if (top[location] == null) {
											top[location] = op.setLeft(collapse(top,location).setParent(op));
										}
										else if (((IFProOperator)top[location]).getOperatorType() == OperatorType.fy || ((IFProOperator)top[location]).getOperatorType() == OperatorType.yf || ((IFProOperator)top[location]).getOperatorType() == OperatorType.xfy) {
											((IFProOperator)top[location]).setRight(op.setLeft(collapse(top,location-1).setParent(top[location])));
											top[location] = op.setParent(top[location]);
										}
										else if (op.getOperatorType() == OperatorType.yfx) {
											top[location] = op.setLeft(collapse(top,location).setParent(op));
										}
										else {
											throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Illegal nesting for operator ["+repo.termRepo().getName(entityId[0])+"]!"); 
										}
										actualMax = item.getUnderlyingPriority(IFProOperator.RIGHT);
										actualMin = IFProOperator.MIN_PRTY;
										prefixNow = true;
										found = true;
										break;
									}
								}
							}
						}
						if (!found) {
							throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Illegal nesting for operator ["+repo.termRepo().getName(entityId[0])+"]!"); 
						}
						break;
					}
			}
			while (from < maxLen && source[from] <= ' ') from++;
		}
		if (from < maxLen && source[from] == '.') {
			from++;
		}
		result[0] = collapse(top,top.length-1);
		if (result[0] != null && result[0].getEntityType() == EntityType.operator
			&& ((IFProOperator)result[0]).getOperatorType() == OperatorType.xfx
			&& ((IFProOperator)result[0]).getEntityId() == goalId 
			&& ((IFProOperator)result[0]).getLeft().getEntityType() == EntityType.predicate) {
			result[0] = convert2Ruled(result[0]);
		}
		return from;
	}

	private IFProEntity collapse(final IFProEntity[] top, final int location) {
		IFProEntity		result = null;
		
		for (int index = 0; index <= location; index++) {
			if (top[index] != null) {
				if (result  == null) {
					result = top[index]; 
				}
				else {
					result.setParent(top[index]);
					switch (((IFProOperator)top[index]).getOperatorType().getSort()) {
						case postfix : case infix :
							((IFProOperator)top[index]).setRight(result);
							result = top[index];
							break;
						case prefix :
							((IFProOperator)top[index]).setLeft(result);
							result = top[index];
							break;
					}
				}
				while (result.getParent() != null) {
					result = result.getParent();
				}
				top[index] = null;
			}
		}
		return result;
	}

	private IFProEntity convert2List(final char[] source, final int from, final IFProEntity root) throws SyntaxException {
		IFProList		actual;		// Note: list builds from tail to head!
		
		if (root.getEntityId() == tailId && (root instanceof IFProOperator)) {	// Tail '|' priority is greater than ','  
			if (((IFProOperator)root).getRight() != null && !FProUtil.isEntityA(((IFProOperator)root).getRight(),ContentType.NonVar)) {
				actual = new ListEntity(null,((IFProOperator)root).getRight());
				actual.getTail().setParent(actual);
				
				final IFProEntity[]		args = andChain2Array(((IFProOperator)root).getLeft(),actual,colonId); 
				
				for (int index = args.length-1; index > 0; index--){
					actual.setChild(args[index]);
					args[index].setParent(actual);
					actual = new ListEntity(null,actual);
					actual.getTail().setParent(actual);
				}
				actual.setChild(args[0]);
				args[0].setParent(actual);
				
				return actual;
			}
			else {
				throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Tail of the list can be variable or anonymous only!"); 
			}
		}
		else if (root.getEntityId() == colonId && (root instanceof IFProOperator)) { // List without tail
			actual = new ListEntity(null,null);
			
			final IFProEntity[]		args = andChain2Array(root,actual,colonId); 
			for (int index = args.length-1; index > 0; index--){
				actual.setChild(args[index]);
				args[index].setParent(actual);
				actual = new ListEntity(null,actual);
				actual.getTail().setParent(actual);
			}
			actual.setChild(args[0]);
			args[0].setParent(actual);
			
			return actual;
		}
		else {	// List has one element only 
			actual = new ListEntity(root,null);
			
			root.setParent(actual);
			return actual;
		}
	}

	private static IFProEntity[] andChain2Array(final IFProEntity root, final IFProEntity parent, final long colonId) {
		int				count = 0;
		IFProEntity		actual = root;
		
		while (actual.getEntityType() == EntityType.operator && actual.getEntityId() == colonId) {
			actual = ((IFProOperator)actual).getRight();
			count++;
		}
		
		final IFProEntity[]	data = new IFProEntity[count+1];
		
		actual = root;
		for (int index = 0; index < count; index++) {
			(data[index] = ((IFProOperator)actual).getLeft()).setParent(parent);
			actual = ((IFProOperator)actual).getRight();
		}
		data[count] = actual;
		data[count].setParent(parent);
		
		return data;
	}

	private int extractEntityId(final char[] source, int from, final long[] result) throws SyntaxException {
		final int	maxLength = source.length; 
		
		if (from < maxLength && source[from] == '\'') {
			int		startName = from++;
			
			while (from < maxLength && source[from] != '\'') {
				from++; 
			}
			if (from < maxLength) {
				from++;
				result[0] = getRepo().termRepo().placeName(source,startName,from-startName-1,null);
				return from;
			}
			else {
				throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing quote (\')!"); 
			}
		}
		else {
			final int	startName = from, endName;
			final long	maxLex = getRepo().termRepo().seekName(source,startName,source.length);
						
			if (maxLex == -startName - 1) {
				throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Unknown term/operator was detected!"); 
			}
			else {
				endName = (int) - maxLex - 1;
				result[0] = getRepo().termRepo().seekName(source,startName,endName);
				return endName;
			}
		}
	}
	
	private int parseOp(final char[] source, int from, final IFProEntity[] result) throws SyntaxException, SyntaxException {
		final int			maxLen = source.length, prty;
		final OperatorType	type;
		final long			operatorId;
		
		from = CharUtils.skipBlank(source,from,false);
		
		if (from < maxLen && source[from] == '(') {
			from = CharUtils.parseInt(source,from+1,forIntResult,true);
			prty = forIntResult[0];
			from = CharUtils.skipBlank(source,from,false);
			
			if (prty < IFProOperator.MIN_PRTY || prty > IFProOperator.MAX_PRTY) {
				throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Operator priority out of range "+IFProOperator.MIN_PRTY+".."+IFProOperator.MAX_PRTY+"!");
			}
			else if (from < maxLen && source[from] == ',') {
				from = CharUtils.skipBlank(source,from+1,false);
				
				if (from < maxLen && Character.isJavaIdentifierStart(source[from])) {
					from = CharUtils.parseName(source,from,forIntResult);
				}
				else {
					throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing xf, fx, yf, fy, xfx, yfx or xfy !");
				}
				
				if (forIntResult[0] == forIntResult[1]) {
					throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing xf, fx, yf, fy, xfx, yfx or xfy !");
				}
				else {
					type = getRepo().operatorType(getRepo().termRepo().placeName(source,forIntResult[0],forIntResult[1]+1,null));
					from = CharUtils.skipBlank(source,from,false);
					
					if (from < maxLen && source[from] == ',') {
						from = CharUtils.skipBlank(source,from+1,false);
						
						if (from < maxLen) {
							if (source[from] == '\'') {
								from = CharUtils.parseUnescapedString(source,from+1,'\'',true,forIntResult);
								if (from < maxLen && source[from-1] == '\'') {
									if (forIntResult[0] > forIntResult[1]) {
										throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Empty string can't be used for operator mnemonics!");
									}
									else {
										operatorId = getRepo().termRepo().placeName(source,forIntResult[0]-1,forIntResult[1]+2,null);
									}
								}
								else {
									throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing '\'' !");
								}
							}
							else if (Character.isJavaIdentifierStart(source[from])) {
								from = CharUtils.parseName(source,from,forIntResult);
								if (forIntResult[0] == forIntResult[1]) {
									throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing operator mnemonics !");
								}
								else {
									operatorId = getRepo().termRepo().placeName(source,forIntResult[0],forIntResult[1]+1,null);
								}
							}
							else {
								throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing operator mnemonics !");
							}
							from = CharUtils.skipBlank(source,from,false);
							
							if (from < maxLen && source[from] == ')') {
								result[0] = new OperatorDefEntity(prty,type,operatorId);
								from++;
							}
							else {
								throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing ')' !");
							}
						}
						else {
							throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing operator name!");
						}
					}
					else {
						throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing ',' !");
					}
				}
			}
			else {
				throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing ',' !");
			}
		}
		else {
			throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Missing '(' !");
		}
		return from;
	}	

	private static int skipName(final char[] source, final int from) {
		final ExtendedBitCharSet	letters = VALID_LETTERS; 
		
		for (int index = from, maxIndex = source.length; index < maxIndex; index++) {
			if (!letters.contains(source[index])) {
				return index;
			}
		}
		return source.length;
	}

	private NameClassification classifyName(final char[] source, final int startName, final int endName) {
		return endName - startName == 1 && source[startName] == '_' ? NameClassification.anonymous : NameClassification.term;
	}
	
	private String blankedName(final String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		else {
			if (!Character.isJavaIdentifierStart(name.charAt(0))) {
				return name;
			}
			else {
				for (char item : name.toCharArray()) {
					if (!Character.isJavaIdentifierPart(item)) {
						return name;
					}
				}
				return ' '+name+' ';
			}
		}
	}

	private boolean needBracket(final IFProOperator root, final IFProEntity child) {
		if (child instanceof IFProOperator) {
			return ((IFProOperator)child).getPriority() > root.getPriority();
		}
		else {
			return false;
		}
	}

	private void printBracket(final CharacterTarget target, final IFProOperator root, final IFProEntity child) throws PrintingException, IOException {
		if (needBracket(root,child)) {
			target.put('(');
			putEntity(child,target);
			target.put(')');
		}
		else {
			putEntity(child,target);
		}
	}

	private int printBracket(final char[] target, int from, final IFProOperator root, final IFProEntity child) throws PrintingException, IOException {
		if (needBracket(root,child)) {
			if (from < target.length) {
				target[from] = '(';
			}
			from = putEntity(child,target,from+1);
			if (from < target.length) {
				target[from] = ')';
			}
			from++;
		}
		else {
			from = putEntity(child,target,from);
		}
		return from;
	}
	
	private int internalPutEntity(final IFProEntity entity, final char[] target, int from) throws IOException, PrintingException, NullPointerException {
		if (entity == null) {
			throw new NullPointerException("Entity to put can't be null!"); 
		}
		else if (target == null) {
			throw new NullPointerException("Target array can't be null!"); 
		}
		else {
			int		targetEnd = target.length;
			
			switch (entity.getEntityType()) {
				case string				:
					final int	stringLength = getRepo().stringRepo().getNameLength(entity.getEntityId());
					
					if (from + stringLength + 2 < targetEnd) {
						target[from] = '\"';
						getRepo().stringRepo().getName(entity.getEntityId(),target,from+1);
						target[from+stringLength+1] = '\"';
					}
					from += stringLength + 2;
					break;
				case integer			:
					final String	intString = String.valueOf(entity.getEntityId());
					
					if (from + intString.length() < targetEnd) {
						intString.getChars(0,intString.length(),target,from);
					}
					from += intString.length();
					break;
				case real				:
					final String	realString = String.valueOf(Double.longBitsToDouble(entity.getEntityId()));
					
					if (from + realString.length() < targetEnd) {
						realString.getChars(0,realString.length(),target,from);
					}
					from += realString.length();
					break;
				case anonymous			:
					if (from < targetEnd) {
						target[from] = '_';
					}
					from++;
					break;
				case variable			:
					final int	varLength = getRepo().termRepo().getNameLength(entity.getEntityId());
					
					if (from + varLength < targetEnd) {
						getRepo().termRepo().getName(entity.getEntityId(),target,from);
					}
					from += varLength;
					break;
				case list				:
					if (((IFProList)entity).getChild() == null && ((IFProList)entity).getTail() == null) {
						if (from + 2 < targetEnd) {
							target[from] = '[';
							target[from+1] = ']';
						}
						from += 2;
					}
					else {
						char			start = '[';
						IFProEntity		actual = entity;
						
						while (actual instanceof IFProList) {
							if (from < targetEnd) {
								target[from] = start;		
							}
							from++;
							from = putEntity(((IFProList)actual).getChild(),target,from);
							actual = ((IFProList)actual).getTail();
							start = ',';
						}
						
						if (actual != null) {
							if (from < targetEnd) {
								target[from] = '|';		
							}
							from++;
							from = putEntity(((IFProList)entity).getTail(),target,from);
						}
						if (from < targetEnd) {
							target[from] = ']';		
						}
						from++;
					}
					break;
				case operator			:
					final String	opName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
					
					switch (((IFProOperator)entity).getOperatorType().getSort()) {
						case prefix :
//							final String	prefName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
//					
							from = printBracket(target,from,(IFProOperator)entity,((IFProOperator)entity).getLeft());
//							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getLeft())) {
//								if (from < targetEnd) {
//									target[from] = '(';		
//								}
//								from++;
//								from = putEntity(((IFProOperator)entity).getLeft(),target,from);
//								if (from < targetEnd) {
//									target[from] = ')';		
//								}
//								from++;
//							}
//							else {
//								from = putEntity(((IFProOperator)entity).getLeft(),target,from);
//							}
							if (from + opName.length() < targetEnd) {
								opName.getChars(0,opName.length(),target,from);
							}
							from += opName.length();
							break;
						case postfix :
//							final String	suffName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
//							
							if (from + opName.length() < targetEnd) {
								opName.getChars(0,opName.length(),target,from);
							}
							from += opName.length();
							from = printBracket(target,from,(IFProOperator)entity,((IFProOperator)entity).getRight());
//							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getRight())) {
//								if (from < targetEnd) {
//									target[from] = '(';		
//								}
//								from++;
//								from = putEntity(((IFProOperator)entity).getRight(),target,from);
//								if (from < targetEnd) {
//									target[from] = ')';		
//								}
//								from++;
//							}
//							else {
//								from = putEntity(((IFProOperator)entity).getRight(),target,from);
//							}
							break;
						case infix :
//							final String	infName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
							
							from = printBracket(target,from,(IFProOperator)entity,((IFProOperator)entity).getLeft());
//							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getLeft())) {
//								if (from < targetEnd) {
//									target[from] = '(';		
//								}
//								from++;
//								from = putEntity(((IFProOperator)entity).getLeft(),target,from);
//								if (from < targetEnd) {
//									target[from] = ')';		
//								}
//								from++;
//							}
//							else {
//								from = putEntity(((IFProOperator)entity).getLeft(),target,from);
//							}
							if (from + opName.length() < targetEnd) {
								opName.getChars(0,opName.length(),target,from);
							}
							from += opName.length();
							from = printBracket(target,from,(IFProOperator)entity,((IFProOperator)entity).getRight());
//							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getRight())) {
//								if (from < targetEnd) {
//									target[from] = '(';		
//								}
//								from++;
//								from = putEntity(((IFProOperator)entity).getRight(),target,from);
//								if (from < targetEnd) {
//									target[from] = ')';		
//								}
//								from++;
//							}
//							else {
//								from = putEntity(((IFProOperator)entity).getRight(),target,from);
//							}
							break;
					}
					if (((IFProRuledEntity)entity).getRule() != null) {
						if (from + 1 < targetEnd) {
							target[from] = ':';		
							target[from+1] = '-';		
						}
						from += 2;
						from = putEntity(((IFProRuledEntity)entity).getRule(),target,from);
					}
					break;
				case predicate			:
					final int	predLength = getRepo().termRepo().getNameLength(entity.getEntityId());
					
					if (from + predLength < targetEnd) {
						getRepo().termRepo().getName(entity.getEntityId(),target,from);
					}
					from += predLength;
					
					if (((IFProPredicate)entity).getArity() > 0) {
						char	prefix = '(';
						
						for (int index = 0; index < ((IFProPredicate)entity).getArity(); index++) {
							if (from < targetEnd) {
								target[from] = prefix;		
							}
							from++;		
							prefix = ',';
							from = putEntity(((IFProPredicate)entity).getParameters()[index],target,from);
						}
						if (from < targetEnd) {
							target[from] = ')';		
						}
						from++;
					}
					if (((IFProRuledEntity)entity).getRule() != null) {
						if (from + 1 < targetEnd) {
							target[from] = ':';		
							target[from+1] = '-';		
						}
						from += 2;
						from = putEntity(((IFProRuledEntity)entity).getRule(),target,from);
					}
					break;
				case operatordef		:
					final String	opDef = String.format("op(%1$d,%2$s,%3$s)",((IFProOperator)entity).getPriority(),((IFProOperator)entity).getOperatorType(),repo.termRepo().getName(entity.getEntityId()));

					if (from + opDef.length() < targetEnd) {
						opDef.getChars(0,opDef.length(),target,from);
					}
					from += opDef.length();
					break;
				default :
					throw new UnsupportedOperationException("Unknown type ti upload: "+entity.getEntityType());
			}
		}
		return from;
	}

	private IFProEntity convert2Ruled(final IFProEntity entity) {
		final IFProEntity left = ((IFProOperator)entity).getLeft(), right = ((IFProOperator)entity).getRight();
		
		((IFProPredicate)left).setRule(right);
		right.setParent(left);
		left.setParent(null);
		
		return left;
	}
}
