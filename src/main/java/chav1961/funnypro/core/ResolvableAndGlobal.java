package chav1961.funnypro.core;

import chav1961.funnypro.core.interfaces.IResolvable;

public class ResolvableAndGlobal<T> {
	public IResolvable<T,?>	resolver;
	public T				global;
	
	public ResolvableAndGlobal(final IResolvable<T,?> resolver, final T global) {
		this.resolver = resolver;
		this.global = global;
	}
}