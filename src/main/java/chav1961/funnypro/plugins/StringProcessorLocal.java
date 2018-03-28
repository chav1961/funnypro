package chav1961.funnypro.plugins;

import java.util.List;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;

class StringProcessorLocal {
	final Change[]			list = new Change[1];
	IFProCallback			callback;
	IFProGlobalStack		stack;
	List<IFProVariable>		vars;
}
