package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;

/**
 * <p>This class describes real entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class RealEntity implements IFProEntity {
	private long			id;
	private IFProEntity		parent = null;

	/**
	 * <p>Constructor of the object</p>
	 * @param value real entity value
	 * @param parent parent node
	 */
	public RealEntity(final double value, final IFProEntity parent) {
		this.id = Double.doubleToLongBits(value);	
		this.parent = parent;
	}	
	
	/**
	 * <p>Constructor of the object</p>
	 * @param value real entity value
	 */
	public RealEntity(final double value) {
		this.id = Double.doubleToLongBits(value);
	}
	
	@Override public EntityType getEntityType() {return EntityType.real;}
	@Override public long getEntityId() {return id;}
	@Override public IFProEntity setEntityId(final long entityId) {this.id = entityId; return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}

	@Override public String toString() {return "RealEntity [value=" + Double.longBitsToDouble(id) + "]";}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		RealEntity other = (RealEntity) obj;
		if (id != other.id) return false;
		return true;
	}

	@Override
	public RealEntity clone() throws CloneNotSupportedException {
		return (RealEntity)super.clone();
	}
}
