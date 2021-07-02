package chav1961.funnypro.plugins;

import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.purelib.basic.ReusableInstances;

class StringProcessorGlobal {
	final ReusableInstances<StringProcessorLocal>	collection = new ReusableInstances<>(()->new StringProcessorLocal(), (i)->{i.callback = null; i.stack = null; i.vars = null; return i;});
	IFProEntitiesRepo		repo;
	IFProParserAndPrinter	pap;
}
