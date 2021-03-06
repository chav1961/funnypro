package chav1961.funnypro.core;

import java.util.List;

import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;

class LocalDescriptor {
	public IFProCallback			callback;
	public IFProGlobalStack			stack;
	public IFProParserAndPrinter	pap;
	public List<IFProVariable>		vars;
	public String[]					varNames = null;
	
	@Override public String toString() {return "LocalDescriptor [stack=" + stack + ", pap=" + pap + "]";}
}