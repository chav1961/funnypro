package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProVariable;

public class VariableEntity implements IFProVariable {
	private long			id;
	private IFProEntity		parent = null;
	private IFProVariable	chain = this;
	

	public VariableEntity(final long id, final IFProEntity parent) {
		this.id = id;	this.parent = parent;
	}	
	
	public VariableEntity(final long id) {
		this.id = id;
	}

	@Override public EntityType getEntityType() {return EntityType.variable;}
	@Override public long getEntityId() {return id;}
	@Override public IFProEntity setEntityId(final long entityId) {this.id = entityId; return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}
	@Override public IFProVariable getChain() {return chain;}
	@Override public IFProVariable setChain(final IFProVariable chain) {this.chain = chain; return this;}

	@Override public String toString() {return "VariableEntity [id=" + id + "]";}

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
		VariableEntity other = (VariableEntity) obj;
		if (id != other.id) return false;
		return true;
	}

}
