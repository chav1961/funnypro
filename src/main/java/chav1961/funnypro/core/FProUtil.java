package chav1961.funnypro.core;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVariable;

/**
 * <p>This class contains most common usable methods for this package</p> 
 * @author chav1961
 *
 */
public class FProUtil {
	private static final int 		EXPONENT_SIZE = 300; 
	private static final double[]	EXPONENT = new double[2 * EXPONENT_SIZE + 1];

	static {
		double	start = Double.valueOf("1E-"+EXPONENT_SIZE).doubleValue();
		for (int index = 0; index < 2 * EXPONENT_SIZE + 1; index++) {
			EXPONENT[index] = start;
			start *= 10;
		}
	}
	
	/**
	 * <p>Simple parser to extract data from the char string</p>
	 * <p>All chars in the template expresses self, except special escapes. The set of escapes is:</p>
	 * <ul>
	 * <li>'{', '|', '}' - alternatives. Every alternative start need differs each other, except (possibly) the same last alternative (pointed as default)</li>
	 * <li>'[', ']' - optional. Need to have explicit differentiation from the next data.</li>
	 * <li>'&lt;', '&gt;seq...' - repeatable clauses. Seq can be any non-escaped char sequence without special chars</li>
	 * </ul>
	 * <p>Also a special escapes can be used:</p>
	 * <ul>
	 * <li>'%b' - any blank chars</li>
	 * <li>'%c' - any non-blank text</li>
	 * <li>'%NNNc' - any non-blank text. It's start and end position will be stored to to parsing result array</li>
	 * <li>'%d' - any decimal</li>
	 * <li>'%NNNd' - any decimal. It's start and end position will be stored to to parsing result array</li>
	 * <li>'%NNN:VVVm' - set location NNN with the value VVV. Not really parses data but can use for marking alternatives</li>
	 * <li>'%{', '%|', '%}', '%[', '%]', '%&lt;', '%&gt;', '%%' - chars as-is</li>
	 * </ul>
	 * @param source source string to parse
	 * @param from start position to parse
	 * @param template parse template to manage parsing
	 * @param locations all %NNNc and %NNNd locations was detected. Non-used members of this array fill be filled by [-1,-1] values. 
	 * @return end position in the parsed string
	 */
	public static int simpleParser(final char[] source, final int from, final String template, final int[][] locations) {
		if (template == null || template.isEmpty()) {
			throw new IllegalArgumentException("Template can;t be null or empty");
		}
		else {
			return simpleParser(source,from,template.toCharArray(),locations);
		}
	}

	/**
	 * <p>Simple parser to extract data from the char string</p>
	 * <p>All chars in the template expresses self, except special escapes. The set of escapes is:</p>
	 * <ul>
	 * <li>'{', '|', '}' - alternatives. Every alternative start need differs each other, except (possibly) the same last alternative (pointed as default)</li>
	 * <li>'[', ']' - optional. Need to have explicit differentiation from the next data.</li>
	 * <li>'&lt;', '&gt;seq...' - repeatable clauses. Seq can be any non-escaped char sequence without special chars</li>
	 * </ul>
	 * <p>Also a special escapes can be used:</p>
	 * <ul>
	 * <li>'%b' - any blank chars</li>
	 * <li>'%c' - any non-blank text</li>
	 * <li>'%NNNc' - any non-blank text. It's start and end position will be stored to to parsing result array</li>
	 * <li>'%d' - any decimal</li>
	 * <li>'%NNNd' - any decimal. It's start and end position will be stored to to parsing result array</li>
	 * <li>'%NNN:VVVm' - set location NNN with the value VVV. Not really parses data but can use for marking alternatives</li>
	 * <li>'%{', '%|', '%}', '%[', '%]', '%&lt;', '%&gt;', '%%' - chars as-is</li>
	 * </ul>
	 * @param source source string to parse
	 * @param from start position to parse
	 * @param template parse template to manage parsing
	 * @param locations all %NNNc and %NNNd locations was detected. Non-used members of this array fill be filled by [] values. Repeatable clauses will have 2*N elements. Can be null, if locations are not interested 
	 * @return end position in the parsed string
	 */
	public static int simpleParser(final char[] source, final int from, final char[] template, final int[][] locations) {
		if (source == null || source.length == 0) {
			throw new IllegalArgumentException("Source array can't be null or empty");
		}
		else if (template == null || template.length == 0) {
			throw new IllegalArgumentException("Template can't be null or empty");
		}
		else if (from < 0 || from >= source.length) {
			throw new IllegalArgumentException("Start position ["+from+"] is outside source. Need be 0.."+(source.length-1));
		}
		else {
			if (locations != null) {
				for (int index = 0; index < locations.length; index++) {
					locations[index] = new int[0]; 
				}
			}
			
			return simpleParser(source, from, source.length, template, 0, template.length, locations, false);
		}
	}

