package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;

/**
 * <p>This class describes integer entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class IntegerEntity implements IFProEntity {
	private long		id;
	private IFProEntity	parent = null;

	/**
	 * <p>Constructor of the object</p>
	 * @param id value of integer (not id!)
	 * @param parent parent node
	 */
	public IntegerEntity(final long id, final IFProEntity parent) {
		this.id = id;	this.parent = parent;
	}	
	
	/**
	 * <p>Constructor of the object</p>
	 * @param id value of integer (not id!)
	 */
	public IntegerEntity(final long id) {
		this.id = id;
	}

	@Override public EntityType getEntityType() {return EntityType.integer;}
	@Override public long getEntityId() {return id;}
	@Override public IFProEntity setEntityId(final long entityId) {this.id = entityId; return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}

	@Override public String toString() {return "LongEntity [id=" + id + "]";}

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
		IntegerEntity other = (IntegerEntity) obj;
		if (id != other.id) return false;
		return true;
	}

	@Override
	public IntegerEntity clone() throws CloneNotSupportedException {
		return (IntegerEntity)super.clone();
	}
}
