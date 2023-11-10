package chav1961.funnypro.plugins;

import java.util.List;

import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;
import chav1961.funnypro.core.interfaces.IFProVariable;

class StandardResolverLocal {
	public IFProCallback			callback;
	public IFProGlobalStack			stack;
	public IFProParserAndPrinter	pap;
	public List<IFProVariable>		vars;
	public String[]					varNames = null;
	
	@Override 
	public String toString() {
		return "StandardResolverLocal [stack=" + stack + ", pap=" + pap + "]";
	}
}