	/**
	 * <p>Serialize entities to the output stream</p>
	 * @param target stream to serialize to
	 * @param entity entity to serialize
	 * @throws IOException in any I/O errors
	 */
	public static void serialize(final OutputStream target, final IFProEntity entity) throws IOException {
		if (target == null) {
			throw new IllegalArgumentException("Target stream can't be null!"); 
		}
		else if (entity == null) {
			throw new IllegalArgumentException("Entity to serialize can't be null!"); 
		}
		else {
			CommonUtil.writeInt(target,entity.getEntityType().ordinal());		// Write operator type
			switch (entity.getEntityType()) {
				case string			:
				case integer		:
				case real			:
				case variable		:
					CommonUtil.writeLong(target,entity.getEntityId());
					break;
				case anonymous		:
					break;
				case list			:
					CommonUtil.writeInt(target,(((IFProList)entity).getChild() != null ? 2 : 0) + (((IFProList)entity).getTail() != null ? 1 : 0));
					if (((IFProList)entity).getChild() != null) {
						serialize(target,((IFProList)entity).getChild());
					}
					if (((IFProList)entity).getTail() != null) {
						serialize(target,((IFProList)entity).getTail());
					}
					break;
				case operator		:
					CommonUtil.writeLong(target,entity.getEntityId());
					CommonUtil.writeInt(target,((IFProOperator)entity).getPriority());
					CommonUtil.writeInt(target,((IFProOperator)entity).getOperatorType().ordinal());
					switch (((IFProOperator)entity).getOperatorType()) {
						case xf : case yf :
							serialize(target,((IFProOperator)entity).getLeft());
							break;
						case fx : case fy :
							serialize(target,((IFProOperator)entity).getRight());
							break;
						case xfy : case yfx : case xfx :					
							serialize(target,((IFProOperator)entity).getLeft());
							serialize(target,((IFProOperator)entity).getRight());
							break;
						default :
							throw new UnsupportedOperationException("Operator Type ["+entity.getEntityType()+"] is not supported!");
					}
					break;
				case predicate		:
					CommonUtil.writeLong(target,entity.getEntityId());
					CommonUtil.writeInt(target,((IFProPredicate)entity).getArity());
					for (int index = 0; index < ((IFProPredicate)entity).getArity(); index++) {
						serialize(target,((IFProPredicate)entity).getParameters()[index]);
					}
					break;
				default :
					throw new UnsupportedOperationException("Type ["+entity.getEntityType()+"] of the node is not supported!");
			}
		}
	}

