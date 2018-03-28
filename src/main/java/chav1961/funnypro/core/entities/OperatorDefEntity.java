package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProOperator;

/**
 * <p>This class describes :- op/3 entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class OperatorDefEntity implements IFProOperator {
	private long			id;
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
		this.type = type;	this.prty = prty;
		this.id = id;
	}

	@Override public EntityType getEntityType() {return EntityType.operatordef;}
	@Override public long getEntityId() {return id;}
	@Override public IFProEntity setEntityId(long entityId) {this.id = entityId; return this;}
	@Override public OperatorType getOperatorType() {return type;}
	@Override public int getPriority() {return prty;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}
	@Override public IFProEntity getLeft() {return null;}
	@Override public IFProEntity getRight() {return null;}
	@Override public IFProOperator setLeft(final IFProEntity node) {throw new UnsupportedOperationException("Don't use this method for this class!");}
	@Override public IFProOperator setRight(IFProEntity node) {throw new UnsupportedOperationException("Don't use this method for this class!");}
	@Override public IFProEntity getRule() {return null;}
	@Override public void setRule(IFProEntity rule) {throw new UnsupportedOperationException("Don't use this method for this class!");}


	@Override
	public int getUnderlyingPriority() {
		switch (getOperatorType()) {
			case fx 	: return getPriority()-1;
			case fy 	: return getPriority();
			case xf 	: return getPriority()-1;
			case yf 	: return getPriority();
			default 	: throw new IllegalArgumentException("Unavailable!");
		}
	}

	@Override 
	public int getUnderlyingPriority(final int prioritySide) {
		if (prioritySide == LEFT) {
			switch (getOperatorType()) {
				case xfx 	: return getPriority()-1;
				case xfy 	: return getPriority()-1;
				case yfx 	: return getPriority();
				default 	: throw new IllegalArgumentException("Unavailable!");
			}
		}
		else {
			switch (getOperatorType()) {
				case xfx 	: return getPriority()-1;
				case xfy 	: return getPriority();
				case yfx 	: return getPriority()-1;
				default 	: throw new IllegalArgumentException("Unavailable!");
			}
		}
	}
	
	@Override public String toString() {return "OperatorDef [id=" + id + ", type=" + type + ", prty=" + prty + "]";}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + prty;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		OperatorDefEntity other = (OperatorDefEntity) obj;
		if (id != other.id) return false;
		if (prty != other.prty) return false;
		if (type != other.type) return false;
		return true;
	}
}
