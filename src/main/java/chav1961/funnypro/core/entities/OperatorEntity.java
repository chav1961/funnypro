package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProOperator;


public class OperatorEntity implements IFProOperator {
	private long			id;
	private OperatorType	type;
	private int 			prty;
	private IFProEntity		left, right, parent, rule;

	public OperatorEntity(final IFProEntity parent, final IFProOperator op) {
		this(parent,op.getPriority(),op.getType(),op.getEntityId());
	}
	
	public OperatorEntity(final IFProOperator op) {
		this(op.getPriority(),op.getType(),op.getEntityId());
	}

	public OperatorEntity(final IFProEntity parent,final int prty, final OperatorType type, final long id) {
		this.type = type;	this.prty = prty;
		this.id = id;		this.parent = parent;
	}
	
	public OperatorEntity(final int prty, final OperatorType type, final long id) {
		this.type = type;	this.prty = prty;
		this.id = id;
	}

	@Override public EntityType getEntityType() {return EntityType.operator;}
	@Override public long getEntityId() {return id;}
	@Override public IFProEntity setEntityId(long entityId) {this.id = entityId; return this;}
	@Override public OperatorType getType() {return type;}
	@Override public int getPriority() {return prty;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}
	@Override public IFProEntity getLeft() {return left;}
	@Override public IFProEntity getRight() {return right;}
	@Override public IFProOperator setLeft(final IFProEntity node) {this.left = node; return this;}
	@Override public IFProOperator setRight(IFProEntity node) {this.right = node; return this;}
	@Override public IFProEntity getRule() {return rule;}
	@Override public void setRule(final IFProEntity rule) {this.rule = rule;}

	@Override
	public int getUnderlyingPriority() {
		switch (getType()) {
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
			switch (getType()) {
				case xfx 	: return getPriority()-1;
				case xfy 	: return getPriority()-1;
				case yfx 	: return getPriority();
				default 	: throw new IllegalArgumentException("Unavailable!");
			}
		}
		else {
			switch (getType()) {
				case xfx 	: return getPriority()-1;
				case xfy 	: return getPriority();
				case yfx 	: return getPriority()-1;
				default 	: throw new IllegalArgumentException("Unavailable!");
			}
		}
	}
	
	@Override public String toString() {return "OperatorEntity [id=" + id + ", type=" + type + ", prty=" + prty + "]";}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + prty;
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		OperatorEntity other = (OperatorEntity) obj;
		if (id != other.id) return false;
		if (left == null) {
			if (other.left != null) return false;
		} else if (!left.equals(other.left)) return false;
		if (parent == null) {
			if (other.parent != null) return false;
		} else if (!parent.equals(other.parent)) return false;
		if (prty != other.prty) return false;
		if (right == null) {
			if (other.right != null) return false;
		} else if (!right.equals(other.right)) return false;
		if (rule == null) {
			if (other.rule != null) return false;
		} else if (!rule.equals(other.rule)) return false;
		if (type != other.type) return false;
		return true;
	}
}