	/**
	 * <p>Deserialize entities from input stream</p>
	 * @param source input stream to deserialize from
	 * @return deserialized entity
	 * @throws IOException on any I/O errors
	 */
	public static IFProEntity deserialize(final InputStream source) throws IOException {
		if (source == null) {
			throw new IllegalArgumentException("Target stream can't be null!"); 
		}
		else {
			final EntityType	type = EntityType.values()[CommonUtil.readInt(source)];

			switch (type) {
				case string			:
					return new StringEntity(CommonUtil.readLong(source));
				case integer		:
					return new IntegerEntity(CommonUtil.readLong(source));
				case real			:
					return new RealEntity(Double.longBitsToDouble(CommonUtil.readLong(source)));
				case variable		:
					return new VariableEntity(CommonUtil.readLong(source));
				case anonymous		:
					return new AnonymousEntity();
				case list			:
					final int			mask = CommonUtil.readInt(source);
					return new ListEntity((mask & 0x02) != 0 ? deserialize(source) : null,(mask & 0x01) != 0 ? deserialize(source) : null); 
				case operator		:
					final long				operatorId = CommonUtil.readLong(source);
					final int				priority = CommonUtil.readInt(source);
					final OperatorType		opType = OperatorType.values()[CommonUtil.readInt(source)];
					final OperatorEntity	opEntity = new OperatorEntity(priority,opType,operatorId);
					
					switch (opType) {
						case xf : case yf :
							opEntity.setLeft(deserialize(source));
							opEntity.getLeft().setParent(opEntity);
							break;
						case fx : case fy :
							opEntity.setRight(deserialize(source));
							opEntity.getRight().setParent(opEntity);
							break;
						case xfy : case yfx : case xfx :					
							opEntity.setLeft(deserialize(source));
							opEntity.getLeft().setParent(opEntity);
							opEntity.setRight(deserialize(source));
							opEntity.getRight().setParent(opEntity);
							break;
						default :
							throw new UnsupportedOperationException("Operator Type ["+opType+"] is not supported!");
					}
					return opEntity;
				case predicate		:
					final long				predicateId = CommonUtil.readLong(source);
					final int				arity = CommonUtil.readInt(source);
					final IFProEntity[]		parms = new IFProEntity[arity];
					final PredicateEntity	predEntity = new PredicateEntity(predicateId,parms); 
					
					for (int index = 0; index < parms.length; index++) {
						parms[index] = deserialize(source);
						parms[index].setParent(predEntity);
					}
					return predEntity;
				default :
					throw new UnsupportedOperationException("Type ["+type+"] of the node is not supported!");
			}
		}
	}
	
	
	/**
	 * <p>Unify two entities and bind variables. Entities after unification will have corrupted parent refs. To restore them call unbind </p>
	 * @param peek entity will be unified with. 
	 * @param entity entity to unify with
	 * @param changesList changes in the entities (modelling parameters by refs). Need always be an array wit exactly one (non)null element. Will be filled with changes
	 * @return true if unification is successful
	 */
	public static boolean unify(final IFProEntity peek, final IFProEntity entity, final Change[] changesList) {
		if (changesList == null || changesList.length < 1) {
			throw new IllegalArgumentException("Changes list need be non-null and non-empty arrray!");
		}
		else if (peek == entity) {
			return true;
		}
		else if (peek == null || entity == null) {
			return false;
		}
		else {final EntityType		leftType = peek.getEntityType(), rightType = entity.getEntityType();
		
			 if (peek.getEntityId() == entity.getEntityId() && leftType == rightType) {
				switch (rightType) {
					case string				:
					case integer			:
					case real				:
					case anonymous			:
						return true;
					case variable			:
						joinChains(changesList,(IFProVariable)peek,(IFProVariable)entity);
						return true;
					case list				:
						return unify(((IFProList)peek).getChild(),((IFProList)entity).getChild(),changesList) 
								&& unify(((IFProList)peek).getTail(),((IFProList)entity).getTail(),changesList); 
					case operator			:
						if (((IFProOperator)peek).getPriority() == ((IFProOperator)entity).getPriority() && ((IFProOperator)peek).getOperatorType() == ((IFProOperator)entity).getOperatorType()) {
							return unify(((IFProOperator)peek).getLeft(),((IFProOperator)entity).getLeft(),changesList)
									&& unify(((IFProOperator)peek).getRight(),((IFProOperator)entity).getRight(),changesList);
						}
						else {
							return false;
						}
					case predicate			:
						if (((IFProPredicate)peek).getArity() == ((IFProPredicate)entity).getArity()) {
							final IFProEntity[]	left = ((IFProPredicate)peek).getParameters(), right = ((IFProPredicate)entity).getParameters();
							
							for (int index = 0, maxIndex = ((IFProPredicate)entity).getArity(); index < maxIndex; index++) {
								if (!unify(left[index],right[index],changesList)) {
									return false;
								}
							}
							return true;
						}
						else {
							return false;
						}
					default :
						throw new UnsupportedOperationException("Unknown type to scratch: "+entity.getEntityType());
				}
			}
			else if (leftType == EntityType.anonymous || rightType == EntityType.anonymous) {
				return true;
			}
			else if (leftType == EntityType.variable && rightType == EntityType.variable) {
				joinChains(changesList,(IFProVariable)peek,(IFProVariable)entity);
				return true;
			}
			else if (leftType == EntityType.variable) {
				substitute(changesList,((IFProVariable)peek),entity);
				return true;
			}
			else if (rightType == EntityType.variable) {
				substitute(changesList,((IFProVariable)entity),peek);
				return true;
			}
			else {
				return false;
			}
		}
	}

