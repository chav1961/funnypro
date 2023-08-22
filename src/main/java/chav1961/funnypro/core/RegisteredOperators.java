package chav1961.funnypro.core;

import chav1961.funnypro.core.interfaces.IFProOperator.OperatorType;

public class RegisteredOperators<T extends Enum<?>> {
	public final int			priority;
	public final OperatorType	type;
	public final String			text;
	public final T				action;
	
	public RegisteredOperators(final int priority, final OperatorType type, final String text, final T action) {
		this.priority = priority;
		this.type  = type;
		this.text = text;
		this.action = action;
	}

	@Override public String toString() {return "RegisteredOperators [priority=" + priority + ", type=" + type + ", text=" + text + ", action = " + action + "]";}
}