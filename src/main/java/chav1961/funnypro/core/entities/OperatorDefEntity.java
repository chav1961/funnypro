package chav1961.funnypro.core.entities;

import java.util.Arrays;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProOperator;

/**
 * <p>This class describes :- op/3 entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class OperatorDefEntity implements IFProOperator {
	private long[]			ids;
	private OperatorType	type;
	private int 			prty;
	private IFProEntity		parent;

	/**
	 * <p>Constructor of the object</p>
	 * @param prty operator priority
	 * @param type operator type
	 * @param id operator mnemonics id (need be registered in the term tree)
	 */
	public OperatorDefEntity(final int prty, final OperatorType type, final long id) {
		this(prty, type, new long[] {id});
	}
	
	/**
	 * <p>Constructor of the object</p>
	 * @param prty operator priority
	 * @param type operator type
	 * @param ids operator mnemonics id's (need be registered in the term tree)
	 */
	public OperatorDefEntity(final int prty, final OperatorType type, final long[] ids) {
		this.type = type;	this.prty = prty;
		this.ids = ids;
	}

	@Override public EntityType getEntityType() {return EntityType.operatordef;}
	@Override public long getEntityId() {return ids[0];}
	@Override public IFProEntity setEntityId(long entityId) {this.ids[0] = entityId; return this;}
	@Override public OperatorType getOperatorType() {return type;}
	@Override public int getPriority() {return prty;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}
	@Override public IFProEntity getLeft() {return null;}
	@Override public IFProEntity getRight() {return null;}
	@Override public IFProOperator setLeft(final IFProEntity node) {throw new UnsupportedOperationException("Don't use this method for this class!");}
	@Override public IFProOperator setRight(IFProEntity node) {throw new UnsupportedOperationException("Don't use this method for this class!");}
	@Override public IFProEntity getRule() {return null;}
	@Override public IFProEntity setRule(IFProEntity rule) {throw new UnsupportedOperationException("Don't use this method for this class!");}

	public long[] getEntities() {
		return ids;
	}
	
	@Override
	public int getUnderlyingPriority() {
		return IFProOperator.getUnderlyingPriority(this);
	}

	@Override 
	public int getUnderlyingPriority(final int prioritySide) {
		return IFProOperator.getUnderlyingPriority(this,prioritySide);
	}

	@Override
	public String toString() {
		return "OperatorDefEntity [ids=" + Arrays.toString(ids) + ", type=" + type + ", prty=" + prty + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(ids);
		result = prime * result + prty;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		OperatorDefEntity other = (OperatorDefEntity) obj;
		if (!Arrays.equals(ids, other.ids)) return false;
		if (prty != other.prty) return false;
		if (type != other.type) return false;
		return true;
	}
}