	/**
	 * <p>Restore two unified entities to it's original state.</p>
	 * @param top content of the changesList[0] (see {@link FProUtil#unify(IFProEntity, IFProEntity, Change[])})
	 */
	public static void unbind(final Change top) {
		Change	actual = top, temp;
		
		while (actual != null) {
			switch (actual.location) {
				case Change.IN_PARENT	:
					actual.container.setParent(actual.oldValue);
					break;
				case Change.IN_CHILD	:
					((IFProList)actual.container).setChild(actual.oldValue);
					break;
				case Change.IN_TAIL		:
					((IFProList)actual.container).setTail(actual.oldValue);
					break;
				case Change.IN_CHAIN	:
					((IFProVariable)actual.container).setChain((IFProVariable) actual.oldValue);
					break;
				case Change.IN_LEFT		:
					((IFProOperator)actual.container).setLeft(actual.oldValue);
					break;
				case Change.IN_RIGHT	:
					((IFProOperator)actual.container).setRight(actual.oldValue);
					break;
				default :
					((IFProPredicate)actual.container).getParameters()[actual.location] = actual.oldValue;
					break;
			}
			temp = actual.next;		actual.next = null;		actual = temp;			
		}
	}
	
	/**
	 * <p>Duplicate entity with all children</p>
	 * @param source item to duplicate. Null source returns null result
	 * @return duplicated item
	 */
	public static IFProEntity duplicate(final IFProEntity source) {
		if (source == null) {
			return null;
		}
		else {
			switch (source.getEntityType()) {
				case string			:
					return new StringEntity(source.getEntityId());
				case integer		:
					return new IntegerEntity(source.getEntityId());
				case real			:
					return new RealEntity(Double.longBitsToDouble(source.getEntityId()));
				case anonymous		:
					return new AnonymousEntity(); 
				case list			:
					final IFProList			list = new ListEntity(duplicate(((IFProList)source).getChild()),duplicate(((IFProList)source).getTail())); 
					
					if (list.getChild() != null) {
						list.getChild().setParent(list);
					}
					if (list.getTail() != null) {
						list.getTail().setParent(list);
					}
					return list;
				case operator		:
					final IFProOperator		op = new OperatorEntity((IFProOperator)source).setLeft(duplicate(((IFProOperator)source).getLeft())).setRight(duplicate(((IFProOperator)source).getRight())); 
					
					if (op.getLeft() != null) {
						op.getLeft().setParent(op);
					}
					if (op.getRight() != null) {
						op.getRight().setParent(op);
					}
					return op;
				case predicate		:
					final IFProPredicate	pred = new PredicateEntity(source.getEntityId());
					final IFProEntity[]		parm = new IFProEntity[((IFProPredicate)source).getArity()];
					
					for (int index = 0; index < parm.length; index++){
						if ((parm[index] = duplicate(((IFProPredicate)source).getParameters()[index])) != null) {
							parm[index].setParent(pred);
						}
					}
					pred.setParameters(parm);
					return pred;
				default :
					throw new IllegalArgumentException("Entity type ["+source.getEntityType()+"] can't be duplicated!");
			
			}
		}
	}

	/**
	 * <p>Remove entity and all it's children</p>
	 * @param source entity to remove
	 */
	public static void removeEntity(final IFProEntity source) {
		if (source != null) {
			source.setParent(null);
			switch (source.getEntityType()) {
				case string			:
				case integer		:
				case real			:
				case anonymous		:
					break;
				case list			:
					if (((IFProList)source).getChild() != null) {
						removeEntity(((IFProList)source).getChild());
						((IFProList)source).setChild(null);
					}
					if (((IFProList)source).getTail() != null) {
						removeEntity(((IFProList)source).getTail());
						((IFProList)source).setTail(null);
					}
					break;
				case operator		:
					if (((IFProOperator)source).getLeft() != null) {
						removeEntity(((IFProOperator)source).getLeft());
						((IFProOperator)source).setLeft(null);
					}
					if (((IFProOperator)source).getRight() != null) {
						removeEntity(((IFProOperator)source).getRight());
						((IFProOperator)source).setRight(null);
					}
					break;
				case predicate		:
					final IFProEntity[]		parm = ((IFProPredicate)source).getParameters();
					
					for (int index = 0; index < parm.length; index++){
						removeEntity(parm[index]);
						parm[index] = null;
					}
					break;
				default :
					throw new IllegalArgumentException("Entity type ["+source.getEntityType()+"] can't be duplicated!");
			}
		}
	}
	
