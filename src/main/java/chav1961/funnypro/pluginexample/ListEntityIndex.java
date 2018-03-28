package chav1961.funnypro.pluginexample;

import java.util.List;

import chav1961.funnypro.core.FProUtil.Change;
import chav1961.funnypro.core.interfaces.IFProGlobalStack;
import chav1961.funnypro.core.interfaces.IFProList;
import chav1961.funnypro.core.interfaces.IFProVariable;
import chav1961.funnypro.core.interfaces.IFProVM.IFProCallback;

class ListEntityIndex {
	Change[]				list = new Change[1];
	IFProList				currentItem;
	IFProCallback			callback;
	IFProGlobalStack		stack;
	List<IFProVariable>		vars;
}