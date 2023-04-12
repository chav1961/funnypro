package chav1961.funnypro.core;

import chav1961.funnypro.core.interfaces.IFProEntity;

public class QuickIds<T extends Enum<?>> implements Comparable<QuickIds<T>>{
	public long 		id;
	public IFProEntity	def;
	public T 			action;
	public QuickIds<T>	next = null;
	
	public QuickIds(final IFProEntity def, final T action) {
		this.id = def.getEntityId();
		this.def = def;			
		this.action = action;
	}

	@Override
	public int compareTo(final QuickIds<T> o) {
		return o.id < id ? 1 : (o.id > id ? -1 : 0);
	}

	@Override public String toString() {return "QuickIds [id=" + id + ", def=" + def + ", action=" + action + "]";}
}