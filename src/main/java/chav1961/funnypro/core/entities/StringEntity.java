package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;

public class StringEntity implements IFProEntity {
	private long		id;
	private IFProEntity	parent = null;

	public StringEntity(final long id, final IFProEntity parent) {
		this.id = id;	this.parent = parent;
	}	
	
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
}
