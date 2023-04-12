package chav1961.funnypro.core;

import chav1961.funnypro.core.interfaces.IResolvable;

public class ResolvableAndGlobal<G,L> {
	public final IResolvable<G,L>	resolver;
	public final G					global;
	
	public ResolvableAndGlobal(final IResolvable<G,L> resolver, final G global) {
		this.resolver = resolver;
		this.global = global;
	}
}