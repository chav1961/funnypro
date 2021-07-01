package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;

/**
 * <p>This class describes string entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class StringEntity implements IFProEntity {
	public static final long	EMPTY_STRING_ID = 0;
	
	private long		id;
	private IFProEntity	parent = null;

	/**
	 * <p>Constructor of the object</p>
	 * @param id string id (need be registered in the string tree)
	 * @param parent parent node
	 */
	public StringEntity(final long id, final IFProEntity parent) {
		this.id = id;	this.parent = parent;
	}	
	
	/**
	 * <p>Constructor of the object</p>
	 * @param id string id (need be registered in the string tree)
	 */
	public StringEntity(final long id) {
		this.id = id;
	}	

	@Override public EntityType getEntityType() {return EntityType.string;}
	@Override public long getEntityId() {return id;}
	@Override public IFProEntity setEntityId(final long entityId) {this.id = entityId; return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}

	@Override public String toString() {return "StringEntity [id=" + id + "]";}

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
		StringEntity other = (StringEntity) obj;
		if (id != other.id) return false;
		return true;
	}

	@Override
	public StringEntity clone() throws CloneNotSupportedException {
		return (StringEntity)super.clone();
	}
}
