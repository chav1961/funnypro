package chav1961.funnypro.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.EnternalPluginEntity;
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
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginItem;
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
	
	public ParserAndPrinter(final LoggerFacade log, final Properties prop, final IFProEntitiesRepo repo) throws SyntaxException {
		if (log == null) {
			throw new IllegalArgumentException("Log can't be null"); 
		}
		else if (prop == null) {
			throw new IllegalArgumentException("Properties can't be null"); 
		}
		else if (repo == null) {
			throw new IllegalArgumentException("Entities repo can't bw null"); 
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
	public void parseEntities(final CharacterSource source, final FProParserCallback callback) throws SyntaxException, IOException, ContentException {
		if (source == null) {
			throw new IllegalArgumentException("Source reader can't be null");
		}
		else if (callback == null) {
			throw new IllegalArgumentException("Callback can't be null");
		}
		else {
			final GrowableCharArray	gca = new GrowableCharArray(false);
			char					symbol;
			
			while ((symbol = source.next()) != CharacterSource.EOF) {
				gca.append(symbol);
			}
			parseEntities(gca.extract(),0,callback);
			gca.clear();
		}
	}

	@Override
	public int parseEntities(final char[] source, int from, final FProParserCallback callback)  throws SyntaxException, IOException {
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
	
	@Override
	public void putEntity(final IFProEntity entity, final CharacterTarget target) throws IOException, PrintingException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity to put can't be null!"); 
		}
		else if (target == null) {
			throw new IllegalArgumentException("Target stream can't be null!"); 
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
					switch (((IFProOperator)entity).getOperatorType()) {
						case xf : case yf :
							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getLeft())) {
								target.put('(');
								putEntity(((IFProOperator)entity).getLeft(),target);
								target.put(')');
							}
							else {
								putEntity(((IFProOperator)entity).getLeft(),target);
							}
							target.put(blankedName(getRepo().termRepo().getName(entity.getEntityId())));
							break;
						case fx : case fy :
							target.put(blankedName(getRepo().termRepo().getName(entity.getEntityId())));
							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getRight())) {
								target.put('(');
								putEntity(((IFProOperator)entity).getRight(),target);
								target.put(')');
							}
							else {
								putEntity(((IFProOperator)entity).getRight(),target);
							}
							break;
						case xfy : case yfx : case xfx :
							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getLeft())) {
								target.put('(');
								putEntity(((IFProOperator)entity).getLeft(),target);
								target.put(')');
							}
							else {
								putEntity(((IFProOperator)entity).getLeft(),target);
							}
							target.put(blankedName(getRepo().termRepo().getName(entity.getEntityId())));
							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getRight())) {
								target.put("(");
								putEntity(((IFProOperator)entity).getRight(),target);
								target.put(")");
							}
							else {
								putEntity(((IFProOperator)entity).getRight(),target);
							}
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
						String	prefix = "(";
						
						for (int index = 0; index < ((IFProPredicate)entity).getArity(); index++) {
							target.put(prefix);	prefix = ",";
							putEntity(((IFProPredicate)entity).getParameters()[index],target);
						}
						target.put(")");
					}
					if (((IFProRuledEntity)entity).getRule() != null) {
						target.put(":-");
						putEntity(((IFProRuledEntity)entity).getRule(),target);
					}
					break;
				case operatordef		:
					target.put(String.format("op(%1$d,%2$s,%3$s)",((IFProOperator)entity).getPriority(),((IFProOperator)entity).getOperatorType(),repo.termRepo().getName(entity.getEntityId())));
					break;
				case externalplugin		:
					target.put(String.format("$external$(\"%1$s\",\"%2$s\",\"%3$s\")",((IFProExternalEntity)entity).getPluginName(),((IFProExternalEntity)entity).getPluginProducer(),((IFProExternalEntity)entity).getPluginVersion()));
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
									case extern		:
										if (!prefixNow) {
											throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Two operands witout infix operators detected"); 
										}
										else {
											from = parseExtern(source,from,result);
											top[0] = result[0];
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
		if (result[0].getEntityType() == EntityType.operator
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
					switch (((IFProOperator)top[index]).getOperatorType()) {
						case fx :
						case fy :
						case xfx :
						case xfy :
						case yfx :
							((IFProOperator)top[index]).setRight(result);
							result = top[index];
							break;
						case xf :
						case yf :
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
		IFProList		actual;
		
		if (root.getEntityId() == tailId && (root instanceof IFProOperator)) {
			if (((IFProOperator)root).getRight() != null && (((IFProOperator)root).getRight().getEntityType() == EntityType.variable || ((IFProOperator)root).getRight().getEntityType() == EntityType.anonymous)) {
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
		else if (root.getEntityId() == colonId && (root instanceof IFProOperator)) {
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
		else {
			actual = new ListEntity(root,null);
			
			root.setParent(actual);
			return actual;
		}
	}

	private static IFProEntity[] andChain2Array(final IFProEntity root, final IFProEntity parent, final long colonId) {
		int				count;
		IFProEntity		actual;
		
		actual = root;		count = 0;
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
	
	private int parseExtern(final char[] source, final int from, final IFProEntity[] result) throws SyntaxException {
		final int[]	locations[] = new int[3][2];
		final int	parsed = FProUtil.simpleParser(source,from,"%b(%b\"%0c\"%b,%b\"%1c\"%b,%b\"%2d\"%b)",locations);
		
		if (parsed > from) {
			for (PluginItem item : repo.pluginsRepo().seek(new String(source,locations[0][0],locations[0][1])
														  ,new String(source,locations[1][0],locations[1][1])
														  ,new int[]{Integer.valueOf(new String(source,locations[2][0],locations[2][1]))})) {
				result[0] = new EnternalPluginEntity(item.getDescriptor().getPluginEntity());
				return parsed;
			}
			throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"External plugin ["+new String(source,locations[0][0],locations[0][1])+"] was not found in the external plugin repo!");
		}
		else {
			throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Illegal external plugin definition format!");
		}
	}

	private int parseOp(final char[] source, final int from, final IFProEntity[] result) throws SyntaxException, SyntaxException {
		final int[]	locations[] = new int[3][2], forPrty = new int[2];
		final int	parsed = FProUtil.simpleParser(source,from,"%b(%b%0d%b,%b%1c%b,%b%2c%b)",locations);
		
		if (parsed > from) {
			CharUtils.parseInt(source,locations[0][0],forPrty,false);
			final OperatorType	type = getRepo().operatorType(getRepo().termRepo().placeName(source,locations[1][0],locations[1][1],null));
			final long			operatorId = getRepo().termRepo().placeName(source,locations[2][0],locations[2][1],null);
			
			result[0] = new OperatorDefEntity(forPrty[0],type,operatorId);
			return parsed;
		}
		else {
			throw new SyntaxException(SyntaxException.toRow(source,from),SyntaxException.toCol(source,from),"Illegal operator definition format!");
		}
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
				return " "+name+" ";
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
	
	private int internalPutEntity(final IFProEntity entity, final char[] target, int from) throws IOException, PrintingException {
		if (entity == null) {
			throw new IllegalArgumentException("Entity places can't be null!"); 
		}
		else if (target == null) {
			throw new IllegalArgumentException("Target stream can't be null!"); 
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
						getRepo().stringRepo().getName(entity.getEntityId(),target,from);
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
					switch (((IFProOperator)entity).getOperatorType()) {
						case xf : case yf :
							final String	prefName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
							
							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getLeft())) {
								if (from < targetEnd) {
									target[from] = '(';		
								}
								from++;
								from = putEntity(((IFProOperator)entity).getLeft(),target,from);
								if (from < targetEnd) {
									target[from] = ')';		
								}
								from++;
							}
							else {
								from = putEntity(((IFProOperator)entity).getLeft(),target,from);
							}
							if (from + prefName.length() < targetEnd) {
								prefName.getChars(0,prefName.length(),target,from);
							}
							from += prefName.length();
							break;
						case fx : case fy :
							final String	suffName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
							
							if (from + suffName.length() < targetEnd) {
								suffName.getChars(0,suffName.length(),target,from);
							}
							from += suffName.length();
							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getRight())) {
								if (from < targetEnd) {
									target[from] = '(';		
								}
								from++;
								from = putEntity(((IFProOperator)entity).getRight(),target,from);
								if (from < targetEnd) {
									target[from] = ')';		
								}
								from++;
							}
							else {
								from = putEntity(((IFProOperator)entity).getRight(),target,from);
							}
							break;
						case xfy : case yfx : case xfx :
							final String	infName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
							
							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getLeft())) {
								if (from < targetEnd) {
									target[from] = '(';		
								}
								from++;
								from = putEntity(((IFProOperator)entity).getLeft(),target,from);
								if (from < targetEnd) {
									target[from] = ')';		
								}
								from++;
							}
							else {
								from = putEntity(((IFProOperator)entity).getLeft(),target,from);
							}
							if (from + infName.length() < targetEnd) {
								infName.getChars(0,infName.length(),target,from);
							}
							from += infName.length();
							if (needBracket((IFProOperator)entity,((IFProOperator)entity).getRight())) {
								if (from < targetEnd) {
									target[from] = '(';		
								}
								from++;
								from = putEntity(((IFProOperator)entity).getRight(),target,from);
								if (from < targetEnd) {
									target[from] = ')';		
								}
								from++;
							}
							else {
								from = putEntity(((IFProOperator)entity).getRight(),target,from);
							}
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
					final String	predName = blankedName(getRepo().termRepo().getName(entity.getEntityId()));
					
					if (from + predName.length() < targetEnd) {
						predName.getChars(0,predName.length(),target,from);
					}
					from += predName.length();
					if (((IFProPredicate)entity).getArity() > 0) {
						char	prefix = '(';
						
						for (int index = 0; index < ((IFProPredicate)entity).getArity(); index++) {
							if (from < targetEnd) {
								target[from] = prefix;		
							}
							from++;		prefix = ',';
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
				case externalplugin		:
					final String	extDef = String.format("$external$(\"%1$s\",\"%2$s\",\"%3$s\")",((IFProExternalEntity)entity).getPluginName(),((IFProExternalEntity)entity).getPluginProducer(),((IFProExternalEntity)entity).getPluginVersion());

					if (from + extDef.length() < targetEnd) {
						extDef.getChars(0,extDef.length(),target,from);
					}
					from += extDef.length();
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


class VarRepo implements AutoCloseable {
	private static final int			INITIAL_ARRAYS_SIZE = 64;
	
	private final List<IFProVariable>	vars;
	private VariableChain[]				varRepo = null;
	int									varCount = 0;
	
	public VarRepo() {
		this(null);
	}
	
	public VarRepo(final List<IFProVariable> vars) {
		this.vars = vars;
	}

	@Override
	public void close() throws Exception {
		if (varCount > 0) {			// Link all identical variables to chains
			if (vars != null) {		// Need fill variables list...
				for (int index = varRepo.length-varCount, maxIndex = varRepo.length; index < maxIndex; index++) {
					varRepo[index].chain = new VariableEntity(varRepo[index].chain.getEntityId()).setChain(varRepo[index].chain);
				}
			}
			for (int index = varRepo.length-varCount, maxIndex = varRepo.length; index < maxIndex; index++) {	// Make a ring chain for all identical variables in the entity
				IFProVariable	start = varRepo[index].chain;
				
				while (start.getChain() != start) {
					start = start.getChain();
				}
				start.setChain(varRepo[index].chain);
			}
			if (vars != null) {
				for (int index = varRepo.length-varCount, maxIndex = varRepo.length; index < maxIndex; index++) {
					vars.add(varRepo[index].chain);
				}
			}
		}
	}
	
	public void storeVariable(final IFProVariable entity) {
		final VariableChain		vc = new VariableChain(entity.getEntityId(),entity);
		
		if (varCount == 0) {
			varRepo = new VariableChain[INITIAL_ARRAYS_SIZE];
			Arrays.fill(varRepo,new VariableChain(-Integer.MAX_VALUE));
			varRepo[INITIAL_ARRAYS_SIZE-1] = vc;
			varCount++;
		}
		else {
			int				found = Arrays.binarySearch(varRepo,vc);
			
			if (found >= 0) {
				entity.setChain(varRepo[found].chain);
				varRepo[found].chain = entity;
			}
			else {
				if (varCount >= varRepo.length) {
					final VariableChain[]	newRepo = new VariableChain[2*varRepo.length];
					
					Arrays.fill(newRepo,new VariableChain(-Integer.MAX_VALUE));
					System.arraycopy(varRepo,0,newRepo,varRepo.length,varRepo.length);
					varRepo = newRepo;
					found = Arrays.binarySearch(varRepo,vc);
				}
				if (-found > varRepo.length) {
					System.arraycopy(varRepo,1,varRepo,0,-2-found);
					varRepo[-2-found] = vc;
				}
				else if (found == -1) {
					System.arraycopy(varRepo,0,varRepo,1,varRepo.length-1);
					varRepo[0] = vc;
				}
				else {
					System.arraycopy(varRepo,1,varRepo,0,-2-found);
					varRepo[-2-found] = vc;
				}
				varCount++;
			}
		}
	}	
	
	@Override
	public String toString() {
		return "VarRepo [vars=" + vars + ", varRepo=" + Arrays.toString(varRepo) + ", varCount=" + varCount + "]";
	}

	
	private static class VariableChain implements Comparable<VariableChain>{
		public long				id;
		public IFProVariable	chain = null;
		
		public VariableChain(final long id) {
			this.id = id;
		}

		public VariableChain(final long id, final IFProVariable chain) {
			this.id = id;		this.chain = chain;

		}
		
		@Override
		public int compareTo(final VariableChain o) {
			return o.id < id ? 1 : (o.id > id ? -1 : 0);
		}

		@Override public String toString() {return "VariableChain [id=" + id + "]";}		
	}
}