	public static enum ContentType {
		Anon, Var, NonVar, Atom, Integer, Float, Number, Atomic, Compound,		
	}	
	
	/**
	 * <p>Test entity type in the FPro terminlogy</p> 
	 * @param entity entity to test
	 * @param type check type (according to FRpo tests)
	 * @return true if entity is a tested type
	 */
	public static boolean isEntityA(final IFProEntity entity, final ContentType type) {
		if (type == null) {
			throw new IllegalArgumentException("Type can't be null");
		}
		else if (entity == null) {
			return false;
		}
		else {
			switch (type) {
				case Anon		:
					return entity.getEntityType() == EntityType.anonymous;
				case Var		:
					return entity.getEntityType() == EntityType.variable;
				case NonVar		:
					return entity.getEntityType() != EntityType.variable;
				case Atom		:
					return entity.getEntityType() == EntityType.string || entity.getEntityType() == EntityType.predicate; 
				case Integer	:
					return entity.getEntityType() == EntityType.integer;
				case Float		:
					return entity.getEntityType() == EntityType.real;
				case Number		:
					return entity.getEntityType() == EntityType.integer || entity.getEntityType() == EntityType.real; 
				case Atomic		:
					return entity.getEntityType() == EntityType.integer || entity.getEntityType() == EntityType.real || entity.getEntityType() == EntityType.string || entity.getEntityType() == EntityType.predicate; 
				case Compound	:	
					return entity.getEntityType() == EntityType.list || entity.getEntityType() == EntityType.operator;
				default :
					return false;
			}
		}
	}
	
	/**
	 * <p>Convert location inside string to row/col pair
	 * @param source string located
	 * @param location position inside string
	 * @return row[0]/col[1]
	 */
	public static int[] toRowCol(final char[] source, final int location) {
		if (source == null) {
			throw new IllegalArgumentException("Source string can't be null"); 
		}
		else if (location < 0 || location >= source.length + 1) {
			throw new IllegalArgumentException("Location ["+location+"] out of range 0.."+source.length); 
		}
		else {
			final int[]		result = new int[2];
			
			for (int index = Math.min(location,source.length-1); index >= 0; index--) {
				if (source[index] == '\n') {
					result[0]++;
					result[1] = 0;
				}
				else {
					result[1]++;
				}
			}
			result[0]++;
			result[1]++;
			return result;
		}
	}
	
