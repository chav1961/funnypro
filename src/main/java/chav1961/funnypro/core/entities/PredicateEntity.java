package chav1961.funnypro.core.entities;

import java.util.Arrays;

import chav1961.funnypro.core.interfaces.IFProEntity;
import chav1961.funnypro.core.interfaces.IFProPredicate;

/**
 * <p>This class describes predicate entity</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class PredicateEntity implements IFProPredicate {
	private long				id;
	private IFProEntity[]		parm;
	private IFProEntity			parent;
	private IFProEntity			rule;

	/**
	 * <p>Constructor of the object</p>
	 * @param id predicate name id (need be registered in the term tree)
	 * @param parm predicate parameters (up to 64)
	 */
	public PredicateEntity(final long id, final IFProEntity... parm) {
		this.id = id;			
		this.parm = parm;
		for (IFProEntity item : parm) {
			item.setParent(this);
		}
	}

	@Override public EntityType getEntityType() {return EntityType.predicate;}
	@Override public long getEntityId() {return id;}
	@Override public IFProEntity setEntityId(final long entityId) {this.id = entityId; return this;}
	@Override public IFProEntity getParent() {return parent;}
	@Override public IFProEntity setParent(final IFProEntity entity) {this.parent = entity; return this;}
	@Override public int getArity() {return parm == null ? 0 : parm.length;}
	@Override public IFProEntity[] getParameters() {return parm;}
	@Override public void setParameters(final IFProEntity... entity) {this.parm = entity;}
	@Override public IFProEntity getRule() {return rule;}
	@Override public IFProEntity setRule(final IFProEntity rule) {this.rule = rule; return this;}
	@Override public boolean isRuled() {return rule != null;}

	@Override public String toString() {return "PredicateEntity [id=" + id + ", arity=" + getArity() + ", rule=" + rule + "]";}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + Arrays.hashCode(parm);
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PredicateEntity other = (PredicateEntity) obj;
		if (id != other.id) return false;
		if (!Arrays.equals(parm, other.parm)) return false;
		if (rule == null) {
			if (other.rule != null) return false;
		} else if (!rule.equals(other.rule)) return false;
		return true;
	}
}
