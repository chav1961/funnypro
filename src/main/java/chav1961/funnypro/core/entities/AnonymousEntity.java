package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;

public class AnonymousEntity implements IFProEntity {
	private IFProEntity	parent;

	public AnonymousEntity(final IFProEntity parent){
		this.parent = parent;
	}
	
	public AnonymousEntity(){}
	
	@Override public EntityType getEntityType() {return EntityType.anonymous;}
	@Override public long getEntityId() {return IFProEntity.ANON_ENTITY_ID;}
	@Override public IFProEntity setEntityId(long entityId) {return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}

	@Override public String toString() {return "AnonymousEntity []";}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return true;
	}
}