	private static int simpleParser(final char[] source, final int from, final int to, final char[] template, final int templateFrom, final int templateTo, final int[][] locations, final boolean needRestore) {
		int[]		lastTail = null;
		int			start = from, stop;
		boolean		failure = false; 
		
		if (templateFrom == templateTo) {							// Empty template area always failed
			return -1;
		}

		if (!needRestore && locations != null) {
			lastTail = new int[locations.length];
			for (int index = 0; index < lastTail.length; index++) {	// Store the sate of the locations for future restoring
				lastTail[index] = locations[index].length;
			}
		}

next:	for (int index = templateFrom; index < templateTo; index++) {
			switch (template[index]) {
				case '%'	:
					if (index < templateTo - 1) {
						switch (template[index+1]) {
							case '{' : case '|'	: case '}' : case '[' : case ']' : case '<' : case '>' : case '*' : case '+' : case '%' :	// Test escaped char as-is
								if (start < to && source[start] == template[index+1]) {
									start++;	index++;
								}
								else {
									failure = true;
									break next;
								}
								break;
							case 'b' :	// Skip all blanks in the source
								while (start < to && source[start] <= ' ') {
									start++;
								}
								index++;
								break;
							case 'c' :	// Skip all non-banks in the source until the next template clause
								while (start < to && source[start] > ' ' && simpleParser(source,start,to,template,index+2,templateTo,locations,true) == -1) {
									start++;
								}
								if (start < to) {
									index++;
								}
								else {
									failure = index < templateTo;
									break next;
								}
								break;
							case 'd' :	// Skip all the number in the source
								while (start < to && source[start] >= '0' && source[start] <= '9') {
									start++;
								}
								index++;
								break;	// Any descriptor with storing it's bound locations
							case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
								int		addIndex = 0;
								
								while (index < templateTo - 1 && template[index+1] >= '0' && template[index+1] <= '9') {
									addIndex = addIndex * 10 + template[index+1] - '0';
									index++;
								}
								if (locations != null && addIndex >= locations.length) {
									throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: index value "+addIndex+" for %NNN{c|d|m}-escape is outside the bounds. Need be 0.."+(locations.length-1));
								}
								
								if (index < templateTo - 1) {
									switch (template[index+1]) {
										case 'c' :	// Skip all non-banks in the source until the next template clause and store it's location
											int		beginningText = start;
											
											if (start >= to) {
												failure = true;
												break next;
											}
											else {
												while (start < to && source[start] > ' ') {
													if ((stop = simpleParser(source,start,to,template,index+2,templateTo,locations,true)) == -1) {
														start++;
													}
													else {
														break;
													}
												}
												if (!needRestore && locations != null) {
													locations[addIndex] = simpleParserExpandArray(locations[addIndex],beginningText,start);
												}
												index++;
												if (start >= to) {
													failure = index+1 < templateTo;
													break next;
												}
											}
											break;
										case 'd' : // Skip all the number in the source an store it's locations
											int		beginningNumber = start;
											
											while (start < to && source[start] >= '0' && source[start] <= '9') {
												start++;
											}
											if (start > beginningNumber) {
												if (!needRestore && locations != null) {
													locations[addIndex] = simpleParserExpandArray(locations[addIndex],beginningNumber,start);
												}
												index++;
											}
											else {
												failure = true;
												break next;
											}
											break;
										case ':' :	// Store value described into the locations without moving on the source
											int	addValue = 0;
											
											index++;
											while (index < templateTo - 1 && template[index+1] >= '0' && template[index+1] <= '9') {
												addValue = addValue * 10 + template[index+1] - '0';
												index++;
											}
											if (index < templateTo - 1) {
												switch (template[index+1]) {
													case 'm' :
														if (!needRestore && locations != null) {
															locations[addIndex] = simpleParserExpandArray(locations[addIndex],addValue,addValue);
														}
														index++;
														break;
													default :
														throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: illegal %-escape (unknown letter at the tail)");
												}
											}
											else {
												failure = true;
												break next;
											}
											break;
										default :
											throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: illegal %-escape (unknown letter at the tail)");
									}
								}
								else {
									throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: illegal %-escape (unknown letter at the tail)");
								}
								break;
							default :
								throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: illegal %-escape (unknown letter at the tail)");								
						}
					}
					else {
						throw new IllegalArgumentException("Template error at col "+index+" for ["+new String(template)+"]: truncated %-escape");
					}
					break;
				case '{'	:	// Process alternatives
					final int[][]	result1 = simpleParserLocateParts(template,index+1,templateTo);
					
					if (result1 != null) {
						for (int[] item : result1) {
							if ((stop = simpleParser(source,start,to,template,item[0],item[1],locations,needRestore)) != -1) {
								start = stop;
								index = result1[result1.length-1][1];
								continue next;
							}
						}
						failure = true;
						break next;
					}
					else {
						throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: closed pair not found!");
					}
				case '['	:	// Process optionals
					final int[][]	result2 = simpleParserLocateParts(template,index+1,templateTo);
					
					if (result2 != null) {
						final int		optionalTemplateSize = (result2[0][1]-result2[0][0])+(templateTo-result2[0][1]-1);
						final char[]	optionalTemplate = new char[optionalTemplateSize];
						
						System.arraycopy(template,result2[0][0],optionalTemplate,0,result2[0][1]-result2[0][0]);
						System.arraycopy(template,result2[0][1]+1,optionalTemplate,result2[0][1]-result2[0][0],templateTo-result2[0][1]-1);
						
						if ((stop = simpleParser(source,start,to,optionalTemplate,0,optionalTemplateSize,locations,needRestore)) != -1) {
							start = stop;
							index =  templateTo - 1;
						}
						else {
							index = result2[0][1];
						}
						break;
					}
					else {
						throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: closed pair not found!");
					}
				case '<'	:	// Process repeatable clauses
					final int[][]	result3 = simpleParserLocateParts(template,index+1,templateTo);
					
					if (result3 != null) {
						int 			scan;
						boolean			pointsFound = false;

						for (scan = result3[0][1]+1; scan < templateTo - 2; scan++) {		// Seek terminal of the clause
							if (template[scan] == '.' && template[scan+1] == '.' && template[scan+2] == '.'){
								pointsFound = true;
								break;
							}
						}
						if (!pointsFound) {
							throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: missing '...' clause after repeatable clause!");
						}
						
						final int		repeatedTemplateSize = scan-result3[0][0]-1;
						final char[]	repeatedTemplate = new char[repeatedTemplateSize];
						
						System.arraycopy(template,result3[0][0],repeatedTemplate,0,result3[0][1]-result3[0][0]);
						System.arraycopy(template,result3[0][1]+1,repeatedTemplate,result3[0][1]-result3[0][0],scan-result3[0][1]-1);

						final int		terminatedTemplateSize = (result3[0][1]-result3[0][0])+(templateTo-scan-3) ;
						final char[]	terminatedTemplate = new char[terminatedTemplateSize];

						System.arraycopy(template,result3[0][0],terminatedTemplate,0,result3[0][1]-result3[0][0]);
						System.arraycopy(template,scan+3,terminatedTemplate,result3[0][1]-result3[0][0],terminatedTemplateSize-(result3[0][1]-result3[0][0]));
						
repeat:					while(start < to) {
							if ((stop = simpleParser(source,start,to,repeatedTemplate,0,repeatedTemplateSize,locations,needRestore)) != -1) {
								if (start == stop) {
									index = scan + 2;
									break;
								}
								else {
									start = stop;
									continue repeat;
								}
							}
							else if ((stop = simpleParser(source,start,to,terminatedTemplate,0,terminatedTemplateSize,locations,needRestore)) != -1) {
								start = stop;
								index = templateTo - 1;
								break;
							}
							else {
								failure = true;
								break;
							}
						}
					}
					else {
						throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(template)+"]: closed pair not found!");
					}
					break;
				default		:	// Test exactly equality for the template and source chars
					if (start < to && source[start] == template[index]) {
						start++;
					}
					else {
						failure = true;
						break next;
					}
					break;
			}
		}
		
