package chav1961.funnypro.core.entities;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProList;

/**
 * <p>This class describes list chain entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class ListEntity implements IFProList {
	private IFProEntity		parent;
	private IFProEntity		child;
	private IFProEntity		tail;

	/**
	 * <p>Constructor of the object</p>
	 * @param parent parent node
	 */
	public ListEntity(final IFProEntity parent) {
		this.parent = parent;
	}
	
	/**
	 * <p>Constructor of the object</p>
	 * @param child any child (exactly one of the list element)
	 * @param tail tail of the list. Can be either null (termination of the list), or another {@link ListEntity} (continuation of the list) or
	 * {@link VariableEntity}/{@link AnonymousEntity} to mark <i>tail</i> of the list (for example [a,b,c|<b>X</b>). Any other nodes are illegal 
	 */
	public ListEntity(final IFProEntity child, final IFProEntity tail) {
		this.child = child;	this.tail = tail;
	}

	@Override public EntityType getEntityType() {return EntityType.list;}
	@Override public long getEntityId() {return IFProEntity.LIST_ENTITY_ID;}
	@Override public IFProEntity setEntityId(long entityId) {return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}
	@Override public IFProEntity getChild() {return child;}
	@Override public IFProList setChild(final IFProEntity child) {this.child = child; return this;}
	@Override public IFProEntity getTail() {return tail;}
	@Override public IFProList setTail(final IFProEntity tail) {this.tail = tail; return this;}
	@Override public String toString() {return "ListEntity [child=" + child + ", tail=" + tail + "]";}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((child == null) ? 0 : child.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((tail == null) ? 0 : tail.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ListEntity other = (ListEntity) obj;
		if (child == null) { 
			if (other.child != null) return false;
		} else if (!child.equals(other.child)) return false;
		if (tail == null) {
			if (other.tail != null) return false;
		} else if (!tail.equals(other.tail)) return false;
		return true;
	}
}
