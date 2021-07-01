package chav1961.funnypro.plugins;

import java.util.ArrayList;
import java.util.List;

import chav1961.funnypro.core.ParserAndPrinter;
import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;

class DatabaseProcessorGlobal {
	final List<DatabaseProcessorLocal>	collection = new ArrayList<>();
	IFProEntitiesRepo		repo;
	IFProParserAndPrinter	pap;
}