		if (!needRestore && failure && locations != null) {	// Restore locations state if comparison failed
			for (int index = 0; index < lastTail.length; index++) {
				if (locations[index].length > lastTail[index]) {
					final int[]		newValue = new int[lastTail[index]];
					
					System.arraycopy(locations[index], 0, newValue, 0, lastTail[index]);
					locations[index] = newValue;
				}
			}
		}
		return failure ? -1 : start;
	}
	
	private static int[] simpleParserExpandArray(final int[] source, final int data1, final int data2) {
		final int[]	result = new int[source.length+2];
		
		System.arraycopy(source,0,result,0,source.length);
		result[result.length-2] = data1;		result[result.length-1] = data2;
		return result;
	}

	private static int[][] simpleParserExpandArray(final int[][] source, final int data1, final int data2) {
		final int[][]	result = new int[source.length+1][];
		
		System.arraycopy(source,0,result,0,source.length);
		result[result.length-1] = new int[]{data1,data2};
		return result;
	}
	
	private static int[][] simpleParserLocateParts(final char[] source, final int from, final int to) {
		int			start = from;
		int[][]		result = new int[0][];
		
		for (int index = from; index < to; index++) {
			switch (source[index]) {
				case '%'	:
					if (index < to - 1) {
						switch (source[index+1]) {
							case '{' : case '|'	: case '}' : case '[' : case ']' : case '<' : case '>' : case '*' : case '+' : case '%' : case 'b' : case 'c' : case 'd' :
								index++;
								break;
							case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
								while (index < to - 1 && source[index+1] >= '0' && source[index+1] <= '9') {
									index++;
								}
								if (index < to - 1) {
									switch (source[index+1]) {
										case 'c' : case 'd' :
											index++;
											break;
										case ':' :
											index++;
											while (index < to - 1 && source[index+1] >= '0' && source[index+1] <= '9') {
												index++;
											}
											if (index < to - 1 && source[index+1] == 'm') {
												index++;
											}
											else {
												throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(source)+"]: illegal %-escape (unknown letter at the tail)");
											}
											break;
										default  :
											throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(source)+"]: illegal %-escape (unknown letter at the tail)");
									}
								}
								break;
							default :
								throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(source)+"]: illegal %-escape (unknown letter at the tail)");
						}
					}
					else {
						throw new IllegalArgumentException("Template error at col "+index+" for ["+Arrays.toString(source)+"]: truncated %-escape");
					}
					break;
				case '{'	:
					final int[][]	result1 = simpleParserLocateParts(source, index+1, to);
					
					index = result1[result1.length-1][1];
					break;
				case '['	:
					final int[][]	result2 = simpleParserLocateParts(source, index+1, to);
					
					index = result2[result2.length-1][1];
					break;
				case '<'	:
					final int[][]	result3 = simpleParserLocateParts(source, index+1, to);
					
					index = result3[result3.length-1][1];
					break;
				case '|'	:
					result = simpleParserExpandArray(result,start,index);
					start = index+1;
					break;
				case '}'	:
				case ']'	:
				case '>'	:
					return simpleParserExpandArray(result,start,index);
			}
		}
		return null;
	}

	private static void joinChains(final Change[] changesList, final IFProVariable var1, final IFProVariable var2) {
		if (var1 != var2) {
			if (var1.getChain() != var1) {
				IFProVariable	start = var1;
				
				while (start != var1) {
					if (start == var2) {	// Chains are joined!
						return;
					}
					else {
						start = start.getChain();
					}
				}
			}
			
			IFProVariable temp = var1.getChain();		// Join chains and store changed fields for unbinding!	
			placeChanges(changesList,var1,var1.getChain(),Change.IN_CHAIN);		
			var1.setChain(var2.getChain());	
			placeChanges(changesList,var2,var2.getChain(),Change.IN_CHAIN);		
			var2.setChain(temp);
		}
	}

	private static void substitute(final Change[] changesList, final IFProVariable var, final IFProEntity value) {
		if (var.getChain() == var) {
			if (var.getParent() == null) {
				placeChanges(changesList,var,null,Change.IN_PARENT);
				var.setParent(value);		// Used when nextEntity(...,List) used to make a list Var=value
			}
			else {
				substitute(changesList,var,var.getParent(),value);
			}
		}
		else {
			IFProVariable	start = var;
			
			do {if (start.getParent() == null) {
					placeChanges(changesList,start,null,Change.IN_PARENT);
					start.setParent(value);		// Used when nextEntity(...,List) used to make a list Var=value
				}
				else {
					substitute(changesList,start,start.getParent(),value);
				}
				start = start.getChain();
			} while (start != var);
		}
	}
	
	private static void substitute(final Change[] changesList, final IFProVariable var, final IFProEntity container, final IFProEntity value) {
		if (container == null) {
			throw new IllegalArgumentException("Container can't be null"); 
		}
		else {
			switch (container.getEntityType()) {
				case list				:
					if (((IFProList)container).getChild() == var) {
						placeChanges(changesList,container,var,Change.IN_CHILD);
						((IFProList)container).setChild(value);
					}
					else if (((IFProList)container).getTail() == var) {
						placeChanges(changesList,container,var,Change.IN_TAIL);		
						((IFProList)container).setTail(value);
					}
					else {
						throw new IllegalArgumentException();
					}
					break;
				case operator			:
					if (((IFProOperator)container).getLeft() == var) {
						placeChanges(changesList,container,var,Change.IN_LEFT);		
						((IFProOperator)container).setLeft(value);
					}
					else if (((IFProOperator)container).getRight() == var) {
						placeChanges(changesList,container,var,Change.IN_RIGHT);		
						((IFProOperator)container).setRight(value);
					}
					else {
						throw new IllegalArgumentException();
					}
					break;
				case predicate			:
					final IFProEntity[]	parm = ((IFProPredicate)container).getParameters(); 
					
					for (int index = 0, maxIndex = ((IFProPredicate)container).getArity(); index < maxIndex; index++) {
						if (parm[index] == var) {
							placeChanges(changesList,container,var,index);		
							parm[index] = value;
						}
					}
					break;
				default :
					throw new UnsupportedOperationException("Unknown type to substitute: "+container.getEntityType());
			}
		}
	}
	
	private static void placeChanges(final Change[] changesList, final IFProEntity container, final IFProEntity oldValue, final int location) {
		final Change	ch = new Change();
		
		ch.next = changesList[0];
		ch.container = container;
		ch.oldValue = oldValue;
		ch.location = location;
		changesList[0] = ch;
	}	
	
	public static class Change {
		public static final int		IN_PARENT = -1;
		public static final int		IN_CHILD = -2;
		public static final int		IN_TAIL = -3;
		public static final int		IN_CHAIN = -4;
		public static final int		IN_LEFT = -5;
		public static final int		IN_RIGHT = -6;
		
		public Change 				next;
		public IFProEntity 			container;
		public IFProEntity 			oldValue;
		public int 					location;
	}

	public static short[] id2Shorts(long id) {
		// TODO Auto-generated method stub
		return null;
	}
}
