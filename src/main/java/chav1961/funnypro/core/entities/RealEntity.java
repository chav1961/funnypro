package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;

/**
 * <p>This class describes real entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class RealEntity implements IFProEntity {
	private double			value;
	private IFProEntity		parent = null;

	/**
	 * <p>Constructor of the object</p>
	 * @param value real entity value
	 * @param parent parent node
	 */
	public RealEntity(final double value, final IFProEntity parent) {
		this.value = value;	this.parent = parent;
	}	
	
	/**
	 * <p>Constructor of the object</p>
	 * @param value real entity value
	 */
	public RealEntity(final double value) {
		this.value = value;
	}
	
	@Override public EntityType getEntityType() {return EntityType.real;}
	@Override public long getEntityId() {return Double.doubleToLongBits(value);}
	@Override public IFProEntity setEntityId(final long entityId) {this.value = Double.longBitsToDouble(entityId); return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}

	@Override public String toString() {return "RealEntity [value=" + value + "]";}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		RealEntity other = (RealEntity) obj;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
			return false;
		return true;
	}
}
