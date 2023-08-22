package chav1961.funnypro.core;


public class RegisteredPredicates<T extends Enum<?>> {
	public final String	text;		
	public final T		action;
	
	public RegisteredPredicates(final String text, final T action) {
		this.text = text;
		this.action = action;
	}

	@Override public String toString() {return "RegisteredPredicates [text=" + text + ", action=" + action + "]";}
}