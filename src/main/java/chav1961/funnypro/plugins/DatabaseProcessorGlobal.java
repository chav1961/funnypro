package chav1961.funnypro.plugins;

import java.sql.Connection;

import chav1961.funnypro.core.interfaces.IFProEntitiesRepo;
import chav1961.funnypro.core.interfaces.IFProParserAndPrinter;
import chav1961.purelib.basic.ReusableInstances;

class DatabaseProcessorGlobal {
	final ReusableInstances<DatabaseProcessorLocal>	collection = new ReusableInstances<>(()->new DatabaseProcessorLocal(),(i)->{i.callback = null; i.stack = null; i.vars = null; return i;});
	final Connection		conn;
	IFProEntitiesRepo		repo;
	IFProParserAndPrinter	pap;

	public DatabaseProcessorGlobal(Connection conn) {
		this.conn = conn;
	}
}
