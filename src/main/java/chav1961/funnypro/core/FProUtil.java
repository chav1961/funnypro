package chav1961.funnypro.core;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import chav1961.funnypro.core.entities.AnonymousEntity;
import chav1961.funnypro.core.entities.IntegerEntity;
import chav1961.funnypro.core.entities.ListEntity;
import chav1961.funnypro.core.entities.OperatorEntity;
import chav1961.funnypro.core.entities.PredicateEntity;
import chav1961.funnypro.core.entities.RealEntity;
import chav1961.funnypro.core.entities.StringEntity;
import chav1961.funnypro.core.entities.VariableEntity;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProEntity.EntityType;
import chav1961.funnypro.core.interfaces.IFProExternalPluginsRepo.PluginItem;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProGlobalStack.GlobalStackTop;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProOperator;
import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProPredicate;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.plugins.StandardResolver;
import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.streams.chartarget.StringBuilderCharTarget;
import chav1961.purelib.streams.interfaces.CharacterTarget;

/**
 * <p>This class contains most common usable methods for this package</p> 
 * @author chav1961
 *
 */
public class FProUtil {
	private static final int 			EXPONENT_SIZE = 300; 
	private static final double[]		EXPONENT = new double[2 * EXPONENT_SIZE + 1];
	private static final IFProEntity[]	NULL_ARRAY = new IFProEntity[0]; 

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
		if (Utils.checkEmptyOrNullString(template)) {
			throw new IllegalArgumentException("Template can't be null or empty");
		}
		else {
			return simpleParser(source, from, template.toCharArray(), locations);
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
	public static void serialize(final DataOutput target, final IFProEntity entity) throws IOException, NullPointerException {
		if (target == null) {
			throw new NullPointerException("Target stream can't be null!"); 
		}
		else if (entity == null) {
			throw new NullPointerException("Entity to serialize can't be null!"); 
		}
		else {
			target.writeInt(entity.getEntityType().ordinal());		// Write operator type
			switch (entity.getEntityType()) {
				case string			:
				case integer		:
				case variable		:
					target.writeLong(entity.getEntityId());
					break;
				case real			:
					target.writeDouble(Double.longBitsToDouble(entity.getEntityId()));
					break;
				case anonymous		:
					break;
				case list			:
					target.writeInt((((IFProList)entity).getChild() != null ? 2 : 0) + (((IFProList)entity).getTail() != null ? 1 : 0));
					if (((IFProList)entity).getChild() != null) {
						serialize(target,((IFProList)entity).getChild());
					}
					if (((IFProList)entity).getTail() != null) {
						serialize(target,((IFProList)entity).getTail());
					}
					break;
				case operator		:
					target.writeLong(entity.getEntityId());
					target.writeInt(((IFProOperator)entity).getPriority());
					target.writeInt(((IFProOperator)entity).getOperatorType().ordinal());
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
					target.writeLong(entity.getEntityId());
					target.writeInt(((IFProPredicate)entity).getArity());
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
	public static IFProEntity deserialize(final DataInput source) throws IOException, NullPointerException {
		if (source == null) {
			throw new NullPointerException("Target stream can't be null!"); 
		}
		else {
			final EntityType	type = EntityType.values()[source.readInt()];

			switch (type) {
				case string			:
					return new StringEntity(source.readLong());
				case integer		:
					return new IntegerEntity(source.readLong());
				case real			:
					return new RealEntity(source.readDouble());
				case variable		:
					return new VariableEntity(source.readLong());
				case anonymous		:
					return new AnonymousEntity();
				case list			:
					final int			mask = source.readInt();
					return new ListEntity((mask & 0x02) != 0 ? deserialize(source) : null,(mask & 0x01) != 0 ? deserialize(source) : null); 
				case operator		:
					final long				operatorId = source.readLong();
					final int				priority = source.readInt();
					final OperatorType		opType = OperatorType.values()[source.readInt()];
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
					final long				predicateId = source.readLong();
					final int				arity = source.readInt();
					final IFProEntity[]		parms = new IFProEntity[arity];
					
					for (int index = 0; index < parms.length; index++) {
						parms[index] = deserialize(source);
					}
					return new PredicateEntity(predicateId,parms); 
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
		
			 if (leftType == rightType && peek.getEntityId() == entity.getEntityId()) {
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
						final IFProPredicate	peekP = (IFProPredicate)peek, entityP = (IFProPredicate)entity;  
						
						if (peekP.getArity() == entityP.getArity()) {
							final IFProEntity[]	left = peekP.getParameters(), right = entityP.getParameters();
							
							for (int index = 0, maxIndex = entityP.getArity(); index < maxIndex; index++) {
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
			final IFProEntity	oldVal = actual.oldValue;
			
			switch (actual.location) {
				case Change.IN_PARENT	:
					actual.container.setParent(oldVal);
					break;
				case Change.IN_CHILD	:
					((IFProList)actual.container).setChild(oldVal);
					break;
				case Change.IN_TAIL		:
					((IFProList)actual.container).setTail(oldVal);
					break;
				case Change.IN_CHAIN	:
					((IFProVariable)actual.container).setChain((IFProVariable)oldVal);
					break;
				case Change.IN_LEFT		:
					((IFProOperator)actual.container).setLeft(oldVal);
					break;
				case Change.IN_RIGHT	:
					((IFProOperator)actual.container).setRight(oldVal);
					break;
				default :
					((IFProPredicate)actual.container).getParameters()[actual.location] = oldVal;
					break;
			}
			temp = actual.next;		
			actual.next = null;		
			actual = temp;			
		}
	}
	
	/**
	 * <p>Duplicate entity with all children</p>
	 * @param source item to duplicate. Null source returns null result
	 * @return duplicated item
	 */
	public static IFProEntity duplicate(final IFProEntity source) {
		try{return duplicate(source, null);
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public static IFProEntity cloneEntity(final IFProEntity source) {
		try(final VarRepo	repo = new VarRepo()) {
			
			return duplicate(source, repo);
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}		
	
	private static IFProEntity duplicate(final IFProEntity source, final VarRepo repo) throws CloneNotSupportedException {
		if (source == null) {
			return null;
		}
		else {
			IFProEntity	e;
			
			switch (source.getEntityType()) {
				case string			:
					e = ((StringEntity)source).clone();
					break;
				case integer		:
					e = ((IntegerEntity)source).clone();
					break;
				case real			:
					e = ((RealEntity)source).clone();
					break;
				case anonymous		:
					e = ((AnonymousEntity)source).clone(); 
					break;
				case list			:
					final IFProList			list = new ListEntity(duplicate(((IFProList)source).getChild(),repo), duplicate(((IFProList)source).getTail(),repo)); 
					
					if (list.getChild() != null) {
						list.getChild().setParent(list);
					}
					if (list.getTail() != null) {
						list.getTail().setParent(list);
					}
					return list;
				case operator		:
					final IFProOperator		op = new OperatorEntity((IFProOperator)source).setLeft(duplicate(((IFProOperator)source).getLeft(),repo)).setRight(duplicate(((IFProOperator)source).getRight(),repo)); 
					
					if (op.getLeft() != null) {
						op.getLeft().setParent(op);
					}
					if (op.getRight() != null) {
						op.getRight().setParent(op);
					}
					if (source.isRuled()) {
						op.setRule(duplicate(((IFProOperator)source).getRule(),repo));
					}
					return op;
				case predicate		:
					final IFProPredicate	pred = new PredicateEntity(source.getEntityId());
					final int				arity = ((IFProPredicate)source).getArity(); 
					
					if (arity > 0) {
						final IFProEntity[]		parm = new IFProEntity[arity];
						
						for (int index = 0; index < arity; index++){
							if ((parm[index] = duplicate(((IFProPredicate)source).getParameters()[index],repo)) != null) {
								parm[index].setParent(pred);
							}
						}
						pred.setParameters(parm);
					}
					else {
						pred.setParameters(NULL_ARRAY);
					}
					if (source.isRuled()) {
						pred.setRule(duplicate(((IFProPredicate)source).getRule(),repo));
					}
					return pred;
				case variable		:
					final IFProVariable		var = new VariableEntity(source.getEntityId());
					
					if (repo == null) {
						final IFProVariable	ref = ((IFProVariable)source).getChain();
						
						var.setChain(ref);
						((IFProVariable)source).setChain(var);
						return var;
					}
					else {
						repo.storeVariable(var);
						return var;
					}					
				default :
					throw new UnsupportedOperationException("Entity duplication for ["+source.getEntityType()+"] is not supported yet!");
			}
			e.setParent(null);
			return e;
		}
	}

	/**
	 * <p>Remove entity and all it's children</p>
	 * @param source entity to remove
	 */
	public static void removeEntity(final SyntaxTreeInterface<?> repo, final IFProEntity source) {
		if (source != null) {
			source.setParent(null);
			
			switch (source.getEntityType()) {
				case string			:
					repo.removeName(source.getEntityId());
					break;
				case integer		:
				case real			:
				case anonymous		:
					break;
				case list			:
					if (((IFProList)source).getChild() != null) {
						removeEntity(repo,((IFProList)source).getChild());
						((IFProList)source).setChild(null);
					}
					if (((IFProList)source).getTail() != null) {
						removeEntity(repo,((IFProList)source).getTail());
						((IFProList)source).setTail(null);
					}
					break;
				case operator		:
					if (((IFProOperator)source).getLeft() != null) {
						removeEntity(repo,((IFProOperator)source).getLeft());
						((IFProOperator)source).setLeft(null);
					}
					if (((IFProOperator)source).getRight() != null) {
						removeEntity(repo,((IFProOperator)source).getRight());
						((IFProOperator)source).setRight(null);
					}
					break;
				case predicate		:
					final IFProEntity[]		parm = ((IFProPredicate)source).getParameters();
					
					for (int index = 0; index < parm.length; index++){
						removeEntity(repo,parm[index]);
						parm[index] = null;
					}
					break;
				case variable		:
					IFProVariable	current = ((IFProVariable)source).getChain();
					
					while (current.getChain() != source) {
						current = current.getChain(); 
					}
					current.setChain(((IFProVariable)source).getChain());
					((IFProVariable)source).setChain((IFProVariable)source);
					break;
				default :
					throw new IllegalArgumentException("Entity type ["+source.getEntityType()+"] can't be removed!");
			}
		}
	}
	
	/**
	 * <p>Does entity have any variable inside</p>
	 * @param source entity to test
	 * @return true if has, false otherwise
	 */
	public static boolean hasAnyVariable(final IFProEntity source) {
		if (source != null) {
			switch (source.getEntityType()) {
				case string	: case integer : case real : case anonymous :
					return false;
				case list			:
					if (hasAnyVariable(((IFProList)source).getChild())) {
						return true;
					}
					if (hasAnyVariable(((IFProList)source).getTail())) {
						return true;
					}
					return false;
				case operator		:
					if (hasAnyVariable(((IFProOperator)source).getLeft())) {
						return true;
					}
					if (hasAnyVariable(((IFProOperator)source).getRight())) {
						return true;
					}
					return false;
				case predicate		:
					final IFProEntity[]		parm = ((IFProPredicate)source).getParameters();
					
					for (int index = 0; index < parm.length; index++){
						if (hasAnyVariable(parm[index])) {
							return true;
						}
					}
					return false;
				case variable		:
					return true;
				default :
					throw new IllegalArgumentException("Entity type ["+source.getEntityType()+"] can't be removed!");
			}
		}
		else {
			return false;
		}
	}

	/**
	 * <p>Does entity have any variable inside</p>
	 * @param source
	 * @return
	 */
	public static boolean hasAnyVariableOrAnonymous(final IFProEntity source) {
		if (source != null) {
			switch (source.getEntityType()) {
				case string : case integer : case real :
					return false;
				case list			:
					if (hasAnyVariableOrAnonymous(((IFProList)source).getChild())) {
						return true;
					}
					if (hasAnyVariableOrAnonymous(((IFProList)source).getTail())) {
						return true;
					}
					return false;
				case operator		:
					if (hasAnyVariableOrAnonymous(((IFProOperator)source).getLeft())) {
						return true;
					}
					if (hasAnyVariableOrAnonymous(((IFProOperator)source).getRight())) {
						return true;
					}
					return false;
				case predicate		:
					final IFProEntity[]		parm = ((IFProPredicate)source).getParameters();
					
					for (int index = 0; index < parm.length; index++){
						if (hasAnyVariableOrAnonymous(parm[index])) {
							return true;
						}
					}
					return false;
				case anonymous : case variable :
					return true;
				default :
					throw new IllegalArgumentException("Entity type ["+source.getEntityType()+"] can't be removed!");
			}
		}
		else {
			return false;
		}
	}
	
	public static boolean unify(final IFProEntity mark, final IFProEntity left, final IFProEntity right, final IFProGlobalStack stack, final Change[] list) {
		list[0] = null;
		final boolean	result = unify(left, right, list);
		
		if (result) {
			if (list[0] != null) {
				stack.push(GlobalStack.getBoundStackTop(mark, mark, list[0]));
			}
		}
		else if (list[0] != null) {
			unbind(list[0]);
		}
		return result;
	}

	public static boolean unifyTemporaries(final IFProEntity mark, final IFProEntity left, final IFProEntity right, final IFProEntity created, final SyntaxTreeInterface<?> repo, final IFProGlobalStack stack, final Change[] list) {
		if (!unify(mark, left, right, stack, list)) {
			removeEntity(repo, created);
			return false;
		}
		else {
			stack.push(GlobalStack.getTemporaryStackTop(mark,created));
			return true;
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public static void releaseTemporaries(final IFProEntity mark, final SyntaxTreeInterface<?> repo, final IFProGlobalStack stack) {
		GlobalStackTop	item;
		
		while (!stack.isEmpty() && (item = stack.peek()).getEntityAssocated() == mark) {
			switch (item.getTopType()) {
				case bounds		:
					unbind(((GlobalStack.BoundStackTop<Change>)item).getChangeChain());
					stack.pop();
					break;
				case andChain	:
				case external	:
				case orChain	:
				case iterator	:
					return;
				case temporary	:
					removeEntity(repo, ((GlobalStack.TemporaryStackTop)item).getEntity());
					stack.pop();
					break;
				default	:
					throw new UnsupportedOperationException("Stack top type ["+item.getTopType()+"] is not supported yet");
			}
		}
	}
	
	public static enum ContentType {
		Anon, Var, NonVar, Atom, Integer, Float, Number, Atomic, Compound,		
	}	
	
	/**
	 * <p>Test entity type in the FPro terminology</p> 
	 * @param entity entity to test
	 * @param type check type (according to FRpo tests)
	 * @return true if entity is a tested type
	 */
	public static boolean isEntityA(final IFProEntity entity, final ContentType type) throws NullPointerException {
		if (type == null) {
			throw new NullPointerException("Type can't be null");
		}
		else if (entity == null) {
			return false;
		}
		else {
			final EntityType	entityType = entity.getEntityType();
			
			switch (type) {
				case Anon		:
					return entityType == EntityType.anonymous;
				case Var		:
					return entityType == EntityType.variable;
				case NonVar		:
					return entityType != EntityType.variable && entityType != EntityType.anonymous;
				case Atom		:
					return entityType == EntityType.string || entityType == EntityType.predicate; 
				case Integer	:
					return entityType == EntityType.integer;
				case Float		:
					return entityType == EntityType.real;
				case Number		:
					return entityType == EntityType.integer || entityType == EntityType.real; 
				case Atomic		:
					return entityType == EntityType.integer || entityType == EntityType.real || entityType == EntityType.string || entityType == EntityType.predicate || entityType == EntityType.anonymous; 
				case Compound	:	
					return entityType == EntityType.list || entityType == EntityType.operator;
				default :
					return false;
			}
		}
	}
	
	/**
	 * <p>Test two entites are identical.</p>
	 * @param left left entity to test
	 * @param right right entity to test
	 * @return true if entites are identical
	 */
	public static boolean isIdentical(final IFProEntity left, final IFProEntity right) {
		if (left == right) {
			return true;
		}
		else if (left == null || right == null) {
			return false;
		}
		else if (left.getEntityId() != right.getEntityId() || left.getEntityType() != right.getEntityType()) {
			return false;
		}
		else {
			switch (left.getEntityType()) {
				case string : case integer : case real : case anonymous : case variable :
					return true;
				case list		:
					return isIdentical(((IFProList)left).getChild(),((IFProList)right).getChild()) && isIdentical(((IFProList)left).getTail(),((IFProList)right).getTail()); 
				case operator	:
					if (((IFProOperator)left).getPriority() == ((IFProOperator)right).getPriority() && ((IFProOperator)left).getOperatorType() == ((IFProOperator)right).getOperatorType()) {
						return isIdentical(((IFProOperator)left).getLeft(),((IFProOperator)right).getLeft()) && isIdentical(((IFProOperator)left).getRight(),((IFProOperator)right).getRight()); 
					}
					else {
						return false;
					}						
				case predicate	:
					final int	size;
					
					if ((size = ((IFProPredicate)left).getArity()) == ((IFProPredicate)right).getArity()) {
						final IFProEntity[]	leftP = ((IFProPredicate)left).getParameters();
						final IFProEntity[]	rightP = ((IFProPredicate)right).getParameters();
						
						for (int index = 0; index < size; index++) {
							if (!isIdentical(leftP[index], rightP[index])) {
								return false;
							}
						}
						return true;
					}
					else {
						return false;
					}
				default :
					throw new UnsupportedOperationException("Entity type ["+left.getEntityType()+"] is not supported yet");
			}
		}
	}

	public static String asString(final IFProParserAndPrinter pap, final IFProEntity entity) throws PrintingException, IOException {
		final StringBuilder		sb = new StringBuilder();
		final CharacterTarget	ct = new StringBuilderCharTarget(sb);
			
		switch (entity.getEntityType()) {
			case string	:
				pap.putEntity(entity, ct);
				return sb.substring(1, sb.length() - 1);
			case integer: case list: case real: case predicate: case operator:
				pap.putEntity(entity, ct);
				return sb.toString();
			case operatordef: case externalplugin: case anonymous: case variable: case any:
				return null;
			default	:
				throw new UnsupportedOperationException("Entity type ["+entity.getEntityType()+"] is not supported yet"); 
		}
	}
	
	@FunctionalInterface
	public interface MakeListNodeCallback<T> {
		IFProEntity toEntity(T value)  throws ContentException, IOException;
	}
	
	public static <T> IFProList toList(final Iterable<T> list, final MakeListNodeCallback<T> callback) throws ContentException, IOException {
		if (list == null) {
			throw new NullPointerException("List to convert content can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback to process content can't be null"); 
		}
		else {
			IFProList	actual = null, result = null;
			
			for (T item : list) {
				final IFProEntity	entity = callback.toEntity(item);
				final IFProList		next = new ListEntity(entity,null);
				
				if (result == null) {
					result = next;
				}
				else {
					actual.setTail(next);
				}
				next.setParent(actual);
				actual = next;
			}
			return result == null ? new ListEntity(null, null) : result;
		}
	}

	@FunctionalInterface
	public interface ProcessListNodeCallback {
		ContinueMode processEntity(IFProEntity node) throws ContentException, IOException;
		
		default ContinueMode processTail(IFProEntity node) throws ContentException, IOException {
			return processEntity(node);
		}
	}
	
	/**
	 * <p>Process list element-by-element</p>
	 * @param list list to process
	 * @param callback callback to process list items
	 * @return Continue mode. Can't be null. Stop also means empty list
	 * @throws IOException on any I/O errors
	 * @throws ContentException on any content errors 
	 */
	public static ContinueMode forList(final IFProList list, final ProcessListNodeCallback callback) throws ContentException, IOException {
		if (list == null) {
			throw new NullPointerException("List to process content can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Callback to process content can't be null"); 
		}
		else {
			IFProEntity		actual = list;
			ContinueMode	rc = ContinueMode.STOP;
			
			while (actual != null) {
				if (actual.getEntityType() == EntityType.list) {
					if (((IFProList)actual).getChild() != null && (rc = callback.processEntity(((IFProList)actual).getChild())) != ContinueMode.CONTINUE) {
						return rc;
					}
					else {
						actual = ((IFProList)actual).getTail();
					}
				}
				else {
					return callback.processTail(actual);
				}
			}
			return rc;
		}
	}
	
	public static <T extends Enum<?>> void fillQuickIds(final Map<Long,QuickIds<T>> repo, final QuickIds<T> data) {
		if (!repo.containsKey(data.id)) {
			repo.put(data.id,data);
		}
		else {
			data.next = repo.get(data.id);
			repo.put(data.id,data);
		}
	}	
	
	public static <T extends Enum<?>> T detect(final LongIdMap<QuickIds<T>> repo, final IFProEntity entity, final T defaultValue) {
		if (repo == null) {
			throw new NullPointerException("Repository can't be null"); 
		}
		else if (entity == null) {
			return defaultValue; 
		}
		else {
			QuickIds<T>	start = repo.get(entity.getEntityId());
			
			if (start != null) {
				final EntityType 	type = entity.getEntityType();

				if (type == EntityType.operator) {
					final IFProOperator	oper = (IFProOperator)entity;
					final int			priority = oper.getPriority();
					final OperatorType	operType = oper.getOperatorType(); 
					
					while (start != null) {
						final IFProOperator	def = (IFProOperator)start.def;
						
						if (def.getPriority() == priority &&  def.getOperatorType() == operType) {
							return start.action;
						}
						else {
							start = start.next;
						}
					}
				}
				else if (type == EntityType.predicate) {
					final int	arity = ((IFProPredicate)entity).getArity();
					
					while (start != null) {
						if (((IFProPredicate)start.def).getArity() == arity) {
							return start.action;
						}
						else {
							start = start.next;
						}
					}
				}
				else {
					throw new UnsupportedOperationException("Entity type ["+type+"] is not supported yet");
				}
				return defaultValue; 
			}
			else {
				return defaultValue; 
			}
		}
	}

	public static <R,G> ResolvableAndGlobal<R,G> getStandardResolver(final IFProEntitiesRepo repo) {
		for (PluginItem item : repo.pluginsRepo().seek(StandardResolver.PLUGIN_NAME, StandardResolver.PLUGIN_PRODUCER, StandardResolver.PLUGIN_VERSION)) {
			return new ResolvableAndGlobal(item.getDescriptor().getPluginEntity().getResolver(), item.getGlobal());
		}
		throw new IllegalStateException("No standard resolver was registered in the system. Use inference with explicit call");
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
					if (start == var2) {	// Chains are already joined!
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
//		final Change	ch = new Change();
//		
//		ch.next = changesList[0];
//		ch.container = container;
//		ch.oldValue = oldValue;
//		ch.location = location;
//		changesList[0] = ch;
		changesList[0] = new Change(changesList[0], container, oldValue, location);
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

		public Change() {
		}
		
		public Change(final Change next, final IFProEntity container, final IFProEntity oldValue, final int location) {
			this.next = next;
			this.container = container;
			this.oldValue = oldValue;
			this.location = location;
		}

		@Override
		public String toString() {
			return "Change [next=" + next + ", container=" + container + ", oldValue=" + oldValue + ", location=" + location + "]";
		}
	}
}
