package chav1961.funnypro.plugins;

import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.purelib.basic.ReusableInstances;

class IOProcessorGlobal {
	final ReusableInstances<IOProcessorLocal>	collection = new ReusableInstances<>(
														()->new IOProcessorLocal(), 
														(i)->{i.callback = null; i.stack = null; i.vars = null; return i;}
													);
	IFProEntitiesRepo		repo;
	IFProParserAndPrinter	pap;
}